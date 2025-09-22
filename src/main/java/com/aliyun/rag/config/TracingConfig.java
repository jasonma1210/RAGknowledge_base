package com.aliyun.rag.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 链路追踪配置
 * <p>
 * 配置分布式链路追踪，为每个请求生成唯一的traceId
 * 支持链路追踪ID的生成、传递和清理
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-19
 */
@Configuration
public class TracingConfig {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    @Bean
    public TracingFilter tracingFilter() {
        return new TracingFilter();
    }

    /**
     * 链路追踪过滤器
     * 为每个HTTP请求生成或传递traceId
     */
    public static class TracingFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            try {
                // 从请求头获取traceId，如果没有则生成新的
                String traceId = request.getHeader(TRACE_ID_HEADER);
                if (traceId == null || traceId.trim().isEmpty()) {
                    traceId = generateTraceId();
                }

                // 设置到MDC中，用于日志输出
                MDC.put(TRACE_ID_MDC_KEY, traceId);

                // 将traceId添加到响应头中
                response.setHeader(TRACE_ID_HEADER, traceId);

                // 继续执行过滤器链
                filterChain.doFilter(request, response);
            } finally {
                // 清理MDC，避免内存泄漏
                MDC.clear();
            }
        }

        /**
         * 生成唯一的追踪ID
         * 
         * @return 追踪ID
         */
        private String generateTraceId() {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * 获取当前请求的traceId
     * 
     * @return traceId
     */
    public static String getCurrentTraceId() {
        return MDC.get(TRACE_ID_MDC_KEY);
    }

    /**
     * 设置traceId到MDC
     * 
     * @param traceId 追踪ID
     */
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.trim().isEmpty()) {
            MDC.put(TRACE_ID_MDC_KEY, traceId);
        }
    }

    /**
     * 清理当前线程的traceId
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_MDC_KEY);
    }
}