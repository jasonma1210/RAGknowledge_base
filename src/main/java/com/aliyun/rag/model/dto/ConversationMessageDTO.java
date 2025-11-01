package com.aliyun.rag.model.dto;

import com.aliyun.rag.model.ConversationMessage;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话消息DTO
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
public class ConversationMessageDTO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 消息类型（USER/ASSISTANT）
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 引用文档数量
     */
    private Integer sourceCount;

    /**
     * 搜索类型
     */
    private String searchType;

    /**
     * Token消耗
     */
    private Integer tokenCount;

    /**
     * 响应时间（毫秒）
     */
    private Integer responseTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 从实体转换为DTO
     */
    public static ConversationMessageDTO fromEntity(ConversationMessage message) {
        ConversationMessageDTO dto = new ConversationMessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setMessageType(message.getMessageType().name());
        dto.setContent(message.getContent());
        dto.setSourceCount(message.getSourceCount());
        dto.setSearchType(message.getSearchType());
        dto.setTokenCount(message.getTokenCount());
        dto.setResponseTime(message.getResponseTime());
        dto.setCreateTime(message.getGmtCreate());
        return dto;
    }
}
