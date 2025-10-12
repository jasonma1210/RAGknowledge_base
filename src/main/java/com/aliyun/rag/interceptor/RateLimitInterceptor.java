package com.aliyun.rag.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * API限流拦截器
 * <p>
 * 实现基于Redis的滑动窗口限流机制
 * 支持不同接口类型的差异化限流策略
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 限流配置
    private static final int AUTH_LIMIT = 5; // 认证接口：5次/分钟
    private static final int DOCUMENT_UPLOAD_LIMIT = 10; // 文档上传：10次/分钟
    private static final int SEARCH_LIMIT = 60; // 搜索接口：60次/分钟
    private static final int AI_CHAT_LIMIT = 20; // AI问答：20次/分钟
    private static final int MONITOR_LIMIT = 100; // 监控端点：100次/分钟
    private static final int DEFAULT_LIMIT = 30; // 默认：30次/分钟

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientId(request);
        String endpoint = request.getRequestURI();
        String method = request.getMethod();

        // 跳过OPTIONS请求
        if ("OPTIONS".equals(method)) {
            return true;
        }

        // 获取限流配置
        int limit = getLimitForEndpoint(endpoint);
        
        // 检查是否允许访问
        if (!isAllowed(clientId, endpoint, limit)) {
            log.warn("API限流触发，客户端: {}, 端点: {}, 限制: {}/分钟", clientId, endpoint, limit);
            
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"success\":false,\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }

        return true;
    }

    /**
     * 检查是否允许访问
     */
    private boolean isAllowed(String clientId, String endpoint, int limit) {
        String key = "rate_limit:" + clientId + ":" + endpoint;
        
        try {
            // 使用Redis的INCR和EXPIRE实现滑动窗口
            String count = redisTemplate.opsForValue().get(key);
            
            if (count == null) {
                // 第一次访问
                redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
                return true;
            }
            
            int currentCount = Integer.parseInt(count);
            
            if (currentCount >= limit) {
                return false;
            }
            
            // 增加计数
            redisTemplate.opsForValue().increment(key);
            
            // 如果是第一次增加，设置过期时间
            if (currentCount == 0) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("限流检查失败: {}", e.getMessage(), e);
            // 限流失败时允许访问，避免影响正常业务
            return true;
        }
    }

    /**
     * 获取客户端标识
     */
    private String getClientId(HttpServletRequest request) {
        // 优先使用用户ID（如果已认证）
        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            return "user:" + userId;
        }
        
        // 使用IP地址
        String ip = getClientIP(request);
        return "ip:" + ip;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 根据端点获取限流配置
     */
    private int getLimitForEndpoint(String endpoint) {
        // 认证相关接口
        if (endpoint.contains("/auth/login") || endpoint.contains("/auth/register")) {
            return AUTH_LIMIT;
        }
        
        // 文档上传接口
        if (endpoint.contains("/document/upload") || endpoint.contains("/document/process")) {
            return DOCUMENT_UPLOAD_LIMIT;
        }
        
        // 搜索接口
        if (endpoint.contains("/search") || endpoint.contains("/vector")) {
            return SEARCH_LIMIT;
        }
        
        // AI问答接口
        if (endpoint.contains("/chat") || endpoint.contains("/ask")) {
            return AI_CHAT_LIMIT;
        }
        
        // 监控端点
        if (endpoint.contains("/actuator")) {
            return MONITOR_LIMIT;
        }
        
        return DEFAULT_LIMIT;
    }

    /**
     * 获取当前限流状态
     */
    public RateLimitStatus getRateLimitStatus(String clientId, String endpoint) {
        String key = "rate_limit:" + clientId + ":" + endpoint;
        int limit = getLimitForEndpoint(endpoint);
        
        try {
            String count = redisTemplate.opsForValue().get(key);
            int currentCount = count != null ? Integer.parseInt(count) : 0;
            
            return new RateLimitStatus(currentCount, limit, limit - currentCount);
        } catch (Exception e) {
            log.error("获取限流状态失败: {}", e.getMessage(), e);
            return new RateLimitStatus(0, limit, limit);
        }
    }

    /**
     * 限流状态类
     */
    public static class RateLimitStatus {
        private final int currentCount;
        private final int limit;
        private final int remaining;

        public RateLimitStatus(int currentCount, int limit, int remaining) {
            this.currentCount = currentCount;
            this.limit = limit;
            this.remaining = remaining;
        }

        public int getCurrentCount() {
            return currentCount;
        }

        public int getLimit() {
            return limit;
        }

        public int getRemaining() {
            return remaining;
        }

        public boolean isLimited() {
            return remaining <= 0;
        }
    }
}
