package com.aliyun.rag.config;

import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus向量数据库配置
 * 
 * @author Jason Ma
 * @version 1.0.0
 */
@Configuration
public class MilvusConfig {

    @Value("${milvus.host:localhost}")
    private String host;

    @Value("${milvus.port:19530}")
    private Integer port;

    @Value("${milvus.collection.dimension}")
    private Integer dimension;

    /**
     * 为指定用户创建MilvusEmbeddingStore实例
     * 每个用户使用独立的collection实现数据隔离
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return MilvusEmbeddingStore实例
     */
    public MilvusEmbeddingStore createUserEmbeddingStore(Long userId, String username) {
        // 使用用户名和用户ID组合命名collection，确保唯一性
        String collectionName = username + "_" + userId;
        
        return MilvusEmbeddingStore.builder()
                .host(host)
                .port(port)
                .collectionName(collectionName)
                .dimension(dimension)
                .idFieldName("id") // 明确指定ID字段名
                .textFieldName("text") // 明确指定文本字段名
                .vectorFieldName("vector") // 明确指定向量字段名
                .build();
    }
    
    /**
     * 获取Milvus服务器配置
     * 
     * @return Milvus服务器主机地址
     */
    public String getHost() {
        return host;
    }
    
    /**
     * 获取Milvus服务器端口
     * 
     * @return Milvus服务器端口
     */
    public Integer getPort() {
        return port;
    }
    
    /**
     * 获取向量维度
     * 
     * @return 向量维度
     */
    public Integer getDimension() {
        return dimension;
    }
}