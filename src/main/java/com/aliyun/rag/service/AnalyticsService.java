package com.aliyun.rag.service;

import com.aliyun.rag.model.User;
import com.aliyun.rag.model.UserBehaviorAnalysis;
import com.aliyun.rag.model.KnowledgeBaseQuality;
import com.aliyun.rag.model.SearchEffectivenessAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能分析服务
 * <p>
 * 提供用户行为分析、知识库质量评估和搜索效果分析等智能分析功能
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-10-12
 */
@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    // 用户行为数据存储（实际项目中应使用数据库存储）
    private final Map<Long, UserBehaviorData> userBehaviorDataMap = new ConcurrentHashMap<>();

    // 知识库质量数据存储（实际项目中应使用数据库存储）
    private final Map<Long, KnowledgeBaseQualityData> knowledgeBaseQualityDataMap = new ConcurrentHashMap<>();

    // 搜索效果数据存储（实际项目中应使用数据库存储）
    private final Map<Long, SearchEffectivenessData> searchEffectivenessDataMap = new ConcurrentHashMap<>();

    /**
     * 用户行为分析
     * 
     * @param userId 用户ID
     * @return 用户行为分析结果
     */
    public UserBehaviorAnalysis analyzeUserBehavior(Long userId) {
        try {
            log.info("分析用户行为: 用户ID={}", userId);

            // 获取用户行为数据
            UserBehaviorData behaviorData = userBehaviorDataMap.getOrDefault(userId, new UserBehaviorData());

            // 构建分析结果
            UserBehaviorAnalysis analysis = new UserBehaviorAnalysis();
            analysis.setUserId(userId);
            analysis.setAnalysisTime(LocalDateTime.now());
            
            // 计算各种指标
            analysis.setTotalSearches(behaviorData.getTotalSearches());
            analysis.setTotalDocumentsUploaded(behaviorData.getTotalDocumentsUploaded());
            analysis.setTotalChatInteractions(behaviorData.getTotalChatInteractions());
            analysis.setFavoriteSearchTypes(behaviorData.getFavoriteSearchTypes());
            analysis.setActiveDays(behaviorData.getActiveDays());
            analysis.setAverageSearchTime(behaviorData.getAverageSearchTime());
            analysis.setSearchSuccessRate(behaviorData.getSearchSuccessRate());

            // 计算用户活跃度评分
            double activityScore = calculateActivityScore(behaviorData);
            analysis.setActivityScore(activityScore);

            // 生成个性化建议
            analysis.setPersonalizedSuggestions(generatePersonalizedSuggestions(behaviorData));

            log.info("用户行为分析完成: 用户ID={}", userId);
            return analysis;

        } catch (Exception e) {
            log.error("用户行为分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("用户行为分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 知识库质量评估
     * 
     * @param userId 用户ID
     * @return 知识库质量评估结果
     */
    public KnowledgeBaseQuality assessKnowledgeBase(Long userId) {
        try {
            log.info("评估知识库质量: 用户ID={}", userId);

            // 获取知识库质量数据
            KnowledgeBaseQualityData qualityData = knowledgeBaseQualityDataMap.getOrDefault(userId, new KnowledgeBaseQualityData());

            // 构建评估结果
            KnowledgeBaseQuality quality = new KnowledgeBaseQuality();
            quality.setUserId(userId);
            quality.setAssessmentTime(LocalDateTime.now());
            
            // 计算各种指标
            quality.setTotalDocuments(qualityData.getTotalDocuments());
            quality.setTotalVectors(qualityData.getTotalVectors());
            quality.setAverageDocumentLength(qualityData.getAverageDocumentLength());
            quality.setDocumentTypeDistribution(qualityData.getDocumentTypeDistribution());
            quality.setUpdateFrequency(qualityData.getUpdateFrequency());
            quality.setCoverageScore(qualityData.getCoverageScore());
            quality.setConsistencyScore(qualityData.getConsistencyScore());

            // 计算综合质量评分
            double overallScore = calculateOverallQualityScore(qualityData);
            quality.setOverallScore(overallScore);

            // 生成改进建议
            quality.setImprovementSuggestions(generateQualityImprovementSuggestions(qualityData));

            log.info("知识库质量评估完成: 用户ID={}", userId);
            return quality;

        } catch (Exception e) {
            log.error("知识库质量评估失败: {}", e.getMessage(), e);
            throw new RuntimeException("知识库质量评估失败: " + e.getMessage(), e);
        }
    }

    /**
     * 搜索效果分析
     * 
     * @param query 搜索查询
     * @param userId 用户ID
     * @return 搜索效果分析结果
     */
    public SearchEffectivenessAnalysis analyzeSearchEffectiveness(String query, Long userId) {
        try {
            log.info("分析搜索效果: 用户ID={}, 查询={}", userId, query);

            // 获取搜索效果数据
            SearchEffectivenessData searchData = searchEffectivenessDataMap.getOrDefault(userId, new SearchEffectivenessData());

            // 构建分析结果
            SearchEffectivenessAnalysis analysis = new SearchEffectivenessAnalysis();
            analysis.setUserId(userId);
            analysis.setQuery(query);
            analysis.setAnalysisTime(LocalDateTime.now());
            
            // 计算各种指标
            analysis.setTotalSearches(searchData.getTotalSearches());
            analysis.setAverageResponseTime(searchData.getAverageResponseTime());
            analysis.setSuccessRate(searchData.getSuccessRate());
            analysis.setAverageResultCount(searchData.getAverageResultCount());
            analysis.setClickThroughRate(searchData.getClickThroughRate());
            analysis.setUserSatisfactionScore(searchData.getUserSatisfactionScore());
            analysis.setTopSearchTerms(searchData.getTopSearchTerms());

            // 计算搜索效果评分
            double effectivenessScore = calculateSearchEffectivenessScore(searchData);
            analysis.setEffectivenessScore(effectivenessScore);

            // 生成优化建议
            analysis.setOptimizationSuggestions(generateSearchOptimizationSuggestions(searchData));

            log.info("搜索效果分析完成: 用户ID={}, 查询={}", userId, query);
            return analysis;

        } catch (Exception e) {
            log.error("搜索效果分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("搜索效果分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 记录用户搜索行为
     * 
     * @param userId 用户ID
     * @param searchType 搜索类型
     * @param success 是否成功
     * @param responseTime 响应时间
     */
    public void recordUserSearch(Long userId, String searchType, boolean success, long responseTime) {
        UserBehaviorData behaviorData = userBehaviorDataMap.computeIfAbsent(userId, k -> new UserBehaviorData());
        behaviorData.incrementTotalSearches();
        behaviorData.addSearchType(searchType);
        if (success) {
            behaviorData.incrementSuccessfulSearches();
        }
        behaviorData.addSearchTime(responseTime);
    }

    /**
     * 记录用户文档上传行为
     * 
     * @param userId 用户ID
     */
    public void recordDocumentUpload(Long userId) {
        UserBehaviorData behaviorData = userBehaviorDataMap.computeIfAbsent(userId, k -> new UserBehaviorData());
        behaviorData.incrementTotalDocumentsUploaded();
    }

    /**
     * 记录用户聊天交互行为
     * 
     * @param userId 用户ID
     */
    public void recordChatInteraction(Long userId) {
        UserBehaviorData behaviorData = userBehaviorDataMap.computeIfAbsent(userId, k -> new UserBehaviorData());
        behaviorData.incrementTotalChatInteractions();
    }

    /**
     * 记录知识库更新
     * 
     * @param userId 用户ID
     * @param documentCount 文档数量
     * @param vectorCount 向量数量
     */
    public void recordKnowledgeBaseUpdate(Long userId, int documentCount, int vectorCount) {
        KnowledgeBaseQualityData qualityData = knowledgeBaseQualityDataMap.computeIfAbsent(userId, k -> new KnowledgeBaseQualityData());
        qualityData.setTotalDocuments(documentCount);
        qualityData.setTotalVectors(vectorCount);
    }

    /**
     * 记录搜索效果
     * 
     * @param userId 用户ID
     * @param success 是否成功
     * @param responseTime 响应时间
     * @param resultCount 结果数量
     * @param clicked 是否点击
     * @param satisfaction 用户满意度
     */
    public void recordSearchEffectiveness(Long userId, boolean success, long responseTime, int resultCount, 
                                         boolean clicked, int satisfaction) {
        SearchEffectivenessData searchData = searchEffectivenessDataMap.computeIfAbsent(userId, k -> new SearchEffectivenessData());
        searchData.incrementTotalSearches();
        if (success) {
            searchData.incrementSuccessfulSearches();
        }
        searchData.addResponseTime(responseTime);
        searchData.addResultCount(resultCount);
        if (clicked) {
            searchData.incrementClicks();
        }
        searchData.addSatisfactionScore(satisfaction);
    }

    /**
     * 计算用户活跃度评分
     */
    private double calculateActivityScore(UserBehaviorData behaviorData) {
        // 基于搜索次数、文档上传和聊天交互计算活跃度评分
        double score = 0.0;
        score += behaviorData.getTotalSearches() * 0.3;
        score += behaviorData.getTotalDocumentsUploaded() * 0.4;
        score += behaviorData.getTotalChatInteractions() * 0.3;
        
        // 归一化到0-100范围
        return Math.min(100.0, score / 10.0);
    }

    /**
     * 生成个性化建议
     */
    private String[] generatePersonalizedSuggestions(UserBehaviorData behaviorData) {
        // 根据用户行为数据生成个性化建议
        if (behaviorData.getTotalSearches() < 10) {
            return new String[]{"建议多使用搜索功能来熟悉知识库内容"};
        } else if (behaviorData.getTotalDocumentsUploaded() < 5) {
            return new String[]{"建议上传更多文档来丰富知识库内容"};
        } else if (behaviorData.getSearchSuccessRate() < 0.7) {
            return new String[]{"建议优化搜索关键词以提高搜索成功率"};
        } else {
            return new String[]{"您是知识库的活跃用户，继续保持！"};
        }
    }

    /**
     * 计算综合质量评分
     */
    private double calculateOverallQualityScore(KnowledgeBaseQualityData qualityData) {
        // 基于覆盖率、一致性等指标计算综合质量评分
        double score = 0.0;
        score += qualityData.getCoverageScore() * 0.5;
        score += qualityData.getConsistencyScore() * 0.3;
        score += Math.min(100.0, qualityData.getUpdateFrequency() * 10) * 0.2;
        
        return Math.min(100.0, score);
    }

    /**
     * 生成质量改进建议
     */
    private String[] generateQualityImprovementSuggestions(KnowledgeBaseQualityData qualityData) {
        // 根据质量数据生成改进建议
        if (qualityData.getCoverageScore() < 70) {
            return new String[]{"建议增加更多相关领域的文档以提高覆盖率"};
        } else if (qualityData.getConsistencyScore() < 70) {
            return new String[]{"建议检查文档格式和内容一致性"};
        } else if (qualityData.getUpdateFrequency() < 1) {
            return new String[]{"建议定期更新知识库内容"};
        } else {
            return new String[]{"知识库质量良好，继续保持！"};
        }
    }

    /**
     * 计算搜索效果评分
     */
    private double calculateSearchEffectivenessScore(SearchEffectivenessData searchData) {
        // 基于成功率、响应时间、点击率等指标计算搜索效果评分
        double score = 0.0;
        score += searchData.getSuccessRate() * 40;
        score += Math.max(0, 100 - (searchData.getAverageResponseTime() / 100)) * 30;
        score += searchData.getClickThroughRate() * 20;
        score += searchData.getUserSatisfactionScore() * 10;
        
        return Math.min(100.0, score);
    }

    /**
     * 生成搜索优化建议
     */
    private String[] generateSearchOptimizationSuggestions(SearchEffectivenessData searchData) {
        // 根据搜索效果数据生成优化建议
        if (searchData.getSuccessRate() < 0.7) {
            return new String[]{"建议优化搜索算法以提高搜索成功率"};
        } else if (searchData.getAverageResponseTime() > 1000) {
            return new String[]{"建议优化向量数据库索引以提高搜索响应速度"};
        } else if (searchData.getClickThroughRate() < 0.3) {
            return new String[]{"建议优化搜索结果排序算法以提高点击率"};
        } else {
            return new String[]{"搜索效果良好，继续保持！"};
        }
    }

    /**
     * 用户行为数据类
     */
    private static class UserBehaviorData {
        private int totalSearches = 0;
        private int successfulSearches = 0;
        private int totalDocumentsUploaded = 0;
        private int totalChatInteractions = 0;
        private Map<String, Integer> searchTypes = new HashMap<>();
        private long totalSearchTime = 0;
        private int searchCount = 0;
        private int activeDays = 0;

        public synchronized void incrementTotalSearches() {
            totalSearches++;
        }

        public synchronized void incrementSuccessfulSearches() {
            successfulSearches++;
        }

        public synchronized void incrementTotalDocumentsUploaded() {
            totalDocumentsUploaded++;
        }

        public synchronized void incrementTotalChatInteractions() {
            totalChatInteractions++;
        }

        public synchronized void addSearchType(String searchType) {
            searchTypes.put(searchType, searchTypes.getOrDefault(searchType, 0) + 1);
        }

        public synchronized void addSearchTime(long searchTime) {
            totalSearchTime += searchTime;
            searchCount++;
        }

        public synchronized void incrementActiveDays() {
            activeDays++;
        }

        // Getters
        public int getTotalSearches() { return totalSearches; }
        public int getTotalDocumentsUploaded() { return totalDocumentsUploaded; }
        public int getTotalChatInteractions() { return totalChatInteractions; }
        public Map<String, Integer> getSearchTypes() { return new HashMap<>(searchTypes); }
        public int getActiveDays() { return activeDays; }

        public double getAverageSearchTime() {
            return searchCount > 0 ? (double) totalSearchTime / searchCount : 0;
        }

        public double getSearchSuccessRate() {
            return totalSearches > 0 ? (double) successfulSearches / totalSearches : 0;
        }

        public String[] getFavoriteSearchTypes() {
            return searchTypes.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .toArray(String[]::new);
        }
    }

    /**
     * 知识库质量数据类
     */
    private static class KnowledgeBaseQualityData {
        private int totalDocuments = 0;
        private int totalVectors = 0;
        private double averageDocumentLength = 0;
        private Map<String, Integer> documentTypeDistribution = new HashMap<>();
        private double updateFrequency = 0;
        private double coverageScore = 0;
        private double consistencyScore = 0;

        // Getters and Setters
        public int getTotalDocuments() { return totalDocuments; }
        public void setTotalDocuments(int totalDocuments) { this.totalDocuments = totalDocuments; }
        public int getTotalVectors() { return totalVectors; }
        public void setTotalVectors(int totalVectors) { this.totalVectors = totalVectors; }
        public double getAverageDocumentLength() { return averageDocumentLength; }
        public void setAverageDocumentLength(double averageDocumentLength) { this.averageDocumentLength = averageDocumentLength; }
        public Map<String, Integer> getDocumentTypeDistribution() { return new HashMap<>(documentTypeDistribution); }
        public void setDocumentTypeDistribution(Map<String, Integer> documentTypeDistribution) { this.documentTypeDistribution = new HashMap<>(documentTypeDistribution); }
        public double getUpdateFrequency() { return updateFrequency; }
        public void setUpdateFrequency(double updateFrequency) { this.updateFrequency = updateFrequency; }
        public double getCoverageScore() { return coverageScore; }
        public void setCoverageScore(double coverageScore) { this.coverageScore = coverageScore; }
        public double getConsistencyScore() { return consistencyScore; }
        public void setConsistencyScore(double consistencyScore) { this.consistencyScore = consistencyScore; }
    }

    /**
     * 搜索效果数据类
     */
    private static class SearchEffectivenessData {
        private int totalSearches = 0;
        private int successfulSearches = 0;
        private long totalResponseTime = 0;
        private int responseTimeCount = 0;
        private int totalResultCount = 0;
        private int resultCountCount = 0;
        private int clicks = 0;
        private int totalSatisfactionScore = 0;
        private int satisfactionScoreCount = 0;
        private Map<String, Integer> topSearchTerms = new HashMap<>();

        public synchronized void incrementTotalSearches() {
            totalSearches++;
        }

        public synchronized void incrementSuccessfulSearches() {
            successfulSearches++;
        }

        public synchronized void addResponseTime(long responseTime) {
            totalResponseTime += responseTime;
            responseTimeCount++;
        }

        public synchronized void addResultCount(int resultCount) {
            totalResultCount += resultCount;
            resultCountCount++;
        }

        public synchronized void incrementClicks() {
            clicks++;
        }

        public synchronized void addSatisfactionScore(int satisfactionScore) {
            totalSatisfactionScore += satisfactionScore;
            satisfactionScoreCount++;
        }

        public synchronized void addSearchTerm(String term) {
            topSearchTerms.put(term, topSearchTerms.getOrDefault(term, 0) + 1);
        }

        // Getters
        public int getTotalSearches() { return totalSearches; }
        public double getAverageResponseTime() { return responseTimeCount > 0 ? (double) totalResponseTime / responseTimeCount : 0; }
        public double getSuccessRate() { return totalSearches > 0 ? (double) successfulSearches / totalSearches : 0; }
        public double getAverageResultCount() { return resultCountCount > 0 ? (double) totalResultCount / resultCountCount : 0; }
        public double getClickThroughRate() { return totalSearches > 0 ? (double) clicks / totalSearches : 0; }
        public double getUserSatisfactionScore() { return satisfactionScoreCount > 0 ? (double) totalSatisfactionScore / satisfactionScoreCount : 0; }
        public Map<String, Integer> getTopSearchTerms() { return new HashMap<>(topSearchTerms); }
    }
}