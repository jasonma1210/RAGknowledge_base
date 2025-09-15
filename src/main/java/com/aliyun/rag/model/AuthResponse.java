package com.aliyun.rag.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 认证响应
 * <p>
 * 用于用户认证接口的响应结果
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-10
 */
public class AuthResponse {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 用户信息
     */
    private User user;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String message;

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}