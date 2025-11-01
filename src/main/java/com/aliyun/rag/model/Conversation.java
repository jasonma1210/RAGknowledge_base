package com.aliyun.rag.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话会话实体
 * <p>
 * 用于存储用户的对话会话，每个会话包含多条消息，支持上下文记忆
 * </p>
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
@Entity
@Table(name = "conversation")
public class Conversation {

    /**
     * 会话ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 会话名称（取首条问题前8个字）
     */
    @Column(name = "session_name", nullable = false, length = 100)
    private String sessionName;

    /**
     * 会话摘要（可选）
     */
    @Column(name = "session_summary", length = 500)
    private String sessionSummary;

    /**
     * 消息数量
     */
    @Column(name = "message_count")
    private Integer messageCount = 0;

    /**
     * 最后一条消息时间
     */
    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    /**
     * 创建时间
     */
    @Column(name = "gmt_create", nullable = false, updatable = false)
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    @Column(name = "gmt_modified", nullable = false)
    private LocalDateTime gmtModified;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;

    @PrePersist
    protected void onCreate() {
        gmtCreate = LocalDateTime.now();
        gmtModified = LocalDateTime.now();
        if (messageCount == null) {
            messageCount = 0;
        }
        if (isDeleted == null) {
            isDeleted = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        gmtModified = LocalDateTime.now();
    }

    /**
     * 生成会话名称（从问题中提取前8个字）
     *
     * @param question 用户问题
     * @return 会话名称
     */
    public static String generateSessionName(String question) {
        if (question == null || question.isEmpty()) {
            return "新对话";
        }
        // 去除空格和换行
        String cleaned = question.replaceAll("\\s+", "");
        
        // 如果是对话时间戳格式，直接返回
        if (cleaned.startsWith("对话")) {
            return cleaned;
        }
        
        if (cleaned.length() <= 8) {
            return cleaned;
        }
        return cleaned.substring(0, 8) + "...";
    }
}
