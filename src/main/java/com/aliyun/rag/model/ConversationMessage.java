package com.aliyun.rag.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话消息实体
 * <p>
 * 用于存储会话中的每条消息（用户问题和AI回答）
 * </p>
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
@Entity
@Table(name = "conversation_message")
public class ConversationMessage {

    /**
     * 消息ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会话ID
     */
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 消息类型（USER-用户问题，ASSISTANT-AI回答）
     */
    @Column(name = "message_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    /**
     * 消息内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 引用文档数量（仅ASSISTANT类型）
     */
    @Column(name = "source_count")
    private Integer sourceCount = 0;

    /**
     * 搜索类型（SEMANTIC/KEYWORD/HYBRID）
     */
    @Column(name = "search_type", length = 20)
    private String searchType;

    /**
     * Token消耗数量
     */
    @Column(name = "token_count")
    private Integer tokenCount = 0;

    /**
     * 响应时间（毫秒）
     */
    @Column(name = "response_time")
    private Integer responseTime = 0;

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
        if (sourceCount == null) {
            sourceCount = 0;
        }
        if (tokenCount == null) {
            tokenCount = 0;
        }
        if (responseTime == null) {
            responseTime = 0;
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
     * 消息类型枚举
     */
    public enum MessageType {
        /**
         * 用户问题
         */
        USER,
        /**
         * AI回答
         */
        ASSISTANT
    }
}
