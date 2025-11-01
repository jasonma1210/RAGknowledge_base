package com.aliyun.rag.model.dto;

import com.aliyun.rag.model.Conversation;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表DTO
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
public class ConversationDTO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话名称
     */
    private String sessionName;

    /**
     * 会话摘要
     */
    private String sessionSummary;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 从实体转换为DTO
     */
    public static ConversationDTO fromEntity(Conversation conversation) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setUserId(conversation.getUserId());
        dto.setSessionName(conversation.getSessionName());
        dto.setSessionSummary(conversation.getSessionSummary());
        dto.setMessageCount(conversation.getMessageCount());
        dto.setLastMessageTime(conversation.getLastMessageTime());
        dto.setCreateTime(conversation.getGmtCreate());
        return dto;
    }
}
