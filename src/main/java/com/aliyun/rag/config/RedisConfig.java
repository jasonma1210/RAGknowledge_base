package com.aliyun.rag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * <p>
 * 配置RedisTemplate用于存储和访问用户令牌
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-16
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置key的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        
        // 设置value的序列化方式
        template.setValueSerializer(new StringRedisSerializer());
        
        return template;
    }
}