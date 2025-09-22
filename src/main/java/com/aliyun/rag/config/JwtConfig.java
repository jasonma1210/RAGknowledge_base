package com.aliyun.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

/**
 * JWT配置属性
 * <p>
 * 用于配置JWT Token的相关参数
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT密钥
     */
    @NotBlank(message = "JWT密钥不能为空")
    private String secret = "RAGKnowledgeBaseDefaultSecretKeyForJWTTokenGenerationAndValidation2025";

    /**
     * Token过期时间（毫秒）
     */
    @Min(value = 3600000, message = "Token过期时间不能少于1小时")
    private Long expiration = 86400000L; // 24小时

    /**
     * Token刷新时间（毫秒）
     */
    @Min(value = 1800000, message = "Token刷新时间不能少于30分钟")
    private Long refreshExpiration = 604800000L; // 7天

    /**
     * Token前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Token头部名称
     */
    private String headerName = "Authorization";

    /**
     * 签发者
     */
    private String issuer = "RAG-Knowledge-Base";

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}