package com.aliyun.rag.repository;

import com.aliyun.rag.model.DocumentMilvusMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文档与Milvus向量ID映射Repository
 * <p>
 * 提供文档与Milvus向量ID映射关系的数据库操作接口
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-11
 */
@Repository
public interface DocumentMilvusMappingRepository extends JpaRepository<DocumentMilvusMapping, Long> {
    
    /**
     * 根据文件记录ID查找未删除的映射记录
     * 
     * @param fileRecordId 文件记录ID
     * @param isDeleted 是否删除
     * @return 映射记录列表
     */
    List<DocumentMilvusMapping> findByFileRecordIdAndIsDeleted(Long fileRecordId, Integer isDeleted);
    
    /**
     * 根据Milvus ID查找未删除的映射记录
     * 
     * @param milvusId Milvus ID
     * @param isDeleted 是否删除
     * @return 映射记录
     */
    Optional<DocumentMilvusMapping> findByMilvusIdAndIsDeleted(String milvusId, Integer isDeleted);
    
    /**
     * 根据文件记录ID删除映射记录
     * 
     * @param fileRecordId 文件记录ID
     * @param isDeleted 是否删除
     * @return 删除的记录数量
     */
    int deleteByFileRecordIdAndIsDeleted(Long fileRecordId, Integer isDeleted);
}