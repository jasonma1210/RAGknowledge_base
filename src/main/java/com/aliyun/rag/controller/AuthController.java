package com.aliyun.rag.controller;

import com.aliyun.rag.model.LoginRequest;
import com.aliyun.rag.model.RegisterRequest;
import com.aliyun.rag.model.AuthResponse;
import com.aliyun.rag.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
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
}