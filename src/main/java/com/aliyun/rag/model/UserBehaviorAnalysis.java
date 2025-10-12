package com.aliyun.rag.model;

import java.time.LocalDateTime;

/**
 * 用户行为分析结果模型
 * <p>
 * 用于存储和管理用户行为分析的结果数据
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-10-12
 */
public class UserBehaviorAnalysis {

    private Long userId;
    
    private LocalDateTime analysisTime;
    
    private int totalSearches;
    
    private int totalDocumentsUploaded;
    
    private int totalChatInteractions;
    
    private String[] favoriteSearchTypes;
    
    private int activeDays;
    
    private double averageSearchTime;
    
    private double searchSuccessRate;
    
    private double activityScore;
    
    private String[] personalizedSuggestions;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public int getTotalDocumentsUploaded() {
        return totalDocumentsUploaded;
    }

    public void setTotalDocumentsUploaded(int totalDocumentsUploaded) {
        this.totalDocumentsUploaded = totalDocumentsUploaded;
    }

    public int getTotalChatInteractions() {
        return totalChatInteractions;
    }

    public void setTotalChatInteractions(int totalChatInteractions) {
        this.totalChatInteractions = totalChatInteractions;
    }

    public String[] getFavoriteSearchTypes() {
        return favoriteSearchTypes;
    }

    public void setFavoriteSearchTypes(String[] favoriteSearchTypes) {
        this.favoriteSearchTypes = favoriteSearchTypes;
    }

    public int getActiveDays() {
        return activeDays;
    }

    public void setActiveDays(int activeDays) {
        this.activeDays = activeDays;
    }

    public double getAverageSearchTime() {
        return averageSearchTime;
    }

    public void setAverageSearchTime(double averageSearchTime) {
        this.averageSearchTime = averageSearchTime;
    }

    public double getSearchSuccessRate() {
        return searchSuccessRate;
    }

    public void setSearchSuccessRate(double searchSuccessRate) {
        this.searchSuccessRate = searchSuccessRate;
    }

    public double getActivityScore() {
        return activityScore;
    }

    public void setActivityScore(double activityScore) {
        this.activityScore = activityScore;
    }

    public String[] getPersonalizedSuggestions() {
        return personalizedSuggestions;
    }

    public void setPersonalizedSuggestions(String[] personalizedSuggestions) {
        this.personalizedSuggestions = personalizedSuggestions;
    }
}