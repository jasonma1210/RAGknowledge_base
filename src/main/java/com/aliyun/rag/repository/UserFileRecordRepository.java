package com.aliyun.rag.repository;

import com.aliyun.rag.model.UserFileRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户文件上传记录Repository
 * <p>
 * 提供用户文件上传记录的数据库操作接口
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-11
 */
@Repository
public interface UserFileRecordRepository extends JpaRepository<UserFileRecord, Long> {

    /**
     * 根据用户ID查找未删除的文件记录
     *
     * @param userId 用户ID
     * @param isDeleted 是否删除
     * @return 文件记录列表
     */
    List<UserFileRecord> findByUserIdAndIsDeleted(Long userId, Integer isDeleted);

    /**
     * 根据用户ID查找未删除的文件记录（分页）
     *
     * @param userId 用户ID
     * @param isDeleted 是否删除
     * @param pageable 分页参数
     * @return 文件记录分页结果
     */
    Page<UserFileRecord> findByUserIdAndIsDeleted(Long userId, Integer isDeleted, Pageable pageable);

    /**
     * 根据用户ID和文件名查找未删除的文件记录
     *
     * @param userId 用户ID
     * @param fileName 文件名
     * @param isDeleted 是否删除
     * @return 文件记录
     */
    Optional<UserFileRecord> findByUserIdAndFileNameAndIsDeleted(Long userId, String fileName, Integer isDeleted);

    /**
     * 根据ID和用户ID查找未删除的文件记录
     *
     * @param id 记录ID
     * @param userId 用户ID
     * @param isDeleted 是否删除
     * @return 文件记录
     */
    Optional<UserFileRecord> findByIdAndUserIdAndIsDeleted(Long id, Long userId, Integer isDeleted);

    /**
     * 根据ID查找未删除的文件记录
     *
     * @param id 记录ID
     * @param isDeleted 是否删除
     * @return 文件记录
     */
    Optional<UserFileRecord> findByIdAndIsDeleted(Long id, Integer isDeleted);
}