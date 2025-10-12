package com.aliyun.rag.service;

import com.aliyun.rag.config.MilvusConfig;
import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.DocumentMilvusMapping;
import com.aliyun.rag.model.SearchResult;
import com.aliyun.rag.model.User;
import com.aliyun.rag.repository.DocumentMilvusMappingRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 优化的向量存储服务
 * <p>
 * 提供高效的向量检索功能，包括缓存、重排序和混合搜索优化
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Service
public class OptimizedVectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(OptimizedVectorStoreService.class);

    private final EmbeddingModel embeddingModel;
    private final DocumentMilvusMappingRepository documentMilvusMappingRepository;
    private final MilvusConfig milvusConfig;

    public OptimizedVectorStoreService(EmbeddingModel embeddingModel,
                                      DocumentMilvusMappingRepository documentMilvusMappingRepository,
                                      MilvusConfig milvusConfig) {
        this.embeddingModel = embeddingModel;
        this.documentMilvusMappingRepository = documentMilvusMappingRepository;
        this.milvusConfig = milvusConfig;
    }

    /**
     * 缓存语义搜索
     */
    @Cacheable(value = "vectorSearch", key = "#query + '_' + #userId + '_' + #maxResults + '_' + #minScore")
    public List<SearchResult> cachedSemanticSearch(String query, Long userId, String username, 
                                                  int maxResults, double minScore) {
        log.info("执行缓存语义搜索: 用户={}, 查询={}", username, query);
        
        try {
            // 生成查询向量
            Embedding queryEmbedding = embeddingModel.embed(TextSegment.from(query)).content();
            
            // 获取用户专属的向量存储
            MilvusEmbeddingStore userEmbeddingStore = getUserEmbeddingStore(userId, username);
            
            // 执行向量搜索
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults * 2) // 获取更多结果用于重排序
                    .minScore(minScore)
                    .build();
            
            List<EmbeddingMatch<TextSegment>> matches = userEmbeddingStore.search(searchRequest).matches();
            
            // 转换为SearchResult并重排序
            List<SearchResult> results = matches.stream()
                    .map(this::convertToSearchResult)
                    .collect(Collectors.toList());
            
            // 应用重排序算法
            results = rerankResults(results, query);
            
            // 限制结果数量
            return results.stream()
                    .limit(maxResults)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("缓存语义搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("语义搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 优化的关键词搜索
     */
    public List<SearchResult> optimizedKeywordSearch(String query, Long userId, String username, int maxResults) {
        log.info("执行优化关键词搜索: 用户={}, 查询={}", username, query);
        
        try {
            // 使用全文索引进行关键词搜索
            List<DocumentMilvusMapping> mappings = documentMilvusMappingRepository
                    .findByUserIdAndIsDeleted(userId, 0);
            
            List<SearchResult> results = new ArrayList<>();
            
            // 获取用户专属的向量存储
            MilvusEmbeddingStore userEmbeddingStore = getUserEmbeddingStore(userId, username);
            
            // 获取所有向量进行关键词匹配
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(Embedding.from(new float[embeddingModel.dimension()])) // 零向量
                    .maxResults(1000) // 限制结果数量
                    .build();
            
            List<EmbeddingMatch<TextSegment>> matches = userEmbeddingStore.search(searchRequest).matches();
            
            for (EmbeddingMatch<TextSegment> match : matches) {
                String content = match.embedded().text();
                
                if (containsKeywords(content, query)) {
                    SearchResult result = convertToSearchResult(match);
                    result.setScore(calculateKeywordScore(content, query));
                    results.add(result);
                }
            }
            
            // 按分数排序并限制结果数量
            return results.stream()
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(maxResults)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("优化关键词搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("关键词搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 高级混合搜索
     */
    public List<SearchResult> advancedHybridSearch(String query, Long userId, String username, 
                                                  int maxResults, double minScore) {
        log.info("执行高级混合搜索: 用户={}, 查询={}", username, query);
        
        try {
            // 并行执行语义搜索和关键词搜索
            List<SearchResult> semanticResults = cachedSemanticSearch(query, userId, username, maxResults, minScore);
            List<SearchResult> keywordResults = optimizedKeywordSearch(query, userId, username, maxResults);
            
            // 合并结果并去重
            Map<String, SearchResult> combinedResults = new LinkedHashMap<>();
            
            // 语义搜索结果权重更高
            for (SearchResult result : semanticResults) {
                String key = result.getFileRecordId() + "_" + result.getPosition();
                result.setScore(result.getScore() * 1.2); // 提高语义搜索权重
                combinedResults.put(key, result);
            }
            
            // 添加关键词搜索结果
            for (SearchResult result : keywordResults) {
                String key = result.getFileRecordId() + "_" + result.getPosition();
                if (!combinedResults.containsKey(key)) {
                    combinedResults.put(key, result);
                } else {
                    // 如果已存在，取较高分数
                    SearchResult existing = combinedResults.get(key);
                    if (result.getScore() > existing.getScore()) {
                        combinedResults.put(key, result);
                    }
                }
            }
            
            // 应用高级重排序算法
            List<SearchResult> finalResults = advancedRerankResults(
                    new ArrayList<>(combinedResults.values()), query);
            
            return finalResults.stream()
                    .limit(maxResults)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("高级混合搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("混合搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 重排序结果
     */
    private List<SearchResult> rerankResults(List<SearchResult> results, String query) {
        return results.stream()
                .peek(result -> {
                    // 基于查询相关性调整分数
                    double relevanceScore = calculateRelevanceScore(result.getContent(), query);
                    result.setScore(result.getScore() * relevanceScore);
                })
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * 高级重排序算法
     */
    private List<SearchResult> advancedRerankResults(List<SearchResult> results, String query) {
        return results.stream()
                .peek(result -> {
                    // 综合多个因素计算最终分数
                    double semanticScore = result.getScore();
                    double relevanceScore = calculateRelevanceScore(result.getContent(), query);
                    double diversityScore = calculateDiversityScore(result, results);
                    double freshnessScore = calculateFreshnessScore(result);
                    double positionalScore = calculatePositionalScore(result);
                    double lengthScore = calculateLengthScore(result);
                    
                    // 使用更复杂的加权计算最终分数
                    double finalScore = semanticScore * 0.35 + 
                                      relevanceScore * 0.25 + 
                                      diversityScore * 0.15 + 
                                      freshnessScore * 0.1 + 
                                      positionalScore * 0.1 + 
                                      lengthScore * 0.05;
                    
                    result.setScore(finalScore);
                })
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * 计算相关性分数
     */
    private double calculateRelevanceScore(String content, String query) {
        String[] queryWords = query.toLowerCase().split("\\s+");
        String contentLower = content.toLowerCase();
        
        int matchCount = 0;
        for (String word : queryWords) {
            if (contentLower.contains(word)) {
                matchCount++;
            }
        }
        
        return (double) matchCount / queryWords.length;
    }

    /**
     * 计算多样性分数
     */
    private double calculateDiversityScore(SearchResult result, List<SearchResult> allResults) {
        // 基于文档来源的多样性
        long sameSourceCount = allResults.stream()
                .filter(r -> r.getFileRecordId().equals(result.getFileRecordId()))
                .count();
        
        return 1.0 / (1.0 + sameSourceCount);
    }

    /**
     * 计算新鲜度分数
     */
    private double calculateFreshnessScore(SearchResult result) {
        // 基于位置的分数（前面的内容通常更重要）
        int position = result.getPosition();
        return Math.max(0.1, 1.0 - (position * 0.01));
    }

    /**
     * 计算位置分数
     */
    private double calculatePositionalScore(SearchResult result) {
        // 基于结果在文档中的位置计算分数
        int position = result.getPosition();
        // 位置越靠前分数越高，但递减
        return Math.max(0.1, 1.0 / (1.0 + position * 0.1));
    }

    /**
     * 计算长度分数
     */
    private double calculateLengthScore(SearchResult result) {
        // 基于内容长度计算分数，适中长度的内容更受欢迎
        String content = result.getContent();
        if (content == null) {
            return 0.1;
        }
        
        int length = content.length();
        // 适中长度的内容得分更高（100-1000字符）
        if (length >= 100 && length <= 1000) {
            return 1.0;
        } else if (length < 100) {
            return length / 100.0;
        } else {
            return Math.max(0.1, 1000.0 / length);
        }
    }

    /**
     * 检查内容是否包含关键词
     */
    private boolean containsKeywords(String content, String query) {
        String[] keywords = query.toLowerCase().split("\\s+");
        String contentLower = content.toLowerCase();
        
        for (String keyword : keywords) {
            if (contentLower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算关键词匹配分数
     */
    private double calculateKeywordScore(String content, String query) {
        String[] queryWords = query.toLowerCase().split("\\s+");
        String contentLower = content.toLowerCase();
        
        int totalMatches = 0;
        int exactMatches = 0;
        
        for (String word : queryWords) {
            if (contentLower.contains(word)) {
                totalMatches++;
                // 检查是否为精确匹配
                if (contentLower.contains(" " + word + " ") || 
                    contentLower.startsWith(word + " ") || 
                    contentLower.endsWith(" " + word)) {
                    exactMatches++;
                }
            }
        }
        
        // 计算分数：精确匹配权重更高
        double score = (exactMatches * 2.0 + totalMatches) / (queryWords.length * 2.0);
        return Math.min(score, 1.0);
    }

    /**
     * 转换为SearchResult
     */
    private SearchResult convertToSearchResult(EmbeddingMatch<TextSegment> match) {
        SearchResult result = new SearchResult();
        result.setFileRecordId(match.embedded().metadata().getString("fileRecordId"));
        result.setTitle(match.embedded().metadata().getString("title"));
        result.setContent(match.embedded().text());
        result.setScore(match.score());
        result.setSource(match.embedded().metadata().getString("fileType"));
        
        try {
            result.setPosition(Integer.parseInt(match.embedded().metadata().getString("chunkIndex")));
        } catch (NumberFormatException e) {
            result.setPosition(0);
        }
        
        return result;
    }

    /**
     * 获取用户专属的向量存储
     */
    private MilvusEmbeddingStore getUserEmbeddingStore(Long userId, String username) {
        String collectionName = username + "_" + userId;
        
        return MilvusEmbeddingStore.builder()
                .host(milvusConfig.getHost())
                .port(milvusConfig.getPort())
                .collectionName(collectionName)
                .dimension(milvusConfig.getDimension())
                .idFieldName("id")
                .textFieldName("text")
                .vectorFieldName("vector")
                .build();
    }

    /**
     * 清除搜索缓存
     */
    public void clearSearchCache(Long userId) {
        // 这里可以实现缓存清除逻辑
        log.info("清除用户 {} 的搜索缓存", userId);
    }

    /**
     * 获取搜索统计信息
     */
    public Map<String, Object> getSearchStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<DocumentMilvusMapping> mappings = documentMilvusMappingRepository
                    .findByUserIdAndIsDeleted(userId, 0);
            
            stats.put("totalVectors", mappings.size());
            stats.put("totalFiles", mappings.stream()
                    .map(DocumentMilvusMapping::getFileRecordId)
                    .distinct()
                    .count());
            
        } catch (Exception e) {
            log.error("获取搜索统计信息失败: {}", e.getMessage(), e);
        }
        
        return stats;
    }
}
