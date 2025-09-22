package com.aliyun.rag.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置类
 * <p>
 * 配置Redis缓存管理器，为VectorStoreService等服务提供缓存支持
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
public class CacheConfig {

    /**
     * 配置Redis缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 默认TTL 1小时
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        // 针对不同缓存名称的特定配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 文档映射缓存 - 30分钟TTL
        cacheConfigurations.put("documentMappings", defaultCacheConfig
                .entryTtl(Duration.ofMinutes(30)));
        
        // 用户向量统计缓存 - 5分钟TTL（更新频繁）
        cacheConfigurations.put("userVectorStats", defaultCacheConfig
                .entryTtl(Duration.ofMinutes(5)));
        
        // 向量数据缓存 - 10分钟TTL
        cacheConfigurations.put("vectorData", defaultCacheConfig
                .entryTtl(Duration.ofMinutes(10)));
        
        // 用户向量数量缓存 - 10分钟TTL
        cacheConfigurations.put("vectorCount", defaultCacheConfig
                .entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}