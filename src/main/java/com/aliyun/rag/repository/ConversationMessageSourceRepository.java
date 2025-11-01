package com.aliyun.rag.repository;

import com.aliyun.rag.model.ConversationMessageSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会话消息引用文档数据访问层
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Repository
public interface ConversationMessageSourceRepository extends JpaRepository<ConversationMessageSource, Long> {

    /**
     * 根据消息ID查询引用文档列表
     *
     * @param messageId 消息ID
     * @return 引用文档列表
     */
    List<ConversationMessageSource> findByMessageId(Long messageId);

    /**
     * 根据会话ID查询引用文档列表
     *
     * @param conversationId 会话ID
     * @return 引用文档列表
     */
    List<ConversationMessageSource> findByConversationId(Long conversationId);

    /**
     * 根据消息ID删除引用文档
     *
     * @param messageId 消息ID
     */
    void deleteByMessageId(Long messageId);
}
