package com.aliyun.rag.controller;

import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.DocumentRequest;
import com.aliyun.rag.service.RAGService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentControllerTest {

    @Mock
    private RAGService ragService;

    @InjectMocks
    private DocumentController documentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadDocument_Success() {
        // 准备测试数据
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "This is a test file content".getBytes()
        );
        
        DocumentRequest request = new DocumentRequest();
        request.setFile(mockFile);
        request.setTitle("Test Document");
        request.setDescription("Test document description");
        request.setTags("test,document");

        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo.setId("test-id");
        documentInfo.setTitle("Test Document");
        documentInfo.setDescription("Test document description");
        documentInfo.setTags("test,document");

        // 设置mock行为
        when(ragService.uploadDocument(any(DocumentRequest.class))).thenReturn(documentInfo);

        // 执行测试
        ResponseEntity<?> response = documentController.uploadDocument(request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof DocumentInfo);
        
        DocumentInfo result = (DocumentInfo) response.getBody();
        assertEquals("test-id", result.getId());
        assertEquals("Test Document", result.getTitle());
        
        // 验证mock方法被调用
        verify(ragService, times(1)).uploadDocument(any(DocumentRequest.class));
    }

    @Test
    void uploadDocument_Failure() {
        // 准备测试数据
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "This is a test file content".getBytes()
        );
        
        DocumentRequest request = new DocumentRequest();
        request.setFile(mockFile);

        // 设置mock行为，模拟异常
        when(ragService.uploadDocument(any(DocumentRequest.class)))
            .thenThrow(new RuntimeException("Upload failed"));

        // 执行测试
        ResponseEntity<?> response = documentController.uploadDocument(request);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof String);
        assertEquals("文档上传失败: Upload failed", response.getBody());
        
        // 验证mock方法被调用
        verify(ragService, times(1)).uploadDocument(any(DocumentRequest.class));
    }

    @Test
    void getAllDocuments_Success() {
        // 准备测试数据
        List<DocumentInfo> documents = new ArrayList<>();
        DocumentInfo doc1 = new DocumentInfo();
        doc1.setId("doc1-id");
        doc1.setTitle("Document 1");
        documents.add(doc1);
        
        DocumentInfo doc2 = new DocumentInfo();
        doc2.setId("doc2-id");
        doc2.setTitle("Document 2");
        documents.add(doc2);

        // 设置mock行为
        when(ragService.getAllDocuments()).thenReturn(documents);

        // 执行测试
        ResponseEntity<List<DocumentInfo>> response = documentController.getAllDocuments();

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Document 1", response.getBody().get(0).getTitle());
        assertEquals("Document 2", response.getBody().get(1).getTitle());
        
        // 验证mock方法被调用
        verify(ragService, times(1)).getAllDocuments();
    }

    @Test
    void getAllDocuments_Failure() {
        // 设置mock行为，模拟异常
        when(ragService.getAllDocuments()).thenThrow(new RuntimeException("Database error"));

        // 执行测试
        ResponseEntity<List<DocumentInfo>> response = documentController.getAllDocuments();

        // 验证结果
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        
        // 验证mock方法被调用
        verify(ragService, times(1)).getAllDocuments();
    }

    @Test
    void deleteDocument_Success() {
        // 准备测试数据
        String documentId = "test-document-id";

        // 执行测试
        ResponseEntity<?> response = documentController.deleteDocument(documentId);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        
        // 验证mock方法被调用
        verify(ragService, times(1)).deleteDocument(documentId);
    }

    @Test
    void deleteDocument_Failure() {
        // 准备测试数据
        String documentId = "test-document-id";

        // 设置mock行为，模拟异常
        doThrow(new RuntimeException("Delete failed")).when(ragService).deleteDocument(documentId);

        // 执行测试
        ResponseEntity<?> response = documentController.deleteDocument(documentId);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("删除文档失败: Delete failed", response.getBody());
        
        // 验证mock方法被调用
        verify(ragService, times(1)).deleteDocument(documentId);
    }
}