package com.aliyun.rag.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 链路追踪任务装饰器配置
 * <p>
 * 为异步任务提供链路追踪支持，确保MDC上下文在异步线程中正确传递
 * 支持分布式链路追踪和日志上下文的一致性
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-19
 */
@Configuration
public class TracingTaskDecoratorConfig {

    /**
     * 支持链路追踪的任务装饰器
     * 将父线程的MDC上下文传递给子线程
     */
    public static class TracingTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        }
    }

    /**
     * 通用的链路追踪任务装饰器Bean
     */
    @Bean
    public TaskDecorator tracingTaskDecorator() {
        return new TracingTaskDecorator();
    }
}