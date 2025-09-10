package com.aliyun.rag.model;

import lombok.Data;

/**
 * 搜索结果模型
 * <p>
 * 用于封装知识库搜索的结果数据
 * 包含匹配的文档内容、相似度分数和元数据信息
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
@Data
public class SearchResult {
    
    private String documentId;
    
    private String title;
    
    private String content;
    
    private Double score;
    
    private String source;
    
    private Integer position;
    
    public String getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Integer getPosition() {
        return position;
    }
    
    public void setPosition(Integer position) {
        this.position = position;
    }
}