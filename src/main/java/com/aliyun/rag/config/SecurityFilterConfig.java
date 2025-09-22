package com.aliyun.rag.config;

import com.aliyun.rag.filter.XSSFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 安全过滤器配置
 * <p>
 * 配置各种安全过滤器，包括XSS防护等
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
@Configuration
public class SecurityFilterConfig {

    /**
     * 注册XSS防护过滤器
     */
    @Bean
    public FilterRegistrationBean<XSSFilter> xssFilterRegistration() {
        FilterRegistrationBean<XSSFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XSSFilter());
        
        // 设置过滤器的URL模式
        registration.addUrlPatterns("/*");
        
        // 设置过滤器优先级（数值越小优先级越高）
        registration.setOrder(1);
        
        // 设置过滤器名称
        registration.setName("XSSFilter");
        
        return registration;
    }
}