package com.aliyun.rag.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 内存中的MultipartFile实现
 * <p>
 * 用于在异步处理中创建MultipartFile对象，避免原始文件对象失效
 * 支持流式处理，优化内存使用
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
public class InMemoryMultipartFile implements MultipartFile {
    
    private final byte[] content;
    private final String name;
    private final String originalFilename;
    private final String contentType;
    
    public InMemoryMultipartFile(byte[] content, String originalFilename, String contentType) {
        this.content = content != null ? content : new byte[0];
        this.name = "file";
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    @Override
    public String getContentType() {
        return contentType;
    }
    
    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }
    
    @Override
    public long getSize() {
        return content.length;
    }
    
    @Override
    public byte[] getBytes() {
        return content.clone();
    }
    
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }
    
    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("transferTo is not supported for InMemoryMultipartFile");
    }
}