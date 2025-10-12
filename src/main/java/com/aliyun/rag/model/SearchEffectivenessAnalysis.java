package com.aliyun.rag.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 搜索效果分析结果模型
 * <p>
 * 用于存储和管理搜索效果分析的结果数据
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-10-12
 */
public class SearchEffectivenessAnalysis {

    private Long userId;
    
    private String query;
    
    private LocalDateTime analysisTime;
    
    private int totalSearches;
    
    private double averageResponseTime;
    
    private double successRate;
    
    private double averageResultCount;
    
    private double clickThroughRate;
    
    private double userSatisfactionScore;
    
    private Map<String, Integer> topSearchTerms;
    
    private double effectivenessScore;
    
    private String[] optimizationSuggestions;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public LocalDateTime getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(LocalDateTime analysisTime) {
        this.analysisTime = analysisTime;
    }

    public int getTotalSearches() {
        return totalSearches;
    }

    public void setTotalSearches(int totalSearches) {
        this.totalSearches = totalSearches;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getAverageResultCount() {
        return averageResultCount;
    }

    public void setAverageResultCount(double averageResultCount) {
        this.averageResultCount = averageResultCount;
    }

    public double getClickThroughRate() {
        return clickThroughRate;
    }

    public void setClickThroughRate(double clickThroughRate) {
        this.clickThroughRate = clickThroughRate;
    }

    public double getUserSatisfactionScore() {
        return userSatisfactionScore;
    }

    public void setUserSatisfactionScore(double userSatisfactionScore) {
        this.userSatisfactionScore = userSatisfactionScore;
    }

    public Map<String, Integer> getTopSearchTerms() {
        return topSearchTerms;
    }

    public void setTopSearchTerms(Map<String, Integer> topSearchTerms) {
        this.topSearchTerms = topSearchTerms;
    }

    public double getEffectivenessScore() {
        return effectivenessScore;
    }

    public void setEffectivenessScore(double effectivenessScore) {
        this.effectivenessScore = effectivenessScore;
    }

    public String[] getOptimizationSuggestions() {
        return optimizationSuggestions;
    }

    public void setOptimizationSuggestions(String[] optimizationSuggestions) {
        this.optimizationSuggestions = optimizationSuggestions;
    }
}