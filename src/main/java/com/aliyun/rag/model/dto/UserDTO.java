package com.aliyun.rag.model.dto;

import com.aliyun.rag.model.User;
import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 * <p>
 * 用于API响应的用户信息，排除敏感字段如密码
 * 遵循数据最小暴露原则
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
public class UserDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱（可能已脱敏）
     */
    private String email;

    /**
     * 用户等级
     */
    private Integer level;

    /**
     * 用户等级描述
     */
    private String levelDescription;

    /**
     * 存储配额（字节）
     */
    private Long storageQuota;

    /**
     * 已使用存储空间（字节）
     */
    private Long usedStorage;

    /**
     * 存储使用率（百分比）
     */
    private Double storageUsagePercentage;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    // Constructor
    public UserDTO() {
    }

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

    public String getLevelDescription() {
        return levelDescription;
    }

    public void setLevelDescription(String levelDescription) {
        this.levelDescription = levelDescription;
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

    public Double getStorageUsagePercentage() {
        return storageUsagePercentage;
    }

    public void setStorageUsagePercentage(Double storageUsagePercentage) {
        this.storageUsagePercentage = storageUsagePercentage;
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

    /**
     * 从User实体转换为UserDTO
     *
     * @param user User实体
     * @return UserDTO
     */
    public static UserDTO fromUser(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setLevel(user.getLevel());
        userDTO.setStorageQuota(user.getStorageQuota());
        userDTO.setUsedStorage(user.getUsedStorage());
        userDTO.setLastLoginTime(user.getLastLoginTime());
        userDTO.setGmtCreate(user.getGmtCreate());

        // 设置等级描述
        userDTO.setLevelDescription(getLevelDescription(user.getLevel()));

        // 计算存储使用率
        if (user.getStorageQuota() != null && user.getStorageQuota() > 0) {
            double percentage = (double) user.getUsedStorage() / user.getStorageQuota() * 100;
            userDTO.setStorageUsagePercentage(Math.round(percentage * 100.0) / 100.0); // 保留两位小数
        } else {
            userDTO.setStorageUsagePercentage(0.0);
        }

        return userDTO;
    }

    /**
     * 获取用户等级描述
     *
     * @param level 用户等级
     * @return 等级描述
     */
    private static String getLevelDescription(Integer level) {
        if (level == null) {
            return "未知";
        }

        return switch (level) {
            case 0 -> "普通用户";
            case 1 -> "进阶用户";
            default -> "未知等级";
        };
    }

    /**
     * 格式化存储空间大小为可读格式
     *
     * @param bytes 字节数
     * @return 格式化后的大小
     */
    public static String formatStorageSize(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 获取格式化的存储配额
     *
     * @return 格式化的存储配额
     */
    public String getFormattedStorageQuota() {
        return formatStorageSize(this.storageQuota);
    }

    /**
     * 获取格式化的已使用存储空间
     *
     * @return 格式化的已使用存储空间
     */
    public String getFormattedUsedStorage() {
        return formatStorageSize(this.usedStorage);
    }

    /**
     * 获取剩余存储空间
     *
     * @return 剩余存储空间（字节）
     */
    public Long getRemainingStorage() {
        if (storageQuota == null || usedStorage == null) {
            return 0L;
        }
        return Math.max(0, storageQuota - usedStorage);
    }

    /**
     * 获取格式化的剩余存储空间
     *
     * @return 格式化的剩余存储空间
     */
    public String getFormattedRemainingStorage() {
        return formatStorageSize(getRemainingStorage());
    }

    /**
     * 检查存储空间是否充足
     *
     * @param requiredSize 需要的空间大小（字节）
     * @return 是否充足
     */
    public boolean hasEnoughStorage(Long requiredSize) {
        if (requiredSize == null || requiredSize <= 0) {
            return true;
        }
        return getRemainingStorage() >= requiredSize;
    }
}