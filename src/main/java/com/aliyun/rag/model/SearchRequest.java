package com.aliyun.rag.model;

import jakarta.validation.constraints.NotNull;

/**
 * 搜索请求模型
 * <p>
 * 用于接收知识库搜索请求的参数模型
 * 支持多种搜索类型和查询参数配置
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
public class SearchRequest {
    
    @NotNull(message = "搜索内容不能为空")
    private String query;
    
    private SearchType searchType = SearchType.SEMANTIC;
    
    private Integer maxResults = 10;
    
    private Double minScore = 0.7;
    
    public enum SearchType {
        SEMANTIC,    // 语义搜索
        KEYWORD,     // 关键词搜索
        HYBRID       // 混合搜索
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }
    
    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }
    
    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }
    
    public String getQuery() {
        return query;
    }
    
    public SearchType getSearchType() {
        return searchType;
    }
    
    public Integer getMaxResults() {
        return maxResults;
    }
    
    public Double getMinScore() {
        return minScore;
    }
}