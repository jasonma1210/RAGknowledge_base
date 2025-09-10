package com.aliyun.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RAG知识库系统启动类
 * <p>
 * Spring Boot应用入口，启动RAG知识库系统
 * 集成Spring Boot自动配置和组件扫描
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
@SpringBootApplication
public class KnowledgeBaseApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeBaseApplication.class, args);
    }
}