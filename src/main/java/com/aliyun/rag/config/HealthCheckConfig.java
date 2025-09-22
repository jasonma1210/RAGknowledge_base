package com.aliyun.rag.config;

import com.aliyun.rag.service.MetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 监控健康检查配置
 * <p>
 * 配置自定义健康检查指标和系统信息
 * 监控数据库连接、Redis连接、外部服务等健康状态
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-19
 */
@Configuration
public class HealthCheckConfig {

    /**
     * 数据库健康检查
     */
    @Bean
    public HealthIndicator databaseHealthIndicator(DataSource dataSource, MetricsService metricsService) {
        return () -> {
            try (Connection connection = dataSource.getConnection()) {
                // 执行简单查询测试连接
                if (connection.isValid(2)) {
                    return Health.up()
                            .withDetail("database", "MySQL")
                            .withDetail("status", "Connected")
                            .withDetail("validationQuery", "SELECT 1")
                            .build();
                } else {
                    return Health.down()
                            .withDetail("database", "MySQL")
                            .withDetail("status", "Connection invalid")
                            .build();
                }
            } catch (Exception e) {
                return Health.down()
                        .withDetail("database", "MySQL")
                        .withDetail("status", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * Redis健康检查
     */
    @Bean
    public HealthIndicator redisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        return () -> {
            try {
                String pong = redisTemplate.getConnectionFactory()
                        .getConnection()
                        .ping();
                        
                if ("PONG".equals(pong)) {
                    return Health.up()
                            .withDetail("redis", "Redis Server")
                            .withDetail("status", "Connected")
                            .withDetail("response", pong)
                            .build();
                } else {
                    return Health.down()
                            .withDetail("redis", "Redis Server")
                            .withDetail("status", "Unexpected response")
                            .withDetail("response", pong)
                            .build();
                }
            } catch (Exception e) {
                return Health.down()
                        .withDetail("redis", "Redis Server")
                        .withDetail("status", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * 存储健康检查
     */
    @Bean
    public HealthIndicator storageHealthIndicator() {
        return () -> {
            try {
                // 检查上传目录是否可写
                java.io.File uploadDir = new java.io.File("uploads");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                if (uploadDir.canWrite()) {
                    long freeSpace = uploadDir.getFreeSpace();
                    long totalSpace = uploadDir.getTotalSpace();
                    double freeSpaceGB = freeSpace / (1024.0 * 1024.0 * 1024.0);
                    double usagePercent = ((double) (totalSpace - freeSpace) / totalSpace) * 100;
                    
                    Health.Builder healthBuilder = Health.up()
                            .withDetail("storage", "Local File System")
                            .withDetail("status", "Available")
                            .withDetail("freeSpaceGB", String.format("%.2f", freeSpaceGB))
                            .withDetail("usagePercent", String.format("%.2f%%", usagePercent));
                    
                    // 如果磁盘空间不足90%，标记为健康，否则为警告
                    if (usagePercent > 90) {
                        return healthBuilder
                                .status("WARNING")
                                .withDetail("warning", "Disk space usage is over 90%")
                                .build();
                    } else {
                        return healthBuilder.build();
                    }
                } else {
                    return Health.down()
                            .withDetail("storage", "Local File System")
                            .withDetail("status", "Not writable")
                            .build();
                }
            } catch (Exception e) {
                return Health.down()
                        .withDetail("storage", "Local File System")
                        .withDetail("status", "Check failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * 应用信息贡献者
     */
    @Bean
    public InfoContributor applicationInfoContributor() {
        return builder -> {
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("name", "RAG Knowledge Base");
            appInfo.put("version", "1.0.0");
            appInfo.put("description", "RAG-based Knowledge Base System");
            appInfo.put("startTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            Map<String, Object> buildInfo = new HashMap<>();
            buildInfo.put("java.version", System.getProperty("java.version"));
            buildInfo.put("java.vendor", System.getProperty("java.vendor"));
            buildInfo.put("os.name", System.getProperty("os.name"));
            buildInfo.put("os.version", System.getProperty("os.version"));
            
            builder.withDetail("application", appInfo);
            builder.withDetail("build", buildInfo);
        };
    }

    /**
     * 系统指标贡献者
     */
    @Bean
    public InfoContributor systemMetricsInfoContributor(MeterRegistry meterRegistry) {
        return builder -> {
            Map<String, Object> systemInfo = new HashMap<>();
            
            // JVM内存信息
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            Map<String, Object> memoryInfo = new HashMap<>();
            memoryInfo.put("maxMemoryMB", maxMemory / (1024 * 1024));
            memoryInfo.put("totalMemoryMB", totalMemory / (1024 * 1024));
            memoryInfo.put("usedMemoryMB", usedMemory / (1024 * 1024));
            memoryInfo.put("freeMemoryMB", freeMemory / (1024 * 1024));
            
            systemInfo.put("memory", memoryInfo);
            systemInfo.put("availableProcessors", runtime.availableProcessors());
            
            builder.withDetail("system", systemInfo);
        };
    }
}