package com.aliyun.rag.interceptor;

import com.aliyun.rag.service.MetricsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 访问日志拦截器
 * <p>
 * 记录API访问日志，包括请求信息、响应信息、执行时间等
 * 支持结构化日志输出和监控指标统计
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-19
 */
@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    private static final Logger accessLog = LoggerFactory.getLogger("ACCESS_LOG");
    private static final String START_TIME_ATTR = "startTime";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;

    public AccessLogInterceptor(MetricsService metricsService, ObjectMapper objectMapper) {
        this.metricsService = metricsService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 在这里可以记录一些中间处理信息
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 计算执行时间
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long executionTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

        // 构建访问日志
        AccessLogEntry logEntry = buildAccessLogEntry(request, response, executionTime, ex);

        // 输出结构化日志
        try {
            String logJson = objectMapper.writeValueAsString(logEntry);
            accessLog.info(logJson);
        } catch (Exception e) {
            accessLog.error("Failed to serialize access log: {}", e.getMessage());
        }

        // 记录监控指标
        recordMetrics(request, response, executionTime);
    }

    /**
     * 构建访问日志条目
     */
    private AccessLogEntry buildAccessLogEntry(HttpServletRequest request, HttpServletResponse response, 
                                             long executionTime, Exception ex) {
        AccessLogEntry entry = new AccessLogEntry();
        
        // 基本请求信息
        entry.setTimestamp(LocalDateTime.now().format(FORMATTER));
        entry.setTraceId(generateTraceId());
        entry.setMethod(request.getMethod());
        entry.setUrl(request.getRequestURL().toString());
        entry.setUri(request.getRequestURI());
        entry.setQueryString(request.getQueryString());
        entry.setProtocol(request.getProtocol());
        
        // 客户端信息
        entry.setRemoteAddr(getClientIpAddress(request));
        entry.setUserAgent(request.getHeader("User-Agent"));
        entry.setReferer(request.getHeader("Referer"));
        
        // 响应信息
        entry.setStatus(response.getStatus());
        entry.setExecutionTimeMs(executionTime);
        
        // 异常信息
        if (ex != null) {
            entry.setException(ex.getClass().getSimpleName());
            entry.setExceptionMessage(ex.getMessage());
        }
        
        // 请求头（过滤敏感信息）
        entry.setHeaders(getFilteredHeaders(request));
        
        return entry;
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 获取过滤后的请求头（排除敏感信息）
     */
    private Map<String, String> getFilteredHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // 过滤敏感请求头
            if (isSensitiveHeader(headerName)) {
                headers.put(headerName, "***");
            } else {
                headers.put(headerName, headerValue);
            }
        }
        
        return headers;
    }

    /**
     * 判断是否为敏感请求头
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerCase = headerName.toLowerCase();
        return lowerCase.contains("authorization") ||
               lowerCase.contains("cookie") ||
               lowerCase.contains("token") ||
               lowerCase.contains("password") ||
               lowerCase.contains("secret");
    }

    /**
     * 记录监控指标
     */
    private void recordMetrics(HttpServletRequest request, HttpServletResponse response, long executionTime) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();
        
        // 根据URI路径记录不同类型的请求
        if (uri.startsWith("/api/documents")) {
            if ("POST".equals(method)) {
                if (status >= 200 && status < 300) {
                    metricsService.recordDocumentUpload("standard");
                } else {
                    metricsService.recordDocumentUploadFailure("http_error_" + status);
                }
            } else if ("DELETE".equals(method)) {
                metricsService.recordDocumentDelete();
            }
        } else if (uri.startsWith("/search")) {
            metricsService.recordSearchRequest("vector_search");
        } else if (uri.startsWith("/chat")) {
            metricsService.recordAiChat();
        } else if (uri.startsWith("/auth/login")) {
            if (status >= 200 && status < 300) {
                metricsService.recordUserLogin("standard");
            } else {
                metricsService.recordUserLoginFailure("invalid_credentials");
            }
        } else if (uri.startsWith("/auth/register")) {
            if (status >= 200 && status < 300) {
                metricsService.recordUserRegistration();
            }
        }
    }

    /**
     * 访问日志条目数据结构
     */
    public static class AccessLogEntry {
        private String timestamp;
        private String traceId;
        private String method;
        private String url;
        private String uri;
        private String queryString;
        private String protocol;
        private String remoteAddr;
        private String userAgent;
        private String referer;
        private int status;
        private long executionTimeMs;
        private String exception;
        private String exceptionMessage;
        private Map<String, String> headers;

        // Getters and Setters
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }

        public String getQueryString() { return queryString; }
        public void setQueryString(String queryString) { this.queryString = queryString; }

        public String getProtocol() { return protocol; }
        public void setProtocol(String protocol) { this.protocol = protocol; }

        public String getRemoteAddr() { return remoteAddr; }
        public void setRemoteAddr(String remoteAddr) { this.remoteAddr = remoteAddr; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

        public String getReferer() { return referer; }
        public void setReferer(String referer) { this.referer = referer; }

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }

        public long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

        public String getException() { return exception; }
        public void setException(String exception) { this.exception = exception; }

        public String getExceptionMessage() { return exceptionMessage; }
        public void setExceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; }

        public Map<String, String> getHeaders() { return headers; }
        public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    }
    
    /**
     * 生成追踪ID
     */
    private String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}