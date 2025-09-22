package com.aliyun.rag.interceptor;

import com.aliyun.rag.model.User;
import com.aliyun.rag.service.AuthService;
import com.aliyun.rag.service.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    public AuthInterceptor(AuthService authService, JwtTokenService jwtTokenService) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"无效的访问令牌\"}");
            return false;
        }
        
        // 获取用户信息
        User user = authService.validateToken(token);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"用户信息不存在\"}");
            return false;
        }
        
        // 检查Token是否即将过期，如果是则记录日志提示前端刷新
        if (jwtTokenService.isTokenExpiringSoon(token)) {
            log.info("用户 {} 的Token即将过期，建议刷新", user.getUsername());
            // 可以在这里添加响应头提示前端刷新Token
            response.setHeader("X-Token-Refresh-Needed", "true");
        }

        log.debug("用户认证成功: {}", user.getUsername());
        // 将用户信息存储到请求属性中
        request.setAttribute("currentUser", user);
        return true;
    }
}