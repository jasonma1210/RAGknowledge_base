package com.aliyun.rag.controller;

import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.DocumentRequest;
import com.aliyun.rag.service.RAGService;
import com.aliyun.rag.service.QiniuUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final RAGService ragService;
    private final QiniuUploadService qiniuUploadService;

    public DocumentController(RAGService ragService, QiniuUploadService qiniuUploadService) {
        this.ragService = ragService;
        this.qiniuUploadService = qiniuUploadService;
    }

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@Valid @ModelAttribute DocumentRequest request) {
        try {
            // 先上传文件到七牛云
            String fileUrl = qiniuUploadService.uploadFile(request.getFile());
            
            // 然后处理文档内容用于知识库
            DocumentInfo documentInfo = ragService.uploadDocument(request);
            
            // 添加文件URL到响应中
            documentInfo.setFileName(fileUrl);
            
            return ResponseEntity.ok(documentInfo);
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return ResponseEntity.badRequest().body("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有文档
     */
    @GetMapping
    public ResponseEntity<List<DocumentInfo>> getAllDocuments() {
        try {
            List<DocumentInfo> documents = ragService.getAllDocuments();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("获取文档列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable String documentId) {
        try {
            ragService.deleteDocument(documentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("删除文档失败", e);
            return ResponseEntity.badRequest().body("删除文档失败: " + e.getMessage());
        }
    }
}