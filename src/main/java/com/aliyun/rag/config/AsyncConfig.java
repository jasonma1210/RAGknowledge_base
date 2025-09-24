package com.aliyun.rag.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 * <p>
 * 配置专用的线程池以替代默认的ForkJoinPool，提供更好的资源控制和监控能力
 * 包含文件处理、文档解析等异步任务的线程池配置
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    private final ThreadPoolProperties threadPoolProperties;

    public AsyncConfig(ThreadPoolProperties threadPoolProperties) {
        this.threadPoolProperties = threadPoolProperties;
    }

    /**
     * 文件处理专用线程池
     * <p>
     * 用于处理文档上传、解析、向量化等文件相关的异步任务
     * 避免使用默认的ForkJoinPool，提供更好的资源控制
     * </p>
     *
     * @return 文件处理线程池执行器
     */
    @Bean(name = "fileProcessExecutor")
    public ThreadPoolTaskExecutor fileProcessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：CPU核心数
        executor.setCorePoolSize(threadPoolProperties.getFileProcess().getCorePoolSize());
        
        // 最大线程数：核心线程数的2倍
        executor.setMaxPoolSize(threadPoolProperties.getFileProcess().getMaxPoolSize());
        
        // 队列容量：100个任务
        executor.setQueueCapacity(threadPoolProperties.getFileProcess().getQueueCapacity());
        
        // 线程名前缀
        executor.setThreadNamePrefix(threadPoolProperties.getFileProcess().getThreadNamePrefix());
        
        // 空闲线程存活时间
        executor.setKeepAliveSeconds(threadPoolProperties.getFileProcess().getKeepAliveSeconds());
        
        // 拒绝策略：由调用者线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待任务完成后关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("文件处理线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
                
        return executor;
    }

    /**
     * 文档解析专用线程池
     * <p>
     * 用于处理PDF、DOCX等文档格式的解析任务
     * 由于文档解析可能比较耗时，使用独立的线程池避免影响其他任务
     * </p>
     *
     * @return 文档解析线程池执行器
     */
    @Bean(name = "documentParseExecutor")
    public ThreadPoolTaskExecutor documentParseExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(threadPoolProperties.getDocumentParse().getCorePoolSize());
        executor.setMaxPoolSize(threadPoolProperties.getDocumentParse().getMaxPoolSize());
        executor.setQueueCapacity(threadPoolProperties.getDocumentParse().getQueueCapacity());
        executor.setThreadNamePrefix(threadPoolProperties.getDocumentParse().getThreadNamePrefix());
        executor.setKeepAliveSeconds(threadPoolProperties.getDocumentParse().getKeepAliveSeconds());
        
        // 文档解析失败时丢弃任务并记录日志
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("文档解析线程池队列已满，丢弃任务: {}", r.toString());
        });
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("文档解析线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
                
        return executor;
    }

    /**
     * 向量处理专用线程池
     * <p>
     * 用于处理向量生成、存储等AI相关的异步任务
     * 由于AI服务调用可能有网络延迟，使用独立线程池
     * </p>
     *
     * @return 向量处理线程池执行器
     */
    @Bean(name = "vectorProcessExecutor")
    public ThreadPoolTaskExecutor vectorProcessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(threadPoolProperties.getVectorProcess().getCorePoolSize());
        executor.setMaxPoolSize(threadPoolProperties.getVectorProcess().getMaxPoolSize());
        executor.setQueueCapacity(threadPoolProperties.getVectorProcess().getQueueCapacity());
        executor.setThreadNamePrefix(threadPoolProperties.getVectorProcess().getThreadNamePrefix());
        executor.setKeepAliveSeconds(threadPoolProperties.getVectorProcess().getKeepAliveSeconds());
        
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(90);
        executor.initialize();
        
        log.info("向量处理线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
                
        return executor;
    }

    /**
     * 通用异步任务线程池
     * <p>
     * 用于处理其他类型的异步任务，如通知发送、日志记录等
     * </p>
     *
     * @return 通用异步任务线程池执行器
     */
    @Bean(name = "asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(threadPoolProperties.getAsyncTask().getCorePoolSize());
        executor.setMaxPoolSize(threadPoolProperties.getAsyncTask().getMaxPoolSize());
        executor.setQueueCapacity(threadPoolProperties.getAsyncTask().getQueueCapacity());
        executor.setThreadNamePrefix(threadPoolProperties.getAsyncTask().getThreadNamePrefix());
        executor.setKeepAliveSeconds(threadPoolProperties.getAsyncTask().getKeepAliveSeconds());
        
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("通用异步任务线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
                
        return executor;
    }


    /**
     * 通用Trace任务线程池
     * <p>
     * 用于处理其他类型的异步任务，Trace
     * </p>
     *
     * @return 通用Trace任务线程池执行器
     */
    @Bean(name = "traceExecutor")
    public Executor traceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(threadPoolProperties.getAsyncTask().getCorePoolSize());
        executor.setMaxPoolSize(threadPoolProperties.getAsyncTask().getMaxPoolSize());
        executor.setQueueCapacity(threadPoolProperties.getAsyncTask().getQueueCapacity());
        executor.setThreadNamePrefix(threadPoolProperties.getAsyncTask().getThreadNamePrefix());
        executor.setKeepAliveSeconds(threadPoolProperties.getAsyncTask().getKeepAliveSeconds());

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("通用Trace任务线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
    
    /**
     * 支持链路追踪的Trace任务线程池
     * <p>
     * 使用与traceExecutor相同的配置，但添加了链路追踪装饰器
     * </p>
     *
     * @return 支持链路追踪的Trace任务线程池执行器
     */
    @Bean(name = "tracingTraceExecutor")
    public ThreadPoolTaskExecutor tracingTraceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 使用traceProcess配置
        executor.setCorePoolSize(threadPoolProperties.getTraceProcess().getCorePoolSize());
        executor.setMaxPoolSize(threadPoolProperties.getTraceProcess().getMaxPoolSize());
        executor.setQueueCapacity(threadPoolProperties.getTraceProcess().getQueueCapacity());
        executor.setThreadNamePrefix(threadPoolProperties.getTraceProcess().getThreadNamePrefix());
        executor.setKeepAliveSeconds(threadPoolProperties.getTraceProcess().getKeepAliveSeconds());
        
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // 设置任务装饰器，支持链路追踪
        executor.setTaskDecorator(new TracingTaskDecoratorConfig.TracingTaskDecorator());
        
        executor.initialize();
        return executor;
    }
}