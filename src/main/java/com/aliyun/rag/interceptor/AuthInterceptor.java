package com.aliyun.rag.interceptor;

import com.aliyun.rag.model.User;
import com.aliyun.rag.service.AuthService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 * <p>
 * 拦截需要认证的请求，验证访问令牌并设置当前用户信息
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-10
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    private final AuthService authService;
    
    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
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
            response.getWriter().write("{\"success\":false,\"message\":\"缺少访问令牌\"}");
            return false;
        }
        
        // 验证令牌
        User user = authService.validateToken(token);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"无效的访问令牌\"}");
            return false;
        }

        System.out.println("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
        // 将用户信息存储到请求属性中
        request.setAttribute("currentUser", user);
        return true;
    }
}