package com.aliyun.rag.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 全局异常处理器
 * <p>
 * 统一处理控制器层抛出的异常，提供标准化的错误响应格式
 * 支持参数验证、文件上传、业务异常等多种异常类型的处理
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "参数验证失败");
        response.put("message", Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "文件过大");
        response.put("message", "上传的文件超过了最大允许大小");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "参数错误");
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        Map<String, Object> response = new HashMap<>();
        response.put("error", "处理失败");
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("系统异常", e);
        Map<String, Object> response = new HashMap<>();
        response.put("error", "系统错误");
        response.put("message", "服务器内部错误");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.internalServerError().body(response);
    }
}