package com.aliyun.rag.model;

/**
 * 错误码枚举
 * <p>
 * 统一定义系统中的错误码和错误信息
 * 避免在代码中硬编码错误信息，提升安全性
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
public enum ErrorCode {

    // 认证相关错误 1000-1999
    AUTH_FAILED(1001, "用户名或密码错误"),
    TOKEN_INVALID(1002, "访问令牌无效"),
    TOKEN_EXPIRED(1003, "访问令牌已过期"),
    TOKEN_MISSING(1004, "缺少访问令牌"),
    PERMISSION_DENIED(1005, "权限不足"),
    USER_NOT_FOUND(1006, "用户不存在"),
    USER_ALREADY_EXISTS(1007, "用户名已存在"),
    PASSWORD_INCORRECT(1008, "原密码错误"),
    PASSWORD_SAME(1009, "新密码不能与原密码相同"),

    // 参数验证错误 2000-2999
    PARAM_INVALID(2001, "参数验证失败"),
    PARAM_MISSING(2002, "缺少必需参数"),
    PARAM_FORMAT_ERROR(2003, "参数格式错误"),

    // 文件相关错误 3000-3999
    FILE_UPLOAD_FAILED(3001, "文件上传失败"),
    FILE_TOO_LARGE(3002, "文件过大"),
    FILE_TYPE_NOT_SUPPORTED(3003, "不支持的文件类型"),
    FILE_NOT_FOUND(3004, "文件不存在"),
    FILE_PROCESSING_FAILED(3005, "文件处理失败"),

    // 存储相关错误 4000-4999
    STORAGE_QUOTA_EXCEEDED(4001, "存储空间不足"),
    DOCUMENT_NOT_FOUND(4002, "文档不存在"),
    DOCUMENT_PROCESSING_FAILED(4003, "文档处理失败"),
    DOCUMENT_IN_PROCESSING(4004, "文档正在处理中，无法删除"),

    // 搜索相关错误 5000-5999
    SEARCH_FAILED(5001, "搜索失败"),
    SEARCH_QUERY_EMPTY(5002, "搜索内容不能为空"),
    SEARCH_RESULTS_EMPTY(5003, "未找到相关内容"),

    // AI服务相关错误 6000-6999
    AI_SERVICE_UNAVAILABLE(6001, "AI服务不可用"),
    AI_REQUEST_FAILED(6002, "AI请求失败"),
    AI_RESPONSE_ERROR(6003, "AI响应错误"),

    // 系统错误 9000-9999
    SYSTEM_ERROR(9001, "系统内部错误"),
    DATABASE_ERROR(9002, "数据库错误"),
    NETWORK_ERROR(9003, "网络错误"),
    SERVICE_UNAVAILABLE(9004, "服务不可用"),
    OPERATION_TIMEOUT(9005, "操作超时"),
    SYSTEM_MAINTENANCE(9006, "系统维护中"),
    RATE_LIMIT_EXCEEDED(9007, "请求过于频繁");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取ErrorCode枚举
     *
     * @param code 错误码
     * @return ErrorCode枚举
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR; // 默认返回系统错误
    }

    @Override
    public String toString() {
        return String.format("ErrorCode{code=%d, message='%s'}", code, message);
    }
}