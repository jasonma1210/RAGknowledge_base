package com.aliyun.rag.model;

import lombok.Data;

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
@Data
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

}