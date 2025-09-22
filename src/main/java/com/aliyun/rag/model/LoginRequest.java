package com.aliyun.rag.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户登录请求
 * <p>
 * 用于用户登录接口的请求参数
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-10
 */
public class LoginRequest {

    /**
     * 用户名
     */
    @JsonProperty("username")
    private String username;

    /**
     * 密码
     */
    @JsonProperty("password")
    private String password;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}