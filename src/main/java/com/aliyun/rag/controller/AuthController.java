package com.aliyun.rag.controller;

import com.aliyun.rag.model.LoginRequest;
import com.aliyun.rag.model.RegisterRequest;
import com.aliyun.rag.model.ChangePasswordRequest;
import com.aliyun.rag.model.AuthResponse;
import com.aliyun.rag.model.User;
import com.aliyun.rag.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 * <p>
 * 提供用户注册、登录等认证接口
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-10
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Value("${cors.allowed.origins:*}")
    private String allowedOrigins;
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("用户注册失败", e);
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("用户登录失败", e);
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("登录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request) {
        try {
            // 从请求头获取访问令牌
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // 去掉"Bearer "前缀
            }
            
            if (token != null && !token.isEmpty()) {
                authService.logout(token);
            }
            
            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("登出成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("用户登出失败", e);
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("登出失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(HttpServletRequest request) {
        try {
            // 从请求头获取访问令牌
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // 去掉"Bearer "前缀
            }
            
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(401).build();
            }
            
            // 验证令牌并获取用户信息
            User user = authService.validateToken(token);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 修改用户密码
     */
    @PutMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        try {
            // 从请求头获取访问令牌
            String token = httpRequest.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // 去掉"Bearer "前缀
            }
            
            if (token == null || token.isEmpty()) {
                AuthResponse response = new AuthResponse();
                response.setSuccess(false);
                response.setMessage("未授权访问");
                return ResponseEntity.status(401).body(response);
            }
            
            // 验证令牌并获取用户信息
            User user = authService.validateToken(token);
            if (user == null) {
                AuthResponse response = new AuthResponse();
                response.setSuccess(false);
                response.setMessage("令牌无效");
                return ResponseEntity.status(401).body(response);
            }
            
            // 调用服务层修改密码
            AuthResponse response = authService.changePassword(user, request.getOldPassword(), request.getNewPassword());
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("修改密码失败", e);
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("修改密码失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}