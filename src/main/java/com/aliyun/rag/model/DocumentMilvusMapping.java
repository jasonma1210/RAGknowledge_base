package com.aliyun.rag.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 文档与Milvus向量ID映射模型
 * <p>
 * 用于存储文件记录ID与Milvus中多个向量ID的映射关系
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-11
 */
@Entity
@Table(name = "document_milvus_mapping")
public class DocumentMilvusMapping {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文件记录ID（对应user_file_record表的id）
     */
    @Column(name = "file_record_id", nullable = false)
    private Long fileRecordId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Milvus向量ID
     */
    @Column(name = "milvus_id", nullable = false, length = 100)
    private String milvusId;

    /**
     * 向量索引（在文档中的位置）
     */
    @Column(name = "vector_index", nullable = false)
    private Integer vectorIndex;

    /**
     * 创建时间
     */
    @Column(name = "gmt_create", nullable = false, updatable = false)
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    @Column(name = "gmt_modified", nullable = false)
    private LocalDateTime gmtModified;

    /**
     * 是否删除（0:未删除 1:已删除）
     */
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileRecordId() {
        return fileRecordId;
    }

    public void setFileRecordId(Long fileRecordId) {
        this.fileRecordId = fileRecordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMilvusId() {
        return milvusId;
    }

    public void setMilvusId(String milvusId) {
        this.milvusId = milvusId;
    }

    public Integer getVectorIndex() {
        return vectorIndex;
    }

    public void setVectorIndex(Integer vectorIndex) {
        this.vectorIndex = vectorIndex;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}