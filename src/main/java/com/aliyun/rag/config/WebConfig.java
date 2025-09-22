package com.aliyun.rag.config;

import com.aliyun.rag.interceptor.AuthInterceptor;
import com.aliyun.rag.interceptor.AccessLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 * <p>
 * 注册拦截器和其他Web配置
 * 集成CORS跨域配置
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final AuthInterceptor authInterceptor;
    private final AccessLogInterceptor accessLogInterceptor;
    private final CorsConfigurationSource corsConfigurationSource;
    
    public WebConfig(AuthInterceptor authInterceptor, 
                    AccessLogInterceptor accessLogInterceptor,
                    CorsConfigurationSource corsConfigurationSource) {
        this.authInterceptor = authInterceptor;
        this.accessLogInterceptor = accessLogInterceptor;
        this.corsConfigurationSource = corsConfigurationSource;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册访问日志拦截器，记录所有请求的访问日志
        registry.addInterceptor(accessLogInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/index.html", "/static/**", "/assets/**", "/favicon.ico")
                .excludePathPatterns("/actuator/health"); // 排除健康检查，避免日志过多
        
        // 注册认证拦截器，拦截所有API请求（除了注册和登录接口）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/register", "/auth/login")  // 只排除注册和登录
                .excludePathPatterns("/api/auth/register", "/api/auth/login")  // 也排除带api前缀的
                .excludePathPatterns("/", "/index.html", "/static/**", "/assets/**", "/favicon.ico")
                .excludePathPatterns("/actuator/**"); // 排除监控端点
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 使用自定义的CORS配置
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}