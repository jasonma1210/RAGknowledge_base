package com.aliyun.rag.model;

import dev.langchain4j.data.embedding.Embedding;

import java.util.List;

/**
 * 文档处理结果模型
 * <p>
 * 用于封装文档处理过程中的结果数据，包括文档信息、分块内容、向量嵌入等
 * 用于在事务操作和非事务操作之间传递数据
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
public class DocumentProcessResult {
    
    /**
     * 文档信息
     */
    private DocumentInfo documentInfo;
    
    /**
     * 文档分块数组
     */
    private String[] chunks;
    
    /**
     * 向量嵌入列表
     */
    private List<Embedding> embeddings;
    
    /**
     * 文件URL
     */
    private String fileUrl;

    // Constructor
    public DocumentProcessResult(DocumentInfo documentInfo, String[] chunks, List<Embedding> embeddings, String fileUrl) {
        this.documentInfo = documentInfo;
        this.chunks = chunks;
        this.embeddings = embeddings;
        this.fileUrl = fileUrl;
    }

    // Getters and Setters
    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }

    public void setDocumentInfo(DocumentInfo documentInfo) {
        this.documentInfo = documentInfo;
    }

    public String[] getChunks() {
        return chunks;
    }

    public void setChunks(String[] chunks) {
        this.chunks = chunks;
    }

    public List<Embedding> getEmbeddings() {
        return embeddings;
    }

    public void setEmbeddings(List<Embedding> embeddings) {
        this.embeddings = embeddings;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}