package com.aliyun.rag.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@Data
public class LoginRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;

}