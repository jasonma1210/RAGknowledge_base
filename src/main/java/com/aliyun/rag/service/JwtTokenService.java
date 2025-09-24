package com.aliyun.rag.service;

import com.aliyun.rag.config.JwtConfig;
import com.aliyun.rag.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token服务
 * <p>
 * 提供JWT Token的生成、验证、解析等功能
 * 支持Access Token和Refresh Token
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtTokenService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        // 使用配置的密钥生成SecretKey
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Access Token
     *
     * @param user 用户信息
     * @return JWT Token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("level", user.getLevel());
        claims.put("email", user.getEmail());
        claims.put("tokenType", "ACCESS");

        return createToken(claims, user.getUsername(), jwtConfig.getExpiration());
    }

    /**
     * 生成Refresh Token
     *
     * @param user 用户信息
     * @return Refresh Token
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("tokenType", "REFRESH");

        return createToken(claims, user.getUsername(), jwtConfig.getRefreshExpiration());
    }

    /**
     * 创建Token
     *
     * @param claims 声明
     * @param subject 主题
     * @param expiration 过期时间
     * @return JWT Token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证Token有效性
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            log.error("获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户等级
     *
     * @param token JWT Token
     * @return 用户等级
     */
    public Integer getUserLevelFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("level", Integer.class);
        } catch (Exception e) {
            log.error("获取用户等级失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查Token是否即将过期（剩余时间少于1小时）
     *
     * @param token JWT Token
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long timeUntilExpiration = expiration.getTime() - now.getTime();
            
            // 如果剩余时间少于1小时，认为即将过期
            return timeUntilExpiration < 3600000; // 1小时 = 3600000毫秒
        } catch (Exception e) {
            log.error("检查Token过期时间失败: {}", e.getMessage());
            return true; // 如果解析失败，认为已过期
        }
    }

    /**
     * 解析Token获取Claims
     *
     * @param token JWT Token
     * @return Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取Token类型
     *
     * @param token JWT Token
     * @return Token类型
     */
    public String getTokenType(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("tokenType", String.class);
        } catch (Exception e) {
            log.error("获取Token类型失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 刷新Access Token
     * 
     * @param refreshToken 刷新令牌
     * @param user 用户信息
     * @return 新的Access Token
     */
    public String refreshAccessToken(String refreshToken, User user) {
        // 验证刷新令牌有效性
        if (!validateToken(refreshToken)) {
            throw new IllegalArgumentException("无效的刷新令牌");
        }
        
        // 检查Token类型是否为REFRESH
        String tokenType = getTokenType(refreshToken);
        if (!"REFRESH".equals(tokenType)) {
            throw new IllegalArgumentException("令牌类型不正确");
        }
        
        // 生成新的Access Token
        return generateAccessToken(user);
    }
}