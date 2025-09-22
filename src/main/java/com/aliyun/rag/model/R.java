package com.aliyun.rag.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一响应结果封装类
 * <p>
 * 用于封装所有API接口的返回结果，提供统一的响应格式
 * 包含success、code、data三个核心字段
 * </p>
 *
 * @param <T> 数据对象类型
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-21
 */
public class R<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 状态码：200表示成功，500表示失败
     */
    private Integer code;
    
    /**
     * 数据对象
     */
    private T data;
    
    /**
     * 时间戳
     */
    private String timestamp;
    
    /**
     * 构造函数
     */
    public R() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    /**
     * 成功响应
     *
     * @param data 数据对象
     * @param <T>  数据类型
     * @return 统一响应结果
     */
    public static <T> R<T> success(T data) {
        R<T> response = new R<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setData(data);
        return response;
    }
    
    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 统一响应结果
     */
    public static <T> R<T> success() {
        R<T> response = new R<>();
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }
    
    /**
     * 失败响应
     *
     * @param code 错误码
     * @param <T>  数据类型
     * @return 统一响应结果
     */
    public static <T> R<T> error(Integer code) {
        R<T> response = new R<>();
        response.setSuccess(false);
        response.setCode(code);
        return response;
    }
    
    /**
     * 失败响应
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 统一响应结果
     */
    public static <T> R<T> error(Integer code, String message) {
        R<T> response = new R<>();
        response.setSuccess(false);
        response.setCode(code);
        // 注意：为了保持响应格式统一，错误信息通过data字段返回
        response.setData((T) message);
        return response;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "R{" +
                "success=" + success +
                ", code=" + code +
                ", data=" + data +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}