package com.aliyun.rag.service;

import com.aliyun.rag.config.MilvusConfig;
import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.DocumentMilvusMapping;
import com.aliyun.rag.model.SearchResult;
import com.aliyun.rag.repository.DocumentMilvusMappingRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.CreateCollectionParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量存储服务
 * 
 * @author Jason Ma
 * @version 1.0.0
 */
@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);
    
    private final EmbeddingModel embeddingModel;
    private final DocumentMilvusMappingRepository documentMilvusMappingRepository;
    private final MilvusConfig milvusConfig;

    @Autowired
    public VectorStoreService(EmbeddingModel embeddingModel, 
                             DocumentMilvusMappingRepository documentMilvusMappingRepository,
                             MilvusConfig milvusConfig) {
        this.embeddingModel = embeddingModel;
        this.documentMilvusMappingRepository = documentMilvusMappingRepository;
        this.milvusConfig = milvusConfig;
    }
    
    /**
     * 为指定用户ID和用户名创建MilvusEmbeddingStore实例
     * 确保collection存在，如果不存在则创建
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return MilvusEmbeddingStore实例
     */
    private MilvusEmbeddingStore getUserEmbeddingStore(Long userId, String username) {
        // 生成用户专属collection名称
        String collectionName = username + "_" + userId;
        
        // 检查collection是否存在，如果不存在则创建
        ensureCollectionExists(collectionName);
        
        // 创建并返回MilvusEmbeddingStore实例
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
     * 确保指定的collection存在，如果不存在则创建
     * 
     * @param collectionName collection名称
     */
    private void ensureCollectionExists(String collectionName) {
        try {
            // 创建Milvus客户端连接
            ConnectParam connectParam = ConnectParam.newBuilder()
                    .withHost(milvusConfig.getHost())
                    .withPort(milvusConfig.getPort())
                    .build();
            
            MilvusServiceClient milvusClient = new MilvusServiceClient(connectParam);
            
            // 检查collection是否存在
            R<Boolean> hasCollectionResponse = milvusClient.hasCollection(HasCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build());
            
            if (!hasCollectionResponse.getData()) {
                log.info("Collection {} 不存在，将依赖MilvusEmbeddingStore自动创建", collectionName);
                // 实际的collection创建会在第一次使用MilvusEmbeddingStore时自动完成
            } else {
                log.debug("Collection {} 已存在", collectionName);
            }
            
            // 关闭客户端连接
            milvusClient.close();
        } catch (Exception e) {
            log.error("检查collection {} 失败: {}", collectionName, e.getMessage(), e);
            // 不抛出异常，让MilvusEmbeddingStore处理collection创建
            log.info("将继续依赖MilvusEmbeddingStore自动处理collection创建");
        }
    }

    /**
     * 存储文档的向量表示
     * 
     * @param fileRecordId 文件记录ID
     * @param userId 用户ID
     * @param username 用户名
     * @param chunks 文档分块
     * @param embeddings 向量嵌入
     * @param documentInfo 文档信息
     */
    public void storeDocument(Long fileRecordId, Long userId, String username, String[] chunks, List<Embedding> embeddings, DocumentInfo documentInfo) {
        try {
            log.info("用户 {}({}) 正在存储文档到专属collection", username, userId);
            
            // 为用户创建独立的MilvusEmbeddingStore实例
            MilvusEmbeddingStore userEmbeddingStore = getUserEmbeddingStore(userId, username);
            
            List<DocumentMilvusMapping> mappings = new ArrayList<>();
            
            for (int i = 0; i < chunks.length; i++) {
                TextSegment segment = TextSegment.from(
                    chunks[i],
                    Metadata.from(
                        Map.of(
                            "fileRecordId", fileRecordId.toString(),
                            "title", documentInfo.getTitle() != null ? documentInfo.getTitle() : documentInfo.getFileName(),
                            "fileType", documentInfo.getFileType(),
                            "chunkIndex", String.valueOf(i),
                            "tags", documentInfo.getTags() != null ? documentInfo.getTags() : ""
                        )
                    )
                );
                
                // 存储向量并获取向量ID
                String milvusId = userEmbeddingStore.add(embeddings.get(i), segment);
                
                // 记录文件ID与向量ID的映射关系
                DocumentMilvusMapping mapping = new DocumentMilvusMapping();
                mapping.setFileRecordId(fileRecordId);
                mapping.setMilvusId(milvusId);
                mapping.setVectorIndex(i);
                mapping.setGmtCreate(LocalDateTime.now());
                mapping.setGmtModified(LocalDateTime.now());
                mapping.setIsDeleted(0);
                mappings.add(mapping);
            }
            
            // 批量保存映射关系到数据库
            documentMilvusMappingRepository.saveAll(mappings);
            
            log.info("用户 {}({}) 成功存储文档: {}, 分块数量: {}", username, userId, fileRecordId, chunks.length);
        } catch (Exception e) {
            log.error("用户 {}({}) 存储文档向量失败: {}", username, userId, e.getMessage(), e);
            throw new RuntimeException("存储文档向量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 语义搜索
     * 
     * @param query 查询内容
     * @param queryEmbedding 查询向量
     * @param maxResults 最大结果数
     * @param minScore 最小相似度分数
     * @param userId 用户ID
     * @param username 用户名
     * @return 搜索结果列表
     */
    public List<SearchResult> semanticSearch(String query, Embedding queryEmbedding, int maxResults, double minScore, Long userId, String username) {
        try {
            log.info("用户 {}({}) 正在进行语义搜索", username, userId);
            
            // 为用户创建独立的MilvusEmbeddingStore实例
            MilvusEmbeddingStore userEmbeddingStore = getUserEmbeddingStore(userId, username);
            
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(minScore)
                    .build();

            List<EmbeddingMatch<TextSegment>> matches = userEmbeddingStore.search(searchRequest).matches();
            
            List<SearchResult> results = matches.stream()
                    .map(match -> {
                        SearchResult result = new SearchResult();
                        result.setFileRecordId(match.embedded().metadata().getString("fileRecordId"));
                        result.setTitle(match.embedded().metadata().getString("title"));
                        result.setContent(match.embedded().text());
                        result.setScore(match.score());
                        result.setSource(match.embedded().metadata().getString("fileType"));
                        try {
                            result.setPosition(Integer.parseInt(match.embedded().metadata().getString("chunkIndex")));
                        } catch (NumberFormatException e) {
                            result.setPosition(0); // 默认位置为0
                        }
                        return result;
                    })
                    .collect(Collectors.toList());
            
            log.info("用户 {}({}) 语义搜索完成，返回 {} 条结果", username, userId, results.size());
            return results;
        } catch (Exception e) {
            log.error("用户 {}({}) 语义搜索失败: {}", username, userId, e.getMessage(), e);
            throw new RuntimeException("语义搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 关键词搜索
     * 
     * @param query 查询内容
     * @param maxResults 最大结果数
     * @param userId 用户ID
     * @param username 用户名
     * @return 搜索结果列表
     */
    public List<SearchResult> keywordSearch(String query, int maxResults, Long userId, String username) {
        try {
            log.info("用户 {}({}) 正在进行关键词搜索", username, userId);
            
            // 为用户创建独立的MilvusEmbeddingStore实例
            MilvusEmbeddingStore userEmbeddingStore = getUserEmbeddingStore(userId, username);
            
            // 使用Milvus的元数据过滤功能进行关键词搜索
            List<SearchResult> allMatches = new ArrayList<>();
            
            // 直接使用过滤条件获取所有属于该用户的文档
            // 限制最大结果数量以提高性能
            int maxSearchResults = Math.min(maxResults * 10, 1000); // 最多获取1000个结果或10倍请求数量
            
            for (EmbeddingMatch<TextSegment> match : userEmbeddingStore.search(EmbeddingSearchRequest.builder()
                    .queryEmbedding(Embedding.from(new float[embeddingModel.dimension()])) // 零向量
                    .maxResults(maxSearchResults) // 限制结果数量以提高性能
                    .build()).matches()) {
                
                String content = match.embedded().text();
                String queryOriginal = query;
                
                // 不区分大小写匹配
                if (content.toLowerCase().contains(queryOriginal.toLowerCase())) {
                    SearchResult result = new SearchResult();
                    result.setFileRecordId(match.embedded().metadata().getString("fileRecordId"));
                    result.setTitle(match.embedded().metadata().getString("title"));
                    result.setContent(content);
                    result.setSource(match.embedded().metadata().getString("fileType"));
                    try {
                        result.setPosition(Integer.parseInt(match.embedded().metadata().getString("chunkIndex")));
                    } catch (NumberFormatException e) {
                        result.setPosition(0); // 默认位置为0
                    }
                    
                    // 计算关键词匹配度
                    double score = calculateKeywordScore(content, queryOriginal);
                    result.setScore(score);
                    
                    allMatches.add(result);
                }
            }
            
            // 按匹配度排序并限制结果数量
            List<SearchResult> results = allMatches.stream()
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(maxResults)
                    .collect(Collectors.toList());
                    
            log.info("用户 {}({}) 关键词搜索完成，返回 {} 条结果", username, userId, results.size());
            return results;
        } catch (Exception e) {
            log.error("用户 {}({}) 关键词搜索失败: {}", username, userId, e.getMessage(), e);
            throw new RuntimeException("关键词搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 混合搜索（语义+关键词）
     * 
     * @param query 查询内容
     * @param queryEmbedding 查询向量
     * @param maxResults 最大结果数
     * @param minScore 最小相似度分数
     * @param userId 用户ID
     * @param username 用户名
     * @return 搜索结果列表
     */
    public List<SearchResult> hybridSearch(String query, Embedding queryEmbedding, int maxResults, double minScore, Long userId, String username) {
        try {
            log.info("用户 {}({}) 正在进行混合搜索", username, userId);
            
            // 获取语义搜索结果
            List<SearchResult> semanticResults = semanticSearch(query, queryEmbedding, maxResults, minScore, userId, username);
            
            // 获取关键词搜索结果
            List<SearchResult> keywordResults = keywordSearch(query, maxResults, userId, username);
            
            // 合并结果并去重
            Map<String, SearchResult> combinedResults = new LinkedHashMap<>();
            
            // 优先语义搜索结果
            for (SearchResult result : semanticResults) {
                String key = result.getFileRecordId() + "_" + result.getPosition();
                combinedResults.put(key, result);
            }
            
            // 添加关键词搜索结果
            for (SearchResult result : keywordResults) {
                String key = result.getFileRecordId() + "_" + result.getPosition();
                if (!combinedResults.containsKey(key)) {
                    combinedResults.put(key, result);
                }
            }
            
            // 按分数排序并限制结果数量
            List<SearchResult> results = combinedResults.values().stream()
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(maxResults)
                    .collect(Collectors.toList());
                    
            log.info("用户 {}({}) 混合搜索完成，返回 {} 条结果", username, userId, results.size());
            return results;
        } catch (Exception e) {
            log.error("用户 {}({}) 混合搜索失败: {}", username, userId, e.getMessage(), e);
            throw new RuntimeException("混合搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文档的所有向量
     * 
     * @param fileRecordId 文件记录ID
     * @param userId 用户ID
     * @param username 用户名
     */
    public void deleteDocument(Long fileRecordId, Long userId, String username) {
        try {
            log.info("用户 {}({}) 正在删除文档向量: {}", username, userId, fileRecordId);
            
            // 为用户创建独立的MilvusEmbeddingStore实例
            MilvusEmbeddingStore userEmbeddingStore = getUserEmbeddingStore(userId, username);
            
            // 查找并删除数据库中的映射关系
            List<DocumentMilvusMapping> mappings = documentMilvusMappingRepository.findByFileRecordIdAndIsDeleted(fileRecordId, 0);
            
            // 删除Milvus中的向量数据
            int deletedCount = 0;
            for (DocumentMilvusMapping mapping : mappings) {
                try {
                    // 尝试从Milvus中删除向量
                    userEmbeddingStore.remove(mapping.getMilvusId());
                    log.info("用户 {}({}) 成功删除向量ID: {}", username, userId, mapping.getMilvusId());
                    deletedCount++;
                } catch (Exception e) {
                    // 如果删除失败，只记录日志
                    log.warn("用户 {}({}) 删除向量ID失败: {}, 错误: {}", username, userId, mapping.getMilvusId(), e.getMessage());
                }
            }
            
            // 标记映射关系为已删除
            for (DocumentMilvusMapping mapping : mappings) {
                mapping.setIsDeleted(1);
                mapping.setGmtModified(LocalDateTime.now());
            }
            documentMilvusMappingRepository.saveAll(mappings);
            
            log.info("用户 {}({}) 删除文档向量完成: {}, 共删除 {} 条记录", username, userId, fileRecordId, deletedCount);
        } catch (Exception e) {
            log.error("用户 {}({}) 删除文档向量失败: {}", username, userId, e.getMessage(), e);
            throw new RuntimeException("删除文档向量失败: " + e.getMessage(), e);
        }
    }

    private int countOccurrences(String content, String query) {
        String[] words = query.split("\\s+");
        int count = 0;
        for (String word : words) {
            int index = 0;
            while ((index = content.indexOf(word, index)) != -1) {
                count++;
                index += word.length();
            }
        }
        return count;
    }
    
    /**
     * 计算关键词匹配度分数
     */
    private double calculateKeywordScore(String content, String query) {
        // 基础分数
        double score = 0.5;
        
        // 转为小写进行比较
        String contentLower = content.toLowerCase();
        String queryLower = query.toLowerCase();
        
        // 完全匹配加分
        if (contentLower.contains(queryLower)) {
            score += 0.3;
        }
        
        // 计算词频匹配度
        String[] queryWords = queryLower.split("\\s+");
        int matchCount = 0;
        for (String word : queryWords) {
            if (contentLower.contains(word)) {
                matchCount++;
            }
        }
        
        // 根据匹配词数加分
        if (queryWords.length > 0) {
            score += (double) matchCount / queryWords.length * 0.2;
        }
        
        return Math.min(score, 1.0); // 确保分数不超过1.0
    }
}