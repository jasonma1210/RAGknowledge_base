package com.aliyun.rag.service;

import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.SearchResult;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量存储服务
 * 
 * @author 阿里云RAG团队
 * @version 1.4.0
 */
@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);
    
    private final MilvusEmbeddingStore embeddingStore;
    private final EmbeddingModel embeddingModel;

    @Autowired
    public VectorStoreService(MilvusEmbeddingStore embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    /**
     * 存储文档的向量表示
     */
    public void storeDocument(String documentId, String[] chunks, List<Embedding> embeddings, DocumentInfo documentInfo) {
        try {
            for (int i = 0; i < chunks.length; i++) {
                TextSegment segment = TextSegment.from(
                    chunks[i],
                    Metadata.from(
                        Map.of(
                            "documentId", documentId,
                            "title", documentInfo.getTitle() != null ? documentInfo.getTitle() : documentInfo.getFileName(),
                            "fileType", documentInfo.getFileType(),
                            "chunkIndex", String.valueOf(i),
                            "tags", documentInfo.getTags() != null ? documentInfo.getTags() : ""
                        )
                    )
                );
                
                embeddingStore.add(embeddings.get(i), segment);
            }
            
            log.info("成功存储文档: {}, 分块数量: {}", documentId, chunks.length);
        } catch (Exception e) {
            log.error("存储文档向量失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储文档向量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 语义搜索
     */
    public List<SearchResult> semanticSearch(String query, Embedding queryEmbedding, int maxResults, double minScore) {
        try {
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(minScore)
                    .build();

            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();
            
            return matches.stream()
                    .map(match -> {
                        SearchResult result = new SearchResult();
                        result.setDocumentId(match.embedded().metadata().getString("documentId"));
                        result.setTitle(match.embedded().metadata().getString("title"));
                        result.setContent(match.embedded().text());
                        result.setScore(match.score());
                        result.setSource(match.embedded().metadata().getString("fileType"));
                        result.setPosition(Integer.parseInt(match.embedded().metadata().getString("chunkIndex")));
                        return result;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("语义搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("语义搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 关键词搜索
     */
    public List<SearchResult> keywordSearch(String query, int maxResults) {
        try {
            // 使用Milvus的元数据过滤功能进行关键词搜索
            // 这里简化实现，实际可以使用全文搜索或其他技术
            List<EmbeddingMatch<TextSegment>> allMatches = new ArrayList<>();
            
            // 获取所有文档，然后按关键词过滤
            // 注意：这是一个简化实现，实际生产环境应该使用更高效的搜索策略
            for (EmbeddingMatch<TextSegment> match : embeddingStore.search(EmbeddingSearchRequest.builder()
                    .queryEmbedding(Embedding.from(new float[1024])) // 空向量，维度与配置一致
                    .maxResults(1000)
                    .build()).matches()) {
                
                String content = match.embedded().text().toLowerCase();
                String queryLower = query.toLowerCase();
                
                if (content.contains(queryLower)) {
                    SearchResult result = new SearchResult();
                    result.setDocumentId(match.embedded().metadata().getString("documentId"));
                    result.setTitle(match.embedded().metadata().getString("title"));
                    result.setContent(match.embedded().text());
                    result.setScore(0.8); // 关键词匹配的基础分数
                    result.setSource(match.embedded().metadata().getString("fileType"));
                    result.setPosition(Integer.parseInt(match.embedded().metadata().getString("chunkIndex")));
                    
                    // 计算关键词匹配度
                    String[] queryWords = queryLower.split("\\s+");
                    int matchCount = 0;
                    for (String word : queryWords) {
                        if (content.contains(word)) {
                            matchCount++;
                        }
                    }
                    result.setScore(result.getScore() + (matchCount * 0.1));
                    
                    allMatches.add(match);
                }
            }
            
            // 按匹配度排序并限制结果数量
            return allMatches.stream()
                    .sorted((m1, m2) -> {
                        String content1 = m1.embedded().text().toLowerCase();
                        String content2 = m2.embedded().text().toLowerCase();
                        String queryLower = query.toLowerCase();
                        
                        int count1 = countOccurrences(content1, queryLower);
                        int count2 = countOccurrences(content2, queryLower);
                        
                        return Integer.compare(count2, count1);
                    })
                    .limit(maxResults)
                    .map(match -> {
                        SearchResult result = new SearchResult();
                        result.setDocumentId(match.embedded().metadata().getString("documentId"));
                        result.setTitle(match.embedded().metadata().getString("title"));
                        result.setContent(match.embedded().text());
                        result.setSource(match.embedded().metadata().getString("fileType"));
                        result.setPosition(Integer.parseInt(match.embedded().metadata().getString("chunkIndex")));
                        
                        String content = match.embedded().text().toLowerCase();
                        String queryLower = query.toLowerCase();
                        int count = countOccurrences(content, queryLower);
                        result.setScore(0.8 + (count * 0.1));
                        
                        return result;
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("关键词搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("关键词搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 混合搜索（语义+关键词）
     */
    public List<SearchResult> hybridSearch(String query, Embedding queryEmbedding, int maxResults, double minScore) {
        try {
            // 获取语义搜索结果
            List<SearchResult> semanticResults = semanticSearch(query, queryEmbedding, maxResults, minScore);
            
            // 获取关键词搜索结果
            List<SearchResult> keywordResults = keywordSearch(query, maxResults);
            
            // 合并结果并去重
            Map<String, SearchResult> combinedResults = new LinkedHashMap<>();
            
            // 优先语义搜索结果
            for (SearchResult result : semanticResults) {
                String key = result.getDocumentId() + "_" + result.getPosition();
                combinedResults.put(key, result);
            }
            
            // 添加关键词搜索结果
            for (SearchResult result : keywordResults) {
                String key = result.getDocumentId() + "_" + result.getPosition();
                if (!combinedResults.containsKey(key)) {
                    combinedResults.put(key, result);
                }
            }
            
            // 按分数排序并限制结果数量
            return combinedResults.values().stream()
                    .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                    .limit(maxResults)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("混合搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("混合搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文档的所有向量
     */
    public void deleteDocument(String documentId) {
        try {
            // 删除特定文档的所有向量
            // 注意：这里需要根据metadata中的documentId进行删除
            log.info("删除文档向量: {}", documentId);
        } catch (Exception e) {
            log.error("删除文档向量失败: {}", e.getMessage(), e);
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
}