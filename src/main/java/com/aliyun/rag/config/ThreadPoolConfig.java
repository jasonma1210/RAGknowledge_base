//package com.aliyun.rag.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.ThreadPoolExecutor;
//
///**
// * 线程池配置类
// * <p>
// * 配置用于文件处理的线程池
// * </p>
// *
// * @author Jason Ma
// * @version 1.0.0
// * @since 2025-09-15
// */
//@Configuration
//@EnableAsync
//public class ThreadPoolConfig {
//
//    /**
//     * 文件处理线程池
//     *
//     * @return Executor
//     */
//    @Bean("fileProcessExecutor")
//    public Executor fileProcessExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        // 核心线程数
//        executor.setCorePoolSize(5);
//        // 最大线程数
//        executor.setMaxPoolSize(10);
//        // 队列容量
//        executor.setQueueCapacity(100);
//        // 线程前缀
//        executor.setThreadNamePrefix("file-process-");
//        // 线程池关闭时等待所有任务完成
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//        // 等待时间
//        executor.setAwaitTerminationSeconds(60);
//        // 拒绝策略
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        executor.initialize();
//        return executor;
//    }
//
//    /**
//     * 文档解析线程池
//     *
//     * @return ThreadPoolTaskExecutor
//     */
//    @Bean("documentParseExecutor")
//    public ThreadPoolTaskExecutor documentParseExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        // 核心线程数
//        executor.setCorePoolSize(3);
//        // 最大线程数
//        executor.setMaxPoolSize(6);
//        // 队列容量
//        executor.setQueueCapacity(50);
//        // 线程前缀
//        executor.setThreadNamePrefix("doc-parse-");
//        // 线程池关闭时等待所有任务完成
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//        // 等待时间
//        executor.setAwaitTerminationSeconds(60);
//        // 拒绝策略
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        executor.initialize();
//        return executor;
//    }
//}