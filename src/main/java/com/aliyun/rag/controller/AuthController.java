package com.aliyun.rag.controller;

import com.aliyun.rag.model.LoginRequest;
import com.aliyun.rag.model.RegisterRequest;
import com.aliyun.rag.model.ChangePasswordRequest;
import com.aliyun.rag.model.AuthResponse;
import com.aliyun.rag.model.User;
import com.aliyun.rag.model.R;
import com.aliyun.rag.model.dto.UserDTO;
import com.aliyun.rag.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 认证控制器
 * <p>
 * 提供用户注册、登录等认证接口
 * 符合Controller层规范：只负责接收请求参数和返回响应，不处理业务逻辑或异常
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-10
 */
@RestController
@RequestMapping("/api/auth")
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
    public ResponseEntity<R<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(R.success(response));
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<R<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(R.success(response));
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<R<AuthResponse>> logout(HttpServletRequest request) {
        // 从拦截器获取已解析的用户信息
        User currentUser = (User) request.getAttribute("currentUser");
        
        AuthResponse response = authService.logout(currentUser);
        return ResponseEntity.ok(R.success(response));
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<R<UserDTO>> getProfile(HttpServletRequest request) {
        // 从拦截器获取已解析的用户信息
        User currentUser = (User) request.getAttribute("currentUser");
        
        // 转换为UserDTO以避免敏感信息泄露
        UserDTO userDTO = UserDTO.fromUser(currentUser);
        return ResponseEntity.ok(R.success(userDTO));
    }
    
    /**
     * 修改用户密码
     */
    @PutMapping("/change-password")
    public ResponseEntity<R<AuthResponse>> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        // 从拦截器获取已解析的用户信息
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        
        AuthResponse response = authService.changePassword(currentUser, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(R.success(response));
    }
    
    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<R<AuthResponse>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            AuthResponse response = AuthResponse.failure(com.aliyun.rag.model.ErrorCode.TOKEN_MISSING, "缺少刷新令牌");
            return ResponseEntity.badRequest().body(R.success(response));
        }
        
        AuthResponse response = authService.refreshToken(refreshToken);
        if (response.isSuccess()) {
            return ResponseEntity.ok(R.success(response));
        } else {
            return ResponseEntity.badRequest().body(R.success(response));
        }
    }
}