package com.aliyun.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户级别配置类
 * <p>
 * 集中管理用户等级相关的配置参数，包括存储配额、权限等
 * 替代硬编码在实体类中的配置
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
@Component
@Configuration
@ConfigurationProperties(prefix = "user.levels")
public class UserLevelProperties {

    /**
     * 普通用户配置
     */
    private UserLevelConfig basic = new UserLevelConfig(0, 5L * 1024 * 1024 * 1024); // 5GB

    /**
     * 进阶用户配置
     */
    private UserLevelConfig advanced = new UserLevelConfig(1, 100L * 1024 * 1024 * 1024); // 100GB

    public UserLevelConfig getBasic() {
        return basic;
    }

    public void setBasic(UserLevelConfig basic) {
        this.basic = basic;
    }

    public UserLevelConfig getAdvanced() {
        return advanced;
    }

    public void setAdvanced(UserLevelConfig advanced) {
        this.advanced = advanced;
    }

    /**
     * 根据级别代码获取配置
     */
    public UserLevelConfig getConfigByCode(int code) {
        return switch (code) {
            case 0 -> basic;
            case 1 -> advanced;
            default -> throw new IllegalArgumentException("Unknown user level code: " + code);
        };
    }

    /**
     * 获取所有级别配置的映射
     */
    public Map<Integer, UserLevelConfig> getAllConfigs() {
        Map<Integer, UserLevelConfig> configs = new HashMap<>();
        configs.put(basic.getCode(), basic);
        configs.put(advanced.getCode(), advanced);
        return configs;
    }

    /**
     * 用户等级配置
     */
    public static class UserLevelConfig {
        
        /**
         * 等级代码
         */
        private int code;
        
        /**
         * 存储配额（字节）
         */
        @Min(value = 1024, message = "存储配额不能小于1KB")
        private Long quota;

        public UserLevelConfig() {
        }

        public UserLevelConfig(int code, Long quota) {
            this.code = code;
            this.quota = quota;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public Long getQuota() {
            return quota;
        }

        public void setQuota(Long quota) {
            this.quota = quota;
        }

        /**
         * 格式化存储配额显示
         */
        public String getQuotaDisplayString() {
            if (quota >= 1024 * 1024 * 1024) {
                return (quota / (1024 * 1024 * 1024)) + "GB";
            } else if (quota >= 1024 * 1024) {
                return (quota / (1024 * 1024)) + "MB";
            } else if (quota >= 1024) {
                return (quota / 1024) + "KB";
            } else {
                return quota + "B";
            }
        }
    }
}