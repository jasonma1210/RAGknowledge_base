package com.aliyun.rag.repository;

import com.aliyun.rag.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 会话数据访问层
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * 根据用户ID和删除状态分页查询会话列表
     *
     * @param userId    用户ID
     * @param isDeleted 是否删除
     * @param pageable  分页参数
     * @return 会话分页列表
     */
    Page<Conversation> findByUserIdAndIsDeletedOrderByLastMessageTimeDesc(
            Long userId, Integer isDeleted, Pageable pageable);

    /**
     * 根据用户ID和删除状态查询所有会话
     *
     * @param userId    用户ID
     * @param isDeleted 是否删除
     * @return 会话列表
     */
    List<Conversation> findByUserIdAndIsDeletedOrderByLastMessageTimeDesc(
            Long userId, Integer isDeleted);

    /**
     * 根据ID、用户ID和删除状态查询会话
     *
     * @param id        会话ID
     * @param userId    用户ID
     * @param isDeleted 是否删除
     * @return 会话对象
     */
    Optional<Conversation> findByIdAndUserIdAndIsDeleted(
            Long id, Long userId, Integer isDeleted);

    /**
     * 统计用户的会话数量
     *
     * @param userId    用户ID
     * @param isDeleted 是否删除
     * @return 会话数量
     */
    long countByUserIdAndIsDeleted(Long userId, Integer isDeleted);
}
