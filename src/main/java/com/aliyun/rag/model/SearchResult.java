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
    

}