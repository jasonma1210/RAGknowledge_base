package com.aliyun.rag.controller;

import com.aliyun.rag.exception.BusinessException;
import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.DocumentRequest;
import com.aliyun.rag.model.ErrorCode;
import com.aliyun.rag.model.FileInfo;
import com.aliyun.rag.model.PageResult;
import com.aliyun.rag.model.User;
import com.aliyun.rag.model.R;
import com.aliyun.rag.model.dto.UserDTO;
import com.aliyun.rag.service.RAGService;
import com.aliyun.rag.service.QiniuUploadService;
import com.aliyun.rag.service.AuthService;
import com.aliyun.rag.util.InMemoryMultipartFile;
import com.aliyun.rag.util.StreamFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 文档管理控制器
 * <p>
 * 提供文档上传、下载、删除、查询等功能
 * 优化了线程池使用和内存管理，提升高并发场景下的性能
 * </p>
 * 
 * @author Jason Ma
 * @version 2.0.0
 * @since 2025-01-18
 */
@RestController
@RequestMapping("/documents")
public class DocumentController {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    
    /**
     * 文件大小限制：100MB
     */
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    private final RAGService ragService;
    private final QiniuUploadService qiniuUploadService;
    private final AuthService authService;
    private final StreamFileProcessor streamFileProcessor;
    private final ThreadPoolTaskExecutor fileProcessExecutor;
    private final ThreadPoolTaskExecutor documentParseExecutor;

    public DocumentController(RAGService ragService, 
                             QiniuUploadService qiniuUploadService, 
                             AuthService authService,
                             StreamFileProcessor streamFileProcessor,
                             @Qualifier("fileProcessExecutor") ThreadPoolTaskExecutor fileProcessExecutor,
                             @Qualifier("documentParseExecutor") ThreadPoolTaskExecutor documentParseExecutor) {
        this.ragService = ragService;
        this.qiniuUploadService = qiniuUploadService;
        this.authService = authService;
        this.streamFileProcessor = streamFileProcessor;
        this.fileProcessExecutor = fileProcessExecutor;
        this.documentParseExecutor = documentParseExecutor;
    }

    /**
     * 上传文档（同步）
     * <p>
     * 同步处理文档上传，包含文件大小检查、格式验证等安全机制
     * 支持回滚机制，确保数据一致性
     * </p>
     */
    @PostMapping("/upload")
    public ResponseEntity<R<DocumentInfo>> uploadDocument(@Valid @ModelAttribute DocumentRequest request, HttpServletRequest httpRequest) {
        // 获取当前用户
        User currentUser = (User)httpRequest.getAttribute("currentUser");
        
        // 转换为UserDTO以避免敏感信息泄露
        UserDTO currentUserDTO = UserDTO.fromUser(currentUser);
        
        // 创建一个User实体用于兼容现有服务方法（仅包含必要信息）
        User user = new User();
        user.setId(currentUserDTO.getId());
        user.setUsername(currentUserDTO.getUsername());
        user.setLevel(currentUserDTO.getLevel());
        user.setStorageQuota(currentUserDTO.getStorageQuota());
        user.setUsedStorage(currentUserDTO.getUsedStorage());
        
        MultipartFile file = request.getFile();
        
        log.info("用户 {} 开始上传文档: {}", user.getUsername(), file.getOriginalFilename());
        
        // 使用流式文件处理器进行文件验证和处理
        streamFileProcessor.validateFileSize(file, MAX_FILE_SIZE);
        
        // 验证文件类型
        String[] allowedTypes = {"pdf", "docx", "txt", "md", "epub"};
        streamFileProcessor.validateFileType(file, allowedTypes);
        
        // 先上传文件到七牛云
        String fileUrl = qiniuUploadService.uploadFile(file, user);

        try {
            // 然后处理文档内容用于知识库，并保存数据库记录
            DocumentInfo documentInfo = ragService.uploadDocument(request, user, fileUrl);
            
            log.info("用户 {} 文档上传成功: {} -> {}", user.getUsername(), 
                    file.getOriginalFilename(), documentInfo.getTitle());
            return ResponseEntity.ok(R.success(documentInfo));
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
    }
    
    /**
     * 上传文档（异步）
     * <p>
     * 使用专用线程池进行异步文档处理，提升用户体验
     * 支持大文件处理，避免内存溢出
     * </p>
     */
    @PostMapping("/upload/async")
    public ResponseEntity<R<Map<String, Object>>> uploadDocumentAsync(@Valid @ModelAttribute DocumentRequest request, HttpServletRequest httpRequest) throws IOException {
        // 获取当前用户
        User currentUser = (User)httpRequest.getAttribute("currentUser");
        
        // 转换为UserDTO以避免敏感信息泄露
        UserDTO currentUserDTO = UserDTO.fromUser(currentUser);
        
        // 创建一个User实体用于兼容现有服务方法（仅包含必要信息）
        User user = new User();
        user.setId(currentUserDTO.getId());
        user.setUsername(currentUserDTO.getUsername());
        user.setLevel(currentUserDTO.getLevel());
        user.setStorageQuota(currentUserDTO.getStorageQuota());
        user.setUsedStorage(currentUserDTO.getUsedStorage());
        
        MultipartFile file = request.getFile();
        
        log.info("用户 {} 开始异步上传文档: {}", user.getUsername(), file.getOriginalFilename());
        
        // 使用流式文件处理器进行文件验证和处理
        streamFileProcessor.validateFileSize(file, MAX_FILE_SIZE);
        
        // 验证文件类型
        String[] allowedTypes = {"pdf", "docx", "txt", "md", "epub"};
        streamFileProcessor.validateFileType(file, allowedTypes);
        
        // 使用流式处理读取文件内容，优化内存使用
        byte[] fileBytes;
        try {
            fileBytes = streamFileProcessor.processFileStream(file);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_PROCESSING_FAILED, "文件读取失败: " + e.getMessage());
        }
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        
        // 创建一个包装类来保存文件信息
        FileInfo fileInfo = new FileInfo(fileBytes, originalFilename, contentType);
        
        // 使用专用文件处理线程池执行异步处理
        CompletableFuture.runAsync(() -> {
            try {
                // 创建一个临时的MultipartFile对象
                MultipartFile tempFile = new InMemoryMultipartFile(
                    fileInfo.getBytes(), fileInfo.getOriginalFilename(), fileInfo.getContentType());
                
                // 更新request中的文件对象
                request.setFile(tempFile);
                
                // 先上传文件到七牛云
                String fileUrl = qiniuUploadService.uploadFile(tempFile, user);

                try {
                    // 然后并行处理文档内容用于知识库，并保存数据库记录
                    DocumentInfo documentInfo = ragService.processFileInParallel(request, user, fileUrl);
                    log.info("用户 {} 异步文档上传成功: {} -> {}", user.getUsername(), 
                            originalFilename, documentInfo.getTitle());
                    // 这里可以添加通知逻辑，比如通过WebSocket通知前端
                } catch (Exception e) {
                    log.error("用户 {} 异步文档处理失败: {}", user.getUsername(), originalFilename, e);
                    // 可以考虑添加重试机制或通知用户
                }
            } catch (Exception e) {
                log.error("用户 {} 异步文档上传到七牛云失败: {}", user.getUsername(), originalFilename, e);
            }
        }, fileProcessExecutor);
        
        // 立即返回响应，表示已接收上传请求
        Map<String, Object> response = Map.of(
            "message", "文件上传请求已接收，正在后台处理中...",
            "filename", originalFilename,
            "status", "processing"
        );
        return ResponseEntity.ok(R.success(response));
    }

    /**
     * 分页获取文档列表
     */
    @GetMapping
    public ResponseEntity<R<PageResult<DocumentInfo>>> getDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest httpRequest) {
        // 获取当前用户
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        
        // 转换为UserDTO以避免敏感信息泄露
        UserDTO currentUserDTO = UserDTO.fromUser(currentUser);
        
        // 创建一个User实体用于兼容现有服务方法（仅包含必要信息）
        User user = new User();
        user.setId(currentUserDTO.getId());
        user.setUsername(currentUserDTO.getUsername());

        // 如果提供了关键词，则进行模糊搜索
        PageResult<DocumentInfo> pageResult;
        if (keyword != null && !keyword.trim().isEmpty()) {
            pageResult = ragService.searchDocuments(user, keyword, page, size);
        } else {
            pageResult = ragService.getDocuments(user, page, size);
        }
        
        return ResponseEntity.ok(R.success(pageResult));
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<R<String>> deleteDocument(@PathVariable String documentId, HttpServletRequest httpRequest) {
        // 获取当前用户
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        
        // 转换为UserDTO以避免敏感信息泄露
        UserDTO currentUserDTO = UserDTO.fromUser(currentUser);
        
        // 创建一个User实体用于兼容现有服务方法（仅包含必要信息）
        User user = new User();
        user.setId(currentUserDTO.getId());
        user.setUsername(currentUserDTO.getUsername());
        
        ragService.deleteDocument(documentId, user);
        return ResponseEntity.ok(R.success("文档删除成功"));
    }
    
    /**
     * 下载文档
     */
    @GetMapping("/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String documentId, HttpServletRequest httpRequest) {
        // 获取当前用户
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        
        // 转换为UserDTO以避免敏感信息泄露
        UserDTO currentUserDTO = UserDTO.fromUser(currentUser);
        
        // 创建一个User实体用于兼容现有服务方法（仅包含必要信息）
        User user = new User();
        user.setId(currentUserDTO.getId());
        user.setUsername(currentUserDTO.getUsername());
        
        // 获取文档信息
        DocumentInfo documentInfo = ragService.getDocumentById(documentId, user);
        
        if (documentInfo == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 从七牛云下载文件
        byte[] fileData = qiniuUploadService.downloadFile(documentInfo.getFileName());
        
        if (fileData == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        
        // 设置响应头
        String filename = documentInfo.getFileName();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "application/octet-stream")
                .body(fileData);
    }
}