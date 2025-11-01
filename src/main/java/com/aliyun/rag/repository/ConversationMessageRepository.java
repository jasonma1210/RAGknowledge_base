package com.aliyun.rag.repository;

import com.aliyun.rag.model.ConversationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会话消息数据访问层
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    /**
     * 根据会话ID和删除状态查询消息列表（按时间升序）
     *
     * @param conversationId 会话ID
     * @param isDeleted      是否删除
     * @return 消息列表
     */
    List<ConversationMessage> findByConversationIdAndIsDeletedOrderByGmtCreateAsc(
            Long conversationId, Integer isDeleted);

    /**
     * 根据会话ID和删除状态分页查询消息列表
     *
     * @param conversationId 会话ID
     * @param isDeleted      是否删除
     * @param pageable       分页参数
     * @return 消息分页列表
     */
    Page<ConversationMessage> findByConversationIdAndIsDeletedOrderByGmtCreateAsc(
            Long conversationId, Integer isDeleted, Pageable pageable);

    /**
     * 统计会话的消息数量
     *
     * @param conversationId 会话ID
     * @param isDeleted      是否删除
     * @return 消息数量
     */
    long countByConversationIdAndIsDeleted(Long conversationId, Integer isDeleted);

    /**
     * 查询会话的最近N条消息（用于上下文记忆）
     *
     * @param conversationId 会话ID
     * @param isDeleted      是否删除
     * @param limit          限制数量
     * @return 消息列表
     */
    @Query(value = "SELECT * FROM conversation_message " +
            "WHERE conversation_id = :conversationId AND is_deleted = :isDeleted " +
            "ORDER BY gmt_create DESC LIMIT :limit",
            nativeQuery = true)
    List<ConversationMessage> findRecentMessages(
            @Param("conversationId") Long conversationId,
            @Param("isDeleted") Integer isDeleted,
            @Param("limit") Integer limit);

    /**
     * 根据用户ID和删除状态查询所有消息
     *
     * @param userId    用户ID
     * @param isDeleted 是否删除
     * @return 消息列表
     */
    List<ConversationMessage> findByUserIdAndIsDeletedOrderByGmtCreateDesc(
            Long userId, Integer isDeleted);

    /**
     * 删除会话的所有消息（软删除）
     *
     * @param conversationId 会话ID
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE ConversationMessage m SET m.isDeleted = 1, m.gmtModified = CURRENT_TIMESTAMP " +
            "WHERE m.conversationId = :conversationId")
    void softDeleteByConversationId(@Param("conversationId") Long conversationId);
}
