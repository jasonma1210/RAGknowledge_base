package com.aliyun.rag.model.dto;

import com.aliyun.rag.model.SearchRequest;
import lombok.Data;

/**
 * AI问答请求DTO（支持上下文记忆）
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
public class ConversationAskRequest {

    /**
     * 会话ID（可选，如果为空则创建新会话）
     */
    private Long conversationId;

    /**
     * 用户问题
     */
    private String question;

    /**
     * 搜索类型（SEMANTIC/KEYWORD/HYBRID）
     */
    private SearchRequest.SearchType searchType = SearchRequest.SearchType.HYBRID;

    /**
     * 最大搜索结果数
     */
    private Integer maxResults = 5;

    /**
     * 最小相关度分数
     */
    private Double minScore = 0.7;

    /**
     * 是否启用上下文记忆（默认启用）
     */
    private Boolean enableContext = true;

    /**
     * 上下文消息数量（默认最近10条）
     */
    private Integer contextMessageCount = 10;
}
