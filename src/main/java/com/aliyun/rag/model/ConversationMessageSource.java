package com.aliyun.rag.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话消息引用文档实体
 * <p>
 * 用于存储AI回答时引用的文档来源
 * </p>
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
@Entity
@Table(name = "conversation_message_source")
public class ConversationMessageSource {

    /**
     * 记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 消息ID
     */
    @Column(name = "message_id", nullable = false)
    private Long messageId;

    /**
     * 会话ID
     */
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    /**
     * 文档ID
     */
    @Column(name = "document_id", nullable = false, length = 100)
    private String documentId;

    /**
     * 文档标题
     */
    @Column(name = "document_title", length = 500)
    private String documentTitle;

    /**
     * 引用片段内容
     */
    @Column(name = "chunk_content", columnDefinition = "TEXT")
    private String chunkContent;

    /**
     * 相关性分数
     */
    @Column(name = "relevance_score")
    private Double relevanceScore;

    /**
     * 创建时间
     */
    @Column(name = "gmt_create", nullable = false, updatable = false)
    private LocalDateTime gmtCreate;

    @PrePersist
    protected void onCreate() {
        gmtCreate = LocalDateTime.now();
    }
}
