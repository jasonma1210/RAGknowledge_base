package com.aliyun.rag.model;

/**
 * 用户注册请求
 * <p>
 * 用于用户注册接口的请求参数
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-10
 */
public class RegisterRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}