package com.aliyun.rag.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控指标服务
 * <p>
 * 提供业务指标统计和监控功能
 * 统一管理各种Counter、Timer、Gauge等监控指标
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

    // 计数器
    private final Counter documentUploadCounter;
    private final Counter documentUploadFailureCounter;
    private final Counter documentDeleteCounter;
    private final Counter userRegistrationCounter;
    private final Counter userLoginCounter;
    private final Counter userLoginFailureCounter;
    private final Counter searchRequestCounter;
    private final Counter aiChatCounter;
    private final Counter rateLimitCounter;
    private final Counter errorCounter;

    // 计时器
    private final Timer documentProcessingTimer;
    private final Timer vectorSearchTimer;
    private final Timer fileUploadTimer;
    private final Timer aiResponseTimer;
    private final Timer chunkingTimer;

    // Gauge指标
    private final AtomicLong activeUsersGauge;
    private final AtomicLong storageUsageGauge;
    private final AtomicLong totalDocumentsGauge;
    private final AtomicLong totalVectorsGauge;
    private final AtomicLong cacheHitRateGauge;

    public MetricsService(Counter documentUploadCounter, 
                         Counter documentUploadFailureCounter,
                         Counter documentDeleteCounter,
                         Counter userRegistrationCounter,
                         Counter userLoginCounter,
                         Counter userLoginFailureCounter,
                         Counter searchRequestCounter,
                         Counter aiChatCounter,
                         Counter rateLimitCounter,
                         Counter errorCounter,
                         Timer documentProcessingTimer,
                         Timer vectorSearchTimer,
                         Timer fileUploadTimer,
                         Timer aiResponseTimer,
                         Timer chunkingTimer,
                         AtomicLong activeUsersGauge,
                         AtomicLong storageUsageGauge,
                         AtomicLong totalDocumentsGauge,
                         AtomicLong totalVectorsGauge,
                         AtomicLong cacheHitRateGauge) {
        this.documentUploadCounter = documentUploadCounter;
        this.documentUploadFailureCounter = documentUploadFailureCounter;
        this.documentDeleteCounter = documentDeleteCounter;
        this.userRegistrationCounter = userRegistrationCounter;
        this.userLoginCounter = userLoginCounter;
        this.userLoginFailureCounter = userLoginFailureCounter;
        this.searchRequestCounter = searchRequestCounter;
        this.aiChatCounter = aiChatCounter;
        this.rateLimitCounter = rateLimitCounter;
        this.errorCounter = errorCounter;
        this.documentProcessingTimer = documentProcessingTimer;
        this.vectorSearchTimer = vectorSearchTimer;
        this.fileUploadTimer = fileUploadTimer;
        this.aiResponseTimer = aiResponseTimer;
        this.chunkingTimer = chunkingTimer;
        this.activeUsersGauge = activeUsersGauge;
        this.storageUsageGauge = storageUsageGauge;
        this.totalDocumentsGauge = totalDocumentsGauge;
        this.totalVectorsGauge = totalVectorsGauge;
        this.cacheHitRateGauge = cacheHitRateGauge;
    }

    /**
     * 记录文档上传成功
     */
    public void recordDocumentUpload(String userLevel) {
        documentUploadCounter.increment();
        log.debug("记录文档上传指标，用户等级: {}", userLevel);
    }

    /**
     * 记录文档上传失败
     */
    public void recordDocumentUploadFailure(String reason) {
        documentUploadFailureCounter.increment();
        log.debug("记录文档上传失败指标，原因: {}", reason);
    }

    /**
     * 记录文档删除
     */
    public void recordDocumentDelete() {
        documentDeleteCounter.increment();
        log.debug("记录文档删除指标");
    }

    /**
     * 记录用户注册
     */
    public void recordUserRegistration() {
        userRegistrationCounter.increment();
        log.debug("记录用户注册指标");
    }

    /**
     * 记录用户登录成功
     */
    public void recordUserLogin(String userLevel) {
        userLoginCounter.increment();
        log.debug("记录用户登录指标，用户等级: {}", userLevel);
    }

    /**
     * 记录用户登录失败
     */
    public void recordUserLoginFailure(String reason) {
        userLoginFailureCounter.increment();
        log.debug("记录用户登录失败指标，原因: {}", reason);
    }

    /**
     * 记录搜索请求
     */
    public void recordSearchRequest(String searchType) {
        searchRequestCounter.increment();
        log.debug("记录搜索请求指标，搜索类型: {}", searchType);
    }

    /**
     * 记录AI问答请求
     */
    public void recordAiChat() {
        aiChatCounter.increment();
        log.debug("记录AI问答指标");
    }

    /**
     * 记录文档处理时间
     */
    public Timer.Sample startDocumentProcessingTimer() {
        return Timer.start();
    }

    public void recordDocumentProcessingTime(Timer.Sample sample) {
        sample.stop(documentProcessingTimer);
        log.debug("记录文档处理时间指标");
    }

    /**
     * 记录向量检索时间
     */
    public Timer.Sample startVectorSearchTimer() {
        return Timer.start();
    }

    public void recordVectorSearchTime(Timer.Sample sample) {
        sample.stop(vectorSearchTimer);
        log.debug("记录向量检索时间指标");
    }

    /**
     * 记录文件上传时间
     */
    public Timer.Sample startFileUploadTimer() {
        return Timer.start();
    }

    public void recordFileUploadTime(Timer.Sample sample) {
        sample.stop(fileUploadTimer);
        log.debug("记录文件上传时间指标");
    }

    /**
     * 更新活跃用户数量
     */
    public void updateActiveUsers(long count) {
        activeUsersGauge.set(count);
        log.debug("更新活跃用户数量: {}", count);
    }

    /**
     * 更新存储使用量
     */
    public void updateStorageUsage(long bytes) {
        storageUsageGauge.set(bytes);
        log.debug("更新存储使用量: {} bytes", bytes);
    }

    /**
     * 更新文档总数
     */
    public void updateTotalDocuments(long count) {
        totalDocumentsGauge.set(count);
        log.debug("更新文档总数: {}", count);
    }

    /**
     * 更新向量总数
     */
    public void updateTotalVectors(long count) {
        totalVectorsGauge.set(count);
        log.debug("更新向量总数: {}", count);
    }

    /**
     * 增加活跃用户数量
     */
    public void incrementActiveUsers() {
        activeUsersGauge.incrementAndGet();
    }

    /**
     * 减少活跃用户数量
     */
    public void decrementActiveUsers() {
        activeUsersGauge.decrementAndGet();
    }

    /**
     * 增加存储使用量
     */
    public void addStorageUsage(long bytes) {
        storageUsageGauge.addAndGet(bytes);
    }

    /**
     * 减少存储使用量
     */
    public void subtractStorageUsage(long bytes) {
        storageUsageGauge.addAndGet(-bytes);
    }

    /**
     * 增加文档总数
     */
    public void incrementTotalDocuments() {
        totalDocumentsGauge.incrementAndGet();
    }

    /**
     * 减少文档总数
     */
    public void decrementTotalDocuments() {
        totalDocumentsGauge.decrementAndGet();
    }

    /**
     * 增加向量总数
     */
    public void addTotalVectors(long count) {
        totalVectorsGauge.addAndGet(count);
    }

    /**
     * 减少向量总数
     */
    public void subtractTotalVectors(long count) {
        totalVectorsGauge.addAndGet(-count);
    }

    /**
     * 记录限流触发
     */
    public void recordRateLimit(String endpoint, String clientId) {
        rateLimitCounter.increment();
        log.debug("记录限流指标: 端点={}, 客户端={}", endpoint, clientId);
    }

    /**
     * 记录错误
     */
    public void recordError(String errorType, String component) {
        errorCounter.increment();
        log.debug("记录错误指标: 类型={}, 组件={}", errorType, component);
    }

    /**
     * 记录AI响应时间
     */
    public Timer.Sample startAiResponseTimer() {
        return Timer.start();
    }

    public void recordAiResponseTime(Timer.Sample sample) {
        sample.stop(aiResponseTimer);
        log.debug("记录AI响应时间指标");
    }

    /**
     * 记录分块处理时间
     */
    public Timer.Sample startChunkingTimer() {
        return Timer.start();
    }

    public void recordChunkingTime(Timer.Sample sample) {
        sample.stop(chunkingTimer);
        log.debug("记录分块处理时间指标");
    }

    /**
     * 更新缓存命中率
     */
    public void updateCacheHitRate(double rate) {
        cacheHitRateGauge.set((long) (rate * 100)); // 转换为百分比
        log.debug("更新缓存命中率: {}%", rate * 100);
    }

    /**
     * 记录文档处理指标（带类型标签）
     */
    public void recordDocumentProcessingMetrics(String documentType, long processingTime, boolean success) {
        documentProcessingTimer.record(processingTime, TimeUnit.MILLISECONDS);
        if (success) {
            documentUploadCounter.increment();
        } else {
            documentUploadFailureCounter.increment();
        }
        log.debug("记录文档处理指标: 类型={}, 时间={}ms, 成功={}", documentType, processingTime, success);
    }

    /**
     * 记录向量搜索指标（带类型标签）
     */
    public void recordVectorSearchMetrics(String searchType, long latency) {
        vectorSearchTimer.record(latency, TimeUnit.MILLISECONDS);
        searchRequestCounter.increment();
        log.debug("记录向量搜索指标: 类型={}, 延迟={}ms", searchType, latency);
    }

    /**
     * 记录用户行为指标
     */
    public void recordUserBehavior(String action, String userLevel) {
        switch (action) {
            case "login" -> userLoginCounter.increment();
            case "register" -> userRegistrationCounter.increment();
            case "search" -> searchRequestCounter.increment();
            case "chat" -> aiChatCounter.increment();
        }
        log.debug("记录用户行为指标: 行为={}, 等级={}", action, userLevel);
    }

    /**
     * 获取系统健康指标
     */
    public java.util.Map<String, Object> getSystemHealthMetrics() {
        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        
        metrics.put("activeUsers", activeUsersGauge.get());
        metrics.put("totalDocuments", totalDocumentsGauge.get());
        metrics.put("totalVectors", totalVectorsGauge.get());
        metrics.put("storageUsageBytes", storageUsageGauge.get());
        metrics.put("cacheHitRate", cacheHitRateGauge.get());
        
        return metrics;
    }
}