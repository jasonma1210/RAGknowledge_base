package com.aliyun.rag.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 知识库质量评估结果模型
 * <p>
 * 用于存储和管理知识库质量评估的结果数据
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-10-12
 */
public class KnowledgeBaseQuality {

    private Long userId;
    
    private LocalDateTime assessmentTime;
    
    private int totalDocuments;
    
    private int totalVectors;
    
    private double averageDocumentLength;
    
    private Map<String, Integer> documentTypeDistribution;
    
    private double updateFrequency;
    
    private double coverageScore;
    
    private double consistencyScore;
    
    private double overallScore;
    
    private String[] improvementSuggestions;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getAssessmentTime() {
        return assessmentTime;
    }

    public void setAssessmentTime(LocalDateTime assessmentTime) {
        this.assessmentTime = assessmentTime;
    }

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(int totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public int getTotalVectors() {
        return totalVectors;
    }

    public void setTotalVectors(int totalVectors) {
        this.totalVectors = totalVectors;
    }

    public double getAverageDocumentLength() {
        return averageDocumentLength;
    }

    public void setAverageDocumentLength(double averageDocumentLength) {
        this.averageDocumentLength = averageDocumentLength;
    }

    public Map<String, Integer> getDocumentTypeDistribution() {
        return documentTypeDistribution;
    }

    public void setDocumentTypeDistribution(Map<String, Integer> documentTypeDistribution) {
        this.documentTypeDistribution = documentTypeDistribution;
    }

    public double getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(double updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public double getCoverageScore() {
        return coverageScore;
    }

    public void setCoverageScore(double coverageScore) {
        this.coverageScore = coverageScore;
    }

    public double getConsistencyScore() {
        return consistencyScore;
    }

    public void setConsistencyScore(double consistencyScore) {
        this.consistencyScore = consistencyScore;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public String[] getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(String[] improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }
}