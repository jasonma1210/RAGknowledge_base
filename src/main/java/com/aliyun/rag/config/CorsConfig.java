package com.aliyun.rag.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置
 * <p>
 * 配置CORS策略，控制跨域访问权限
 * 生产环境需要严格限制允许的域名
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:Content-Type,Authorization,X-Requested-With,Accept,Origin}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers:Content-Length,Content-Range}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 设置允许的源（只允许特定域名）
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        // 验证域名格式，避免使用通配符
        for (String origin : origins) {
            if ("*".equals(origin.trim())) {
                throw new IllegalArgumentException("不允许使用通配符 '*' 作为允许的域名，请配置具体的域名");
            }
        }
        configuration.setAllowedOrigins(origins);
        
        // 设置允许的HTTP方法
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        
        // 设置允许的请求头（不使用通配符）
        if ("*".equals(allowedHeaders.trim())) {
            // 选择性允许常用的请求头，而不是全部
            configuration.setAllowedHeaders(Arrays.asList(
                "Content-Type", "Authorization", "X-Requested-With", 
                "Accept", "Origin", "Cache-Control", "Content-Length"
            ));
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            configuration.setAllowedHeaders(headers);
        }
        
        // 设置暴露的响应头
        if (!exposedHeaders.trim().isEmpty()) {
            List<String> exposed = Arrays.asList(exposedHeaders.split(","));
            configuration.setExposedHeaders(exposed);
        }
        
        // 设置是否允许发送Cookie
        configuration.setAllowCredentials(allowCredentials);
        
        // 设置预检请求的缓存时间（限制在1小时内）
        configuration.setMaxAge(Math.min(maxAge, 3600));
        
        // 配置路径映射
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}