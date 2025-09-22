package com.aliyun.rag.model;

import com.aliyun.rag.model.dto.UserDTO;

/**
 * 认证响应
 * <p>
 * 用于用户认证接口的响应结果
 * 使用UserDTO替代User实体，避免敏感信息泄露
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
public class AuthResponse {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 刷新令牌（可选）
     */
    private String refreshToken;

    /**
     * 用户信息（不包含敏感字段）
     */
    private UserDTO user;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息或错误信息
     */
    private String message;

    /**
     * 错误码（可选）
     */
    private Integer errorCode;

    /**
     * 时间戳
     */
    private Long timestamp;

    public AuthResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建成功响应
     *
     * @param token 访问令牌
     * @param user 用户信息
     * @param message 消息
     * @return AuthResponse
     */
    public static AuthResponse success(String token, UserDTO user, String message) {
        AuthResponse response = new AuthResponse();
        response.setSuccess(true);
        response.setToken(token);
        response.setUser(user);
        response.setMessage(message);
        return response;
    }

    /**
     * 创建失败响应
     *
     * @param errorCode 错误码
     * @param message 错误信息
     * @return AuthResponse
     */
    public static AuthResponse failure(ErrorCode errorCode, String message) {
        AuthResponse response = new AuthResponse();
        response.setSuccess(false);
        response.setErrorCode(errorCode.getCode());
        response.setMessage(message != null ? message : errorCode.getMessage());
        return response;
    }

    /**
     * 创建失败响应
     *
     * @param errorCode 错误码
     * @return AuthResponse
     */
    public static AuthResponse failure(ErrorCode errorCode) {
        return failure(errorCode, null);
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
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

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}