package com.aliyun.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 线程池配置属性
 * <p>
 * 用于配置文件处理和文档解析的线程池参数
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
@Configuration
@ConfigurationProperties(prefix = "thread.pool")
public class ThreadPoolProperties {

    /**
     * 文件处理线程池配置
     */
    private PoolConfig fileProcess = new PoolConfig();

    /**
     * 文档解析线程池配置
     */
    private PoolConfig documentParse = new PoolConfig();

    /**
     * 向量处理线程池配置
     */
    private PoolConfig vectorProcess = new PoolConfig();

    /**
     * 通用异步任务线程池配置
     */
    private PoolConfig asyncTask = new PoolConfig();

    /**
     * 请求追踪线程池配置
     */
    private PoolConfig traceProcess = new PoolConfig();

    // Getters and Setters
    public PoolConfig getFileProcess() {
        return fileProcess;
    }

    public void setFileProcess(PoolConfig fileProcess) {
        this.fileProcess = fileProcess;
    }

    public PoolConfig getDocumentParse() {
        return documentParse;
    }

    public void setDocumentParse(PoolConfig documentParse) {
        this.documentParse = documentParse;
    }

    public PoolConfig getVectorProcess() {
        return vectorProcess;
    }

    public void setVectorProcess(PoolConfig vectorProcess) {
        this.vectorProcess = vectorProcess;
    }

    public PoolConfig getAsyncTask() {
        return asyncTask;
    }

    public void setAsyncTask(PoolConfig asyncTask) {
        this.asyncTask = asyncTask;
    }

    public PoolConfig getTraceProcess() {
        return traceProcess;
    }

    public void setTraceProcess(PoolConfig traceProcess) {
        this.traceProcess = traceProcess;
    }

    /**
     * 线程池配置内部类
     */
    public static class PoolConfig {
        /**
         * 核心线程数
         */
        private int corePoolSize = 5;

        /**
         * 最大线程数
         */
        private int maxPoolSize = 20;

        /**
         * 队列容量
         */
        private int queueCapacity = 100;

        /**
         * 线程空闲时间（秒）
         */
        private int keepAliveSeconds = 60;

        /**
         * 线程名前缀
         */
        private String threadNamePrefix = "task-";

        // Getters and Setters
        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public int getKeepAliveSeconds() {
            return keepAliveSeconds;
        }

        public void setKeepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }
}