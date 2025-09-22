package com.aliyun.rag.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控指标配置
 * <p>
 * 配置业务相关的监控指标，包括文档处理、用户活动、系统性能等
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
@Configuration
public class MetricsConfig {

    /**
     * 文档上传计数器
     */
    @Bean
    public Counter documentUploadCounter(MeterRegistry meterRegistry) {
        return Counter.builder("document.upload.count")
                .description("Total number of documents uploaded")
                .tag("type", "upload")
                .register(meterRegistry);
    }

    /**
     * 文档上传失败计数器
     */
    @Bean
    public Counter documentUploadFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("document.upload.failure.count")
                .description("Total number of failed document uploads")
                .tag("type", "failure")
                .register(meterRegistry);
    }

    /**
     * 文档删除计数器
     */
    @Bean
    public Counter documentDeleteCounter(MeterRegistry meterRegistry) {
        return Counter.builder("document.delete.count")
                .description("Total number of documents deleted")
                .tag("type", "delete")
                .register(meterRegistry);
    }

    /**
     * 用户注册计数器
     */
    @Bean
    public Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("user.registration.count")
                .description("Total number of user registrations")
                .tag("type", "registration")
                .register(meterRegistry);
    }

    /**
     * 用户登录计数器
     */
    @Bean
    public Counter userLoginCounter(MeterRegistry meterRegistry) {
        return Counter.builder("user.login.count")
                .description("Total number of user logins")
                .tag("type", "login")
                .register(meterRegistry);
    }

    /**
     * 用户登录失败计数器
     */
    @Bean
    public Counter userLoginFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("user.login.failure.count")
                .description("Total number of failed user logins")
                .tag("type", "failure")
                .register(meterRegistry);
    }

    /**
     * 搜索请求计数器
     */
    @Bean
    public Counter searchRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("search.request.count")
                .description("Total number of search requests")
                .tag("type", "search")
                .register(meterRegistry);
    }

    /**
     * AI问答请求计数器
     */
    @Bean
    public Counter aiChatCounter(MeterRegistry meterRegistry) {
        return Counter.builder("ai.chat.count")
                .description("Total number of AI chat requests")
                .tag("type", "chat")
                .register(meterRegistry);
    }

    /**
     * 文档处理时间计时器
     */
    @Bean
    public Timer documentProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("document.processing.time")
                .description("Time taken to process documents")
                .tag("operation", "processing")
                .register(meterRegistry);
    }

    /**
     * 向量检索时间计时器
     */
    @Bean
    public Timer vectorSearchTimer(MeterRegistry meterRegistry) {
        return Timer.builder("vector.search.time")
                .description("Time taken for vector searches")
                .tag("operation", "search")
                .register(meterRegistry);
    }

    /**
     * 文件上传时间计时器
     */
    @Bean
    public Timer fileUploadTimer(MeterRegistry meterRegistry) {
        return Timer.builder("file.upload.time")
                .description("Time taken to upload files")
                .tag("operation", "upload")
                .register(meterRegistry);
    }

    /**
     * 活跃用户数量gauge
     */
    @Bean
    public AtomicLong activeUsersGauge(MeterRegistry meterRegistry) {
        AtomicLong activeUsers = new AtomicLong(0);
        Gauge.builder("users.active.count", activeUsers, AtomicLong::doubleValue)
                .description("Number of currently active users")
                .tag("status", "active")
                .register(meterRegistry);
        return activeUsers;
    }

    /**
     * 系统存储使用量gauge
     */
    @Bean
    public AtomicLong storageUsageGauge(MeterRegistry meterRegistry) {
        AtomicLong storageUsage = new AtomicLong(0);
        Gauge.builder("system.storage.usage.bytes", storageUsage, AtomicLong::doubleValue)
                .description("Total storage usage in bytes")
                .tag("type", "storage")
                .register(meterRegistry);
        return storageUsage;
    }

    /**
     * 文档总数量gauge
     */
    @Bean
    public AtomicLong totalDocumentsGauge(MeterRegistry meterRegistry) {
        AtomicLong totalDocuments = new AtomicLong(0);
        Gauge.builder("documents.total.count", totalDocuments, AtomicLong::doubleValue)
                .description("Total number of documents in the system")
                .tag("type", "documents")
                .register(meterRegistry);
        return totalDocuments;
    }

    /**
     * 向量总数量gauge
     */
    @Bean
    public AtomicLong totalVectorsGauge(MeterRegistry meterRegistry) {
        AtomicLong totalVectors = new AtomicLong(0);
        Gauge.builder("vectors.total.count", totalVectors, AtomicLong::doubleValue)
                .description("Total number of vectors in the system")
                .tag("type", "vectors")
                .register(meterRegistry);
        return totalVectors;
    }
}