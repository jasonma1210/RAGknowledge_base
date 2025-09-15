package com.aliyun.rag.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户文件上传记录模型
 * <p>
 * 用于存储和管理用户上传的文件记录信息
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-11
 */
@Data
@Entity
@Table(name = "user_file_record")
public class UserFileRecord {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 文件路径
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * 文件类型
     */
    @Column(name = "file_type", length = 50)
    private String fileType;

    /**
     * 上传时间
     */
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

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
}