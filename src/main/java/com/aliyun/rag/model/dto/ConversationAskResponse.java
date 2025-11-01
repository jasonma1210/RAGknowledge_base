package com.aliyun.rag.model.dto;

import com.aliyun.rag.model.SearchResult;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI问答响应DTO（包含会话信息）
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
public class ConversationAskResponse {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 会话名称
     */
    private String sessionName;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 用户问题
     */
    private String question;

    /**
     * AI回答
     */
    private String answer;

    /**
     * 引用来源列表
     */
    private List<SearchResult> sources;

    /**
     * 引用来源数量
     */
    private Integer sourceCount;

    /**
     * 响应时间（毫秒）
     */
    private Integer responseTime;

    /**
     * Token消耗
     */
    private Integer tokenCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
