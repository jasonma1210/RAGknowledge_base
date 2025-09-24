package com.aliyun.rag.interceptor;

import com.aliyun.rag.model.User;
import com.aliyun.rag.service.AuthService;
import com.aliyun.rag.service.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 * <p>
 * 拦截需要认证的请求，验证JWT访问令牌并设置当前用户信息
 * 支持Token的验证和解析
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    
    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public AuthInterceptor(AuthService authService, JwtTokenService jwtTokenService, RedisTemplate<String, Object> redisTemplate) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取访问令牌
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉"Bearer "前缀
        }
        
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"缺少访问令牌\"}");
            return false;
        }
        
        // 验证JWT令牌
        if (!jwtTokenService.validateToken(token)) {
            // 如果Access Token无效，尝试使用Refresh Token刷新
            String refreshToken = request.getHeader("X-Refresh-Token");
            if (refreshToken != null && !refreshToken.isEmpty()) {
                try {
                    // 验证刷新令牌有效性
                    if (jwtTokenService.validateToken(refreshToken)) {
                        // 检查Token类型是否为REFRESH
                        String tokenType = jwtTokenService.getTokenType(refreshToken);
                        if ("REFRESH".equals(tokenType)) {
                            // 从令牌中获取用户ID
                            Long userId = jwtTokenService.getUserIdFromToken(refreshToken);
                            if (userId != null) {
                                // 从Redis中获取存储的刷新令牌进行比对
                                String refreshTokenKey = "refresh_token:" + userId;
                                String storedRefreshToken = (String) redisTemplate.opsForValue().get(refreshTokenKey);
                                if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
                                    // 获取用户信息
                                    User user = authService.getUserById(userId);
                                    if (user != null) {
                                        // 生成新的Access Token
                                        String newAccessToken = jwtTokenService.refreshAccessToken(refreshToken, user);
                                        
                                        // 将新的Access Token添加到响应头中
                                        response.setHeader("X-New-Access-Token", newAccessToken);
                                        
                                        // 使用新的Access Token继续验证
                                        token = newAccessToken;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("刷新Token失败: {}", e.getMessage());
                }
            }
            
            // 如果仍然无效，则返回错误
            if (!jwtTokenService.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"无效的访问令牌\"}");
                return false;
            }
        }
        
        // 获取用户信息
        User user = authService.validateToken(token);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"用户信息不存在\"}");
            return false;
        }
        
        // 检查Token是否即将过期，如果是则在响应头中添加提示
        if (jwtTokenService.isTokenExpiringSoon(token)) {
            log.info("用户 {} 的Token即将过期，建议刷新", user.getUsername());
            // 添加响应头提示前端刷新Token
            response.setHeader("X-Token-Refresh-Needed", "true");
        }

        log.debug("用户认证成功: {}", user.getUsername());
        // 将用户信息存储到请求属性中
        request.setAttribute("currentUser", user);
        return true;
    }
}