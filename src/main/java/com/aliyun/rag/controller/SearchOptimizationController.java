package com.aliyun.rag.controller;

import com.aliyun.rag.model.R;
import com.aliyun.rag.model.SearchResult;
import com.aliyun.rag.model.User;
import com.aliyun.rag.service.MetricsService;
import com.aliyun.rag.service.OptimizedVectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索优化控制器
 * <p>
 * 提供优化的搜索功能，包括缓存、重排序等高级特性
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@RestController
@RequestMapping("/api/search")
public class SearchOptimizationController {

    private static final Logger log = LoggerFactory.getLogger(SearchOptimizationController.class);

    @Autowired
    private OptimizedVectorStoreService optimizedVectorStoreService;

    @Autowired
    private MetricsService metricsService;

    /**
     * 优化搜索请求
     */
    @PostMapping("/optimized")
    public R<Map<String, Object>> optimizedSearch(@RequestBody OptimizedSearchRequest request,
                                                 @RequestAttribute("currentUser") User user) {
        try {
            log.info("执行优化搜索: 用户={}, 查询={}, 类型={}", 
                    user.getUsername(), request.getQuery(), request.getSearchType());

            long startTime = System.currentTimeMillis();
            List<SearchResult> results;
            boolean cacheHit = false;

            // 根据搜索类型执行不同的搜索策略
            switch (request.getSearchType()) {
                case "SEMANTIC":
                    results = optimizedVectorStoreService.cachedSemanticSearch(
                            request.getQuery(), user.getId(), user.getUsername(),
                            request.getMaxResults(), request.getMinScore());
                    cacheHit = true; // 语义搜索使用缓存
                    break;
                case "KEYWORD":
                    results = optimizedVectorStoreService.optimizedKeywordSearch(
                            request.getQuery(), user.getId(), user.getUsername(), request.getMaxResults());
                    break;
                case "HYBRID":
                    results = optimizedVectorStoreService.advancedHybridSearch(
                            request.getQuery(), user.getId(), user.getUsername(),
                            request.getMaxResults(), request.getMinScore());
                    cacheHit = true; // 混合搜索使用缓存
                    break;
                default:
                    results = optimizedVectorStoreService.cachedSemanticSearch(
                            request.getQuery(), user.getId(), user.getUsername(),
                            request.getMaxResults(), request.getMinScore());
                    cacheHit = true;
            }

            long searchTime = System.currentTimeMillis() - startTime;

            // 记录搜索指标
            metricsService.recordVectorSearchMetrics(request.getSearchType(), searchTime);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            response.put("totalResults", results.size());
            response.put("searchTime", searchTime);
            response.put("cacheHit", cacheHit);
            response.put("rerankApplied", request.isEnableRerank());
            response.put("searchType", request.getSearchType());

            return R.success(response);

        } catch (Exception e) {
            log.error("优化搜索失败: {}", e.getMessage(), e);
            metricsService.recordError("search_error", "optimized_search");
            return R.error(500, "搜索失败: " + e.getMessage());
        }
    }

    /**
     * 优化搜索请求类
     */
    public static class OptimizedSearchRequest {
        private String query;
        private String searchType = "HYBRID";
        private int maxResults = 10;
        private double minScore = 0.7;
        private boolean enableRerank = true;
        private boolean enableCache = true;

        // Getters and Setters
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getSearchType() {
            return searchType;
        }

        public void setSearchType(String searchType) {
            this.searchType = searchType;
        }

        public int getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }

        public double getMinScore() {
            return minScore;
        }

        public void setMinScore(double minScore) {
            this.minScore = minScore;
        }

        public boolean isEnableRerank() {
            return enableRerank;
        }

        public void setEnableRerank(boolean enableRerank) {
            this.enableRerank = enableRerank;
        }

        public boolean isEnableCache() {
            return enableCache;
        }

        public void setEnableCache(boolean enableCache) {
            this.enableCache = enableCache;
        }
    }
}
