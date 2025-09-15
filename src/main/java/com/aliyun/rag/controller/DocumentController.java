package com.aliyun.rag.controller;

import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.DocumentRequest;
import com.aliyun.rag.model.PageResult;
import com.aliyun.rag.model.User;
import com.aliyun.rag.service.AuthService;
import com.aliyun.rag.service.RAGService;
import com.aliyun.rag.service.QiniuUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 文档管理控制器
 * <p>
 * 提供文档上传、查询、删除等RESTful接口
 * 支持Multipart文件上传和跨域访问
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
@RestController
@RequestMapping("/documents")

public class DocumentController {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final RAGService ragService;
    private final QiniuUploadService qiniuUploadService;
    private final AuthService authService;

    public DocumentController(RAGService ragService, QiniuUploadService qiniuUploadService,AuthService authService) {
        this.ragService = ragService;
        this.qiniuUploadService = qiniuUploadService;
        this.authService = authService;
    }

    /**
     * 上传文档（同步）
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@Valid @ModelAttribute DocumentRequest request, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            User currentUser = (User)httpRequest.getAttribute("currentUser");
            System.out.println("当前用户: " + currentUser);
            
            // 先上传文件到七牛云
            String fileUrl = qiniuUploadService.uploadFile(request.getFile(), currentUser);

            try {
                // 然后处理文档内容用于知识库，并保存数据库记录
                DocumentInfo documentInfo = ragService.uploadDocument(request, currentUser, fileUrl);
                
                // 添加文件URL到响应中
                documentInfo.setFileName(fileUrl);
                
                return ResponseEntity.ok(documentInfo);
            } catch (Exception e) {
                // 如果数据库操作失败，需要删除已上传到七牛云的文件
                try {
                    qiniuUploadService.deleteFile(fileUrl);
                    log.info("已回滚七牛云文件上传: {}", fileUrl);
                } catch (Exception deleteException) {
                    log.warn("回滚七牛云文件上传失败: {}", deleteException.getMessage());
                }
                
                throw e; // 重新抛出异常
            }
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return ResponseEntity.badRequest().body("文档上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传文档（异步）
     */
    @PostMapping("/upload/async")
    public ResponseEntity<?> uploadDocumentAsync(@Valid @ModelAttribute DocumentRequest request, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            User currentUser = (User)httpRequest.getAttribute("currentUser");
            System.out.println("当前用户: " + currentUser);
            
            // 在主线程中读取文件内容，避免在异步线程中访问已销毁的MultipartFile对象
            byte[] fileBytes = request.getFile().getBytes();
            String originalFilename = request.getFile().getOriginalFilename();
            String contentType = request.getFile().getContentType();
            
            // 创建一个包装类来保存文件信息
            FileInfo fileInfo = new FileInfo(fileBytes, originalFilename, contentType);
            
            // 启动异步处理线程
            new Thread(() -> {
                try {
                    // 创建一个临时的MultipartFile对象
                    MultipartFile tempFile = new InMemoryMultipartFile(fileInfo.getBytes(), fileInfo.getOriginalFilename(), fileInfo.getContentType());
                    
                    // 更新request中的文件对象
                    request.setFile(tempFile);
                    
                    // 先上传文件到七牛云
                    String fileUrl = qiniuUploadService.uploadFile(request.getFile(), currentUser);

                    try {
                        // 然后并行处理文档内容用于知识库，并保存数据库记录
                        DocumentInfo documentInfo = ragService.processFileInParallel(request, currentUser, fileUrl);
                        log.info("异步文档上传成功: {}", documentInfo.getId());
                        // 这里可以添加通知逻辑，比如通过WebSocket通知前端
                    } catch (Exception e) {
                        log.error("异步文档处理失败", e);
                    }
                } catch (Exception e) {
                    log.error("异步文档上传到七牛云失败", e);
                }
            }).start();
            
            // 立即返回响应，表示已接收上传请求
            return ResponseEntity.ok("文件上传请求已接收，正在后台处理中...");
        } catch (Exception e) {
            log.error("文档上传请求处理失败", e);
            return ResponseEntity.badRequest().body("文档上传请求处理失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取文档列表
     */
    @GetMapping
    public ResponseEntity<PageResult<DocumentInfo>> getDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            User currentUser = (User) httpRequest.getAttribute("currentUser");

            PageResult<DocumentInfo> pageResult = ragService.getDocuments(currentUser, page, size);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("获取文档列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

        /**
     * 删除文档
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable String documentId, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            User currentUser = (User) httpRequest.getAttribute("currentUser");
            
            ragService.deleteDocument(documentId, currentUser);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("删除文档失败", e);
            return ResponseEntity.badRequest().body("删除文档失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载文档
     */
    @GetMapping("/{documentId}/download")
    public ResponseEntity<?> downloadDocument(@PathVariable String documentId, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            User currentUser = (User) httpRequest.getAttribute("currentUser");
            
            // 获取文档信息
            DocumentInfo documentInfo = ragService.getDocumentById(documentId, currentUser);
            
            if (documentInfo == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 从七牛云下载文件
            byte[] fileData = qiniuUploadService.downloadFile(documentInfo.getFileName());
            
            if (fileData == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 设置响应头
            String filename = documentInfo.getFileName();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", "application/octet-stream")
                    .body(fileData);
        } catch (Exception e) {
            log.error("下载文档失败", e);
            return ResponseEntity.badRequest().body("下载文档失败: " + e.getMessage());
        }
    }
}