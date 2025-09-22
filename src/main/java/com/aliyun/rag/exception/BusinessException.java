package com.aliyun.rag.exception;

import com.aliyun.rag.model.ErrorCode;

/**
 * 业务异常类
 * <p>
 * 用于封装业务逻辑中的异常情况，配合ErrorCode提供统一的错误信息处理
 * 这个异常会被GlobalExceptionHandler统一捕获和处理
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String customMessage;
    
    /**
     * 使用ErrorCode构造业务异常
     * 
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }
    
    /**
     * 使用ErrorCode和自定义消息构造业务异常
     * 
     * @param errorCode 错误码枚举
     * @param customMessage 自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage != null ? customMessage : errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
    
    /**
     * 使用ErrorCode和异常原因构造业务异常
     * 
     * @param errorCode 错误码枚举
     * @param cause 异常原因
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = null;
    }
    
    /**
     * 使用ErrorCode、自定义消息和异常原因构造业务异常
     * 
     * @param errorCode 错误码枚举
     * @param customMessage 自定义错误消息
     * @param cause 异常原因
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage != null ? customMessage : errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
    
    /**
     * 获取错误码
     * 
     * @return 错误码枚举
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取自定义消息
     * 
     * @return 自定义错误消息
     */
    public String getCustomMessage() {
        return customMessage;
    }
    
    /**
     * 获取错误码的数值
     * 
     * @return 错误码数值
     */
    public int getErrorCodeValue() {
        return errorCode.getCode();
    }
}