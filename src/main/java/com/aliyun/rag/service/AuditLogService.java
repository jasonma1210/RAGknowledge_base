package com.aliyun.rag.service;

import com.aliyun.rag.model.User;
import com.aliyun.rag.util.HttpRequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 审计日志服务
 * <p>
 * 记录关键业务操作，用于安全审计和问题追溯
 * 审计日志独立于业务日志，保存更长时间（建议365天）
 * </p>
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Service
public class AuditLogService {

    /**
     * 独立的审计日志器
     * 在logback配置中单独配置输出到audit.log文件
     */
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT_LOG");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 记录文档上传操作
     *
     * @param user 用户信息
     * @param documentId 文档ID
     * @param documentTitle 文档标题
     * @param fileSize 文件大小（字节）
     */
    public void logDocumentUpload(User user, String documentId, String documentTitle, long fileSize) {
        auditLog.info("文档上传 - 用户ID: {}, 用户名: {}, 文档ID: {}, 文档标题: {}, 文件大小: {} bytes, IP: {}, 时间: {}",
            user.getId(),
            user.getUsername(),
            documentId,
            documentTitle,
            fileSize,
            getClientIP(),
            getCurrentTime()
        );
    }

    /**
     * 记录文档删除操作
     *
     * @param user 用户信息
     * @param documentId 文档ID
     * @param documentTitle 文档标题
     */
    public void logDocumentDeletion(User user, String documentId, String documentTitle) {
        auditLog.warn("文档删除 - 用户ID: {}, 用户名: {}, 文档ID: {}, 文档标题: {}, IP: {}, 时间: {}",
            user.getId(),
            user.getUsername(),
            documentId,
            documentTitle,
            getClientIP(),
            getCurrentTime()
        );
    }

    /**
     * 记录密码修改操作
     *
     * @param user 用户信息
     * @param success 是否成功
     * @param failureReason 失败原因（如果失败）
     */
    public void logPasswordChange(User user, boolean success, String failureReason) {
        if (success) {
            auditLog.warn("密码修改成功 - 用户ID: {}, 用户名: {}, IP: {}, 时间: {}",
                user.getId(),
                user.getUsername(),
                getClientIP(),
                getCurrentTime()
            );
        } else {
            auditLog.error("密码修改失败 - 用户ID: {}, 用户名: {}, 原因: {}, IP: {}, 时间: {}",
                user.getId(),
                user.getUsername(),
                failureReason,
                getClientIP(),
                getCurrentTime()
            );
        }
    }

    /**
     * 记录登录尝试
     *
     * @param username 用户名
     * @param success 是否成功
     * @param failureReason 失败原因（如果失败）
     */
    public void logLoginAttempt(String username, boolean success, String failureReason) {
        if (success) {
            auditLog.info("登录成功 - 用户名: {}, IP: {}, 时间: {}",
                username,
                getClientIP(),
                getCurrentTime()
            );
        } else {
            auditLog.warn("登录失败 - 用户名: {}, 原因: {}, IP: {}, 时间: {}",
                username,
                failureReason != null ? failureReason : "未知",
                getClientIP(),
                getCurrentTime()
            );
        }
    }

    /**
     * 记录用户注册
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param success 是否成功
     */
    public void logUserRegistration(String username, Long userId, boolean success) {
        if (success) {
            auditLog.info("用户注册成功 - 用户名: {}, 用户ID: {}, IP: {}, 时间: {}",
                username,
                userId,
                getClientIP(),
                getCurrentTime()
            );
        } else {
            auditLog.warn("用户注册失败 - 用户名: {}, IP: {}, 时间: {}",
                username,
                getClientIP(),
                getCurrentTime()
            );
        }
    }

    /**
     * 记录敏感数据访问
     *
     * @param user 用户信息
     * @param operation 操作类型（如：查看、导出、修改）
     * @param resourceType 资源类型（如：文档、用户信息）
     * @param resourceId 资源ID
     */
    public void logSensitiveDataAccess(User user, String operation, String resourceType, String resourceId) {
        auditLog.info("敏感数据访问 - 用户: {} (ID: {}), 操作: {}, 资源类型: {}, 资源ID: {}, IP: {}, 时间: {}",
            user.getUsername(),
            user.getId(),
            operation,
            resourceType,
            resourceId,
            getClientIP(),
            getCurrentTime()
        );
    }

    /**
     * 记录批量操作
     *
     * @param user 用户信息
     * @param operation 操作类型
     * @param count 操作数量
     * @param details 详细信息
     */
    public void logBatchOperation(User user, String operation, int count, String details) {
        auditLog.info("批量操作 - 用户: {} (ID: {}), 操作: {}, 数量: {}, 详情: {}, IP: {}, 时间: {}",
            user.getUsername(),
            user.getId(),
            operation,
            count,
            details,
            getClientIP(),
            getCurrentTime()
        );
    }

    /**
     * 记录搜索操作（用于行为分析）
     *
     * @param user 用户信息
     * @param query 搜索关键词
     * @param resultCount 结果数量
     * @param searchType 搜索类型（语义/关键词/混合）
     */
    public void logSearch(User user, String query, int resultCount, String searchType) {
        auditLog.info("搜索操作 - 用户: {} (ID: {}), 关键词: {}, 结果数: {}, 类型: {}, IP: {}, 时间: {}",
            user.getUsername(),
            user.getId(),
            maskSensitiveQuery(query), // 脱敏处理
            resultCount,
            searchType,
            getClientIP(),
            getCurrentTime()
        );
    }

    /**
     * 记录AI问答操作
     *
     * @param user 用户信息
     * @param question 问题（脱敏）
     * @param answerLength 答案长度
     * @param sourceCount 引用来源数量
     */
    public void logAIQuestion(User user, String question, int answerLength, int sourceCount) {
        auditLog.info("AI问答 - 用户: {} (ID: {}), 问题: {}, 答案长度: {}, 来源数: {}, IP: {}, 时间: {}",
            user.getUsername(),
            user.getId(),
            maskSensitiveQuery(question),
            answerLength,
            sourceCount,
            getClientIP(),
            getCurrentTime()
        );
    }

    /**
     * 记录权限变更
     *
     * @param operatorUser 操作者
     * @param targetUserId 目标用户ID
     * @param oldLevel 原等级
     * @param newLevel 新等级
     */
    public void logPermissionChange(User operatorUser, Long targetUserId, int oldLevel, int newLevel) {
        auditLog.warn("权限变更 - 操作者: {} (ID: {}), 目标用户ID: {}, 原等级: {}, 新等级: {}, IP: {}, 时间: {}",
            operatorUser.getUsername(),
            operatorUser.getId(),
            targetUserId,
            oldLevel,
            newLevel,
            getClientIP(),
            getCurrentTime()
        );
    }

    /**
     * 记录异常登录尝试（可疑行为）
     *
     * @param username 用户名
     * @param reason 原因
     * @param additionalInfo 附加信息
     */
    public void logSuspiciousActivity(String username, String reason, String additionalInfo) {
        auditLog.error("可疑活动 - 用户名: {}, 原因: {}, 附加信息: {}, IP: {}, 时间: {}",
            username,
            reason,
            additionalInfo,
            getClientIP(),
            getCurrentTime()
        );
    }

    /**
     * 获取客户端IP地址
     * 从RequestContextHolder获取（需要配合Filter实现）
     */
    private String getClientIP() {
        try {
            return HttpRequestContextHolder.getClientIP();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 获取当前时间
     */
    private String getCurrentTime() {
        return LocalDateTime.now().format(FORMATTER);
    }

    /**
     * 脱敏处理查询内容
     * 限制长度，避免记录过长的查询
     */
    private String maskSensitiveQuery(String query) {
        if (query == null) {
            return "";
        }
        // 限制长度为100字符
        if (query.length() > 100) {
            return query.substring(0, 100) + "...";
        }
        return query;
    }
}
