package com.aliyun.rag.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户模型
 * <p>
 * 表示系统中的用户信息，包括基本信息、存储配额和权限等级
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-10
 */
@Entity
@Table(name = "user_info")
public class User {

    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    /**
     * 密码（加密存储）
     */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 用户等级
     */
    @Column(name = "user_level", nullable = false)
    private Integer level;

    /**
     * 存储配额（字节）
     */
    @Column(name = "storage_quota", nullable = false)
    private Long storageQuota;

    /**
     * 已使用存储空间（字节）
     */
    @Column(name = "used_storage", nullable = false)
    private Long usedStorage;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(Long storageQuota) {
        this.storageQuota = storageQuota;
    }

    public Long getUsedStorage() {
        return usedStorage;
    }

    public void setUsedStorage(Long usedStorage) {
        this.usedStorage = usedStorage;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
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

    /**
     * 用户等级枚举
     */
    public enum UserLevel {
        /**
         * 普通用户（5GB存储）
         */
        BASIC(0, 5L * 1024 * 1024 * 1024),

        /**
         * 进阶用户（100GB存储）
         */
        ADVANCED(1, 100L * 1024 * 1024 * 1024);

        private final int code;
        private final Long quota;

        UserLevel(int code, Long quota) {
            this.code = code;
            this.quota = quota;
        }

        public int getCode() {
            return code;
        }

        public Long getQuota() {
            return quota;
        }

        public static UserLevel fromCode(int code) {
            for (UserLevel level : UserLevel.values()) {
                if (level.getCode() == code) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Unknown user level code: " + code);
        }
    }
}