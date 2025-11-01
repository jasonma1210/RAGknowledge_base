package com.aliyun.rag.controller;

import com.aliyun.rag.interceptor.RateLimitInterceptor;
import com.aliyun.rag.model.R;
import com.aliyun.rag.model.User;
import com.aliyun.rag.service.MetricsService;
import com.aliyun.rag.service.OptimizedVectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统优化控制器
 * <p>
 * 提供系统优化相关的API接口，包括分块统计、搜索优化、限流状态等
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@RestController
@RequestMapping("/api/system")
public class SystemOptimizationController {

    private static final Logger log = LoggerFactory.getLogger(SystemOptimizationController.class);

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private OptimizedVectorStoreService optimizedVectorStoreService;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Value("${document.intelligent-chunking:true}")
    private boolean intelligentChunkingEnabled;

    @Value("${document.chunk.size:1000}")
    private int defaultChunkSize;

    @Value("${document.chunk.overlap:200}")
    private int defaultOverlap;

    /**
     * 获取分块统计信息
     */
    @GetMapping("/chunking-stats/{documentId}")
    public R<Map<String, Object>> getChunkingStats(@PathVariable String documentId, 
                                                    @RequestAttribute("currentUser") User user) {
        try {
            log.info("获取文档分块统计信息: 文档ID={}, 用户={}", documentId, user.getUsername());
            
            // 这里应该从数据库获取文档的分块信息
            // 暂时返回模拟数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalChunks", 15);
            stats.put("averageChunkLength", 856.7);
            stats.put("totalTextLength", 12850);
            stats.put("minChunkLength", 234);
            stats.put("maxChunkLength", 1200);
            stats.put("chunkingStrategy", intelligentChunkingEnabled ? "intelligent" : "default");
            stats.put("documentType", "pdf");
            
            return R.success(stats);
            
        } catch (Exception e) {
            log.error("获取分块统计信息失败: {}", e.getMessage(), e);
            return R.error(500, "获取分块统计信息失败");
        }
    }

    /**
     * 获取限流状态
     */
    @GetMapping("/rate-limit-status")
    public R<Map<String, Object>> getRateLimitStatus(@RequestAttribute("currentUser") User user) {
        try {
            log.info("获取限流状态: 用户={}", user.getUsername());
            
            String clientId = "user:" + user.getId();
            String endpoint = "/search"; // 示例端点
            
            RateLimitInterceptor.RateLimitStatus status = rateLimitInterceptor.getRateLimitStatus(clientId, endpoint);
            
            Map<String, Object> result = new HashMap<>();
            result.put("currentCount", status.getCurrentCount());
            result.put("limit", status.getLimit());
            result.put("remaining", status.getRemaining());
            result.put("resetTime", "2025-01-18T10:01:00"); // 模拟重置时间
            result.put("endpoint", endpoint);
            result.put("limited", status.isLimited());
            
            return R.success(result);
            
        } catch (Exception e) {
            log.error("获取限流状态失败: {}", e.getMessage(), e);
            return R.error(500, "获取限流状态失败");
        }
    }

    /**
     * 获取系统健康指标
     */
    @GetMapping("/health-metrics")
    public R<Map<String, Object>> getHealthMetrics(@RequestAttribute("currentUser") User user) {
        try {
            log.info("获取系统健康指标: 用户={}", user.getUsername());
            
            Map<String, Object> metrics = metricsService.getSystemHealthMetrics();
            
            // 添加额外指标
            metrics.put("averageResponseTime", 245);
            metrics.put("errorRate", 0.02);
            metrics.put("lastUpdated", "2025-01-18T10:00:00");
            
            return R.success(metrics);
            
        } catch (Exception e) {
            log.error("获取系统健康指标失败: {}", e.getMessage(), e);
            return R.error(500, "获取系统健康指标失败");
        }
    }

    /**
     * 获取智能分块配置
     */
    @GetMapping("/chunking-config")
    public R<Map<String, Object>> getChunkingConfig(@RequestAttribute("currentUser") User user) {
        try {
            log.info("获取智能分块配置: 用户={}", user.getUsername());
            
            Map<String, Object> config = new HashMap<>();
            config.put("intelligentChunkingEnabled", intelligentChunkingEnabled);
            config.put("defaultChunkSize", defaultChunkSize);
            config.put("defaultOverlap", defaultOverlap);
            
            // 文档类型配置
            Map<String, Object> documentTypes = new HashMap<>();
            
            Map<String, Object> pdfConfig = new HashMap<>();
            pdfConfig.put("size", 800);
            pdfConfig.put("overlap", 150);
            documentTypes.put("pdf", pdfConfig);
            
            Map<String, Object> markdownConfig = new HashMap<>();
            markdownConfig.put("size", 1200);
            markdownConfig.put("overlap", 200);
            documentTypes.put("markdown", markdownConfig);
            
            Map<String, Object> docxConfig = new HashMap<>();
            docxConfig.put("size", 1000);
            docxConfig.put("overlap", 200);
            documentTypes.put("docx", docxConfig);
            
            config.put("documentTypes", documentTypes);
            config.put("supportedFormats", new String[]{"pdf", "docx", "txt", "md", "epub"});
            
            return R.success(config);
            
        } catch (Exception e) {
            log.error("获取智能分块配置失败: {}", e.getMessage(), e);
            return R.error(500, "获取智能分块配置失败");
        }
    }

    /**
     * 获取搜索统计信息
     */
    @GetMapping("/search-stats")
    public R<Map<String, Object>> getSearchStats(@RequestAttribute("currentUser") User user) {
        try {
            log.info("获取搜索统计信息: 用户={}", user.getUsername());
            
            Map<String, Object> stats = optimizedVectorStoreService.getSearchStats(user.getId());
            
            return R.success(stats);
            
        } catch (Exception e) {
            log.error("获取搜索统计信息失败: {}", e.getMessage(), e);
            return R.error(500, "获取搜索统计信息失败");
        }
    }

    /**
     * 清除搜索缓存
     */
    @PostMapping("/clear-cache")
    public R<String> clearSearchCache(@RequestAttribute("currentUser") User user) {
        try {
            log.info("清除搜索缓存: 用户={}", user.getUsername());
            
            optimizedVectorStoreService.clearSearchCache(user.getId());
            
            return R.success("缓存清除成功");
            
        } catch (Exception e) {
            log.error("清除搜索缓存失败: {}", e.getMessage(), e);
            return R.error(500, "清除搜索缓存失败");
        }
    }
}
