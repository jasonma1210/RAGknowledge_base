package com.aliyun.rag.exception;

import com.aliyun.rag.model.ErrorCode;
import com.aliyun.rag.model.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.io.IOException;
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

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<String>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        
        R<String> response = R.error(e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity.status(getHttpStatus(e.getErrorCode())).body(response);
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<String>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("参数验证失败: {}", e.getBindingResult().getFieldError().getDefaultMessage());
        
        R<String> response = R.error(ErrorCode.PARAM_INVALID.getCode(), 
            Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<R<String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件上传大小超限: {}", e.getMessage());
        
        R<String> response = R.error(ErrorCode.FILE_TOO_LARGE.getCode(), ErrorCode.FILE_TOO_LARGE.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<R<String>> handleMultipartException(MultipartException e) {
        log.warn("文件上传异常: {}", e.getMessage());
        
        R<String> response = R.error(ErrorCode.FILE_UPLOAD_FAILED.getCode(), ErrorCode.FILE_UPLOAD_FAILED.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理IO异常
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<R<String>> handleIOException(IOException e) {
        log.error("文件IO异常: {}", e.getMessage(), e);
        
        R<String> response = R.error(ErrorCode.FILE_PROCESSING_FAILED.getCode(), ErrorCode.FILE_PROCESSING_FAILED.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage());
        
        R<String> response = R.error(ErrorCode.PARAM_INVALID.getCode(), e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<R<String>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        
        R<String> response = R.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<String>> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        
        R<String> response = R.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
    
    /**
     * 根据ErrorCode获取对应的HTTP状态码
     */
    private HttpStatus getHttpStatus(ErrorCode errorCode) {
        switch (errorCode) {
            case AUTH_FAILED:
            case TOKEN_INVALID:
            case TOKEN_EXPIRED:
            case TOKEN_MISSING:
                return HttpStatus.UNAUTHORIZED;
            case PERMISSION_DENIED:
                return HttpStatus.FORBIDDEN;
            case USER_NOT_FOUND:
            case DOCUMENT_NOT_FOUND:
            case FILE_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case PARAM_INVALID:
            case PARAM_MISSING:
            case PARAM_FORMAT_ERROR:
            case FILE_TYPE_NOT_SUPPORTED:
            case FILE_TOO_LARGE:
            case STORAGE_QUOTA_EXCEEDED:
            case PASSWORD_INCORRECT:
            case PASSWORD_SAME:
            case USER_ALREADY_EXISTS:
            case DOCUMENT_IN_PROCESSING:
                return HttpStatus.BAD_REQUEST;
            case SYSTEM_ERROR:
            case DATABASE_ERROR:
            case NETWORK_ERROR:
            case SERVICE_UNAVAILABLE:
            case AI_SERVICE_UNAVAILABLE:
            case AI_REQUEST_FAILED:
            case AI_RESPONSE_ERROR:
            case OPERATION_TIMEOUT:
            case SYSTEM_MAINTENANCE:
                return HttpStatus.INTERNAL_SERVER_ERROR;
            case RATE_LIMIT_EXCEEDED:
                return HttpStatus.TOO_MANY_REQUESTS;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}