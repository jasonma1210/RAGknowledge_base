package com.aliyun.rag.service;

import com.aliyun.rag.config.MilvusConfig;
import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.DocumentMilvusMapping;
import com.aliyun.rag.model.PageResult;
import com.aliyun.rag.model.SearchResult;
import com.aliyun.rag.model.User;
import com.aliyun.rag.model.UserFileRecord;
import com.aliyun.rag.model.VectorData;
import com.aliyun.rag.repository.DocumentMilvusMappingRepository;
import com.aliyun.rag.repository.UserFileRecordRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.GetCollectionStatisticsParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.response.GetCollStatResponseWrapper;
import io.milvus.response.QueryResultsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final UserFileRecordRepository userFileRecordRepository;
    private final MilvusConfig milvusConfig;

    public VectorStoreService(EmbeddingModel embeddingModel,
                              DocumentMilvusMappingRepository documentMilvusMappingRepository,
                              UserFileRecordRepository userFileRecordRepository,
                              MilvusConfig milvusConfig) {
        this.embeddingModel = embeddingModel;
        this.documentMilvusMappingRepository = documentMilvusMappingRepository;
        this.userFileRecordRepository = userFileRecordRepository;
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
                                        "title", (documentInfo != null) ? String.valueOf(documentInfo) : "Unknown",
                                        "fileType", "Unknown",
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
                mapping.setUserId(userId);
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
    @Caching(evict = {
        @CacheEvict(value = "documentMappings", key = "#fileRecordId"),
        @CacheEvict(value = "userVectorStats", key = "#userId + '_' + #username"),
        @CacheEvict(value = "vectorCount", key = "#userId + '_' + #username"),
        @CacheEvict(value = "userMappings", key = "#userId")
    })
    public void deleteDocument(Long fileRecordId, Long userId, String username) {
        try {
            log.info("用户 {}({}) 正在删除文档向量: {}", username, userId, fileRecordId);

            // 为用户创建独立的MilvusEmbeddingStore实例
            MilvusEmbeddingStore userEmbeddingStore = getUserEmbeddingStore(userId, username);

            // 查找并删除数据库中的映射关系
            List<DocumentMilvusMapping> mappings = getDocumentMappingsFromCache(fileRecordId);

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

    /**
     * 获取指定用户的向量总数
     *
     * @param user 用户对象
     * @return 向量总数
     */
    public long getVectorCount(User user) {
        try {
            log.info("用户 {}({}) 正在获取向量总数", user.getUsername(), user.getId());

            // 实时查询数据库获取用户的向量总数，确保数据准确性
            long count = documentMilvusMappingRepository.countByUserIdAndIsDeleted(user.getId(), 0);

            log.info("用户 {}({}) 向量总数: {} (实时数据库查询)", user.getUsername(), user.getId(), count);
            return count;
        } catch (Exception e) {
            log.error("用户 {}({}) 获取向量总数失败: {}", user.getUsername(), user.getId(), e.getMessage(), e);
            throw new RuntimeException("获取向量总数失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用户的向量集合列表（分页）
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页结果，包含向量数据列表
     */
    public PageResult<VectorData> getUserVectors(Long userId, String username, int page, int size) {
        try {
            log.info("用户 {}({}) 正在获取向量列表，页码: {}, 大小: {}", username, userId, page, size);

            // 生成用户专属collection名称
            String collectionName = username + "_" + userId;

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
                log.info("Collection {} 不存在，返回空结果", collectionName);
                milvusClient.close();
                return new PageResult<>(new ArrayList<>(), page, size, 0);
            }

            // 实时查询数据库获取准确的向量总数
            long totalCount = documentMilvusMappingRepository.countByUserIdAndIsDeleted(userId, 0);
            
            // 获取分页数据
            List<DocumentMilvusMapping> allMappings = getUserMappingsFromCache(userId);

            // 计算偏移量和限制
            int offset = page * size;
            int limit = size;

            // 如果偏移量大于总数，返回空结果
            if (offset >= totalCount) {
                milvusClient.close();
                return new PageResult<>(new ArrayList<>(), page, size, totalCount);
            }

            // 使用更简单的方法：直接基于offset和limit分页查询，然后用数据库映射信息补充
            QueryParam queryParam = QueryParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withOffset((long) offset)
                    .withLimit((long) size)
                    .withOutFields(Arrays.asList("id", "text"))
                    .build();

            // 执行查询获取向量数据
            R<io.milvus.grpc.QueryResults> response = milvusClient.query(queryParam);
            
            // 创建VectorData列表
            List<VectorData> vectorDataList = new ArrayList<>();
            if (response.getStatus() == 0 && response.getData() != null) { // 检查查询是否成功
                QueryResultsWrapper queryResultsWrapper = new QueryResultsWrapper(response.getData());
                
                // 获取各字段的数据
                List<?> ids = queryResultsWrapper.getFieldWrapper("id").getFieldData();
                List<?> texts = queryResultsWrapper.getFieldWrapper("text").getFieldData();
                
                // 构建VectorData对象列表
                int resultCount = ids.size();
                for (int i = 0; i < resultCount; i++) {
                    String milvusId = ids.get(i).toString();
                    String text = texts.size() > i ? texts.get(i).toString() : "";
                    
                    VectorData vectorData = new VectorData();
                    vectorData.setId(milvusId);
                    vectorData.setText(text);
                    // 设置显示用的截断文本，不超过一行
                    String displayText = text.length() > 100 ? text.substring(0, 100) + "..." : text;
                    vectorData.setDisplayText(displayText);
                    
                    // 从数据库映射表中获取详细信息
                    try {
                        Optional<DocumentMilvusMapping> mappingOpt = documentMilvusMappingRepository.findByMilvusIdAndIsDeleted(milvusId, 0);
                        if (mappingOpt.isPresent()) {
                            DocumentMilvusMapping mapping = mappingOpt.get();
                            vectorData.setFileRecordId(mapping.getFileRecordId().toString());
                            vectorData.setChunkIndex(mapping.getVectorIndex());
                            
                            // 尝试获取文件信息
                            try {
                                Optional<UserFileRecord> fileRecordOpt = userFileRecordRepository.findByIdAndIsDeleted(mapping.getFileRecordId(), 0);
                                if (fileRecordOpt.isPresent()) {
                                    UserFileRecord fileRecord = fileRecordOpt.get();
                                    vectorData.setTitle(fileRecord.getFileName());
                                    vectorData.setFileType(fileRecord.getFileType());
                                    vectorData.setTags(""); // UserFileRecord doesn't have tags field
                                    vectorData.setCreateTime(fileRecord.getUploadTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                                }
                            } catch (Exception e) {
                                log.warn("获取文件信息失败: {}", e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("获取向量映射信息失败: {}", e.getMessage());
                    }
                    
                    vectorDataList.add(vectorData);
                }
            } else {
                log.warn("Milvus查询失败或返回空结果，状态码: {}", response.getStatus());
            }

            // vectorDataList已经在上面构建好了

            // 返回分页结果
            PageResult<VectorData> result = new PageResult<>(vectorDataList, page, size, totalCount);
            
            log.info("用户 {}({}) 获取向量列表完成，返回 {} 条结果", username, userId, vectorDataList.size());
            
            // 关闭客户端连接
            milvusClient.close();
            
            return result;
        } catch (Exception e) {
            log.error("用户 {}({}) 获取向量列表失败: {}", username, userId, e.getMessage(), e);
            throw new RuntimeException("获取向量列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用户的向量统计信息
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return 向量统计信息
     */
    public Map<String, Object> getUserVectorStats(Long userId, String username) {
        try {
            log.info("用户 {}({}) 正在获取向量统计信息", username, userId);

            Map<String, Object> stats = new HashMap<>();

            // 实时查询数据库获取准确的向量总数
            long totalCount = documentMilvusMappingRepository.countByUserIdAndIsDeleted(userId, 0);
            
            // 获取文件数量（也使用实时查询）
            List<DocumentMilvusMapping> mappings = getUserMappingsFromCache(userId);
            long fileCount = mappings.stream()
                    .map(DocumentMilvusMapping::getFileRecordId)
                    .distinct()
                    .count();

            // 生成用户专属collection名称
            String collectionName = username + "_" + userId;

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
                log.info("Collection {} 不存在，返回默认统计信息", collectionName);
                stats.put("totalCount", totalCount);
                stats.put("collectionExists", false);
                stats.put("dimension", milvusConfig.getDimension());
                stats.put("fileCount", fileCount);
                stats.put("vectorStorageUsed", 0L);
                stats.put("vectorStorageQuota", 0L);
                stats.put("vectorStorageUsagePercentage", 0.0);
                milvusClient.close();
                return stats;
            }

            // 计算向量存储容量
            // 每个向量大约占用 4KB (1024维 * 4字节/浮点数) + 元数据开销
            long vectorStorageUsed = totalCount * 4096; // 简化计算，每个向量约4KB
            
            // 根据用户等级设置向量存储配额
            // 普通用户：1GB向量存储，进阶用户：10GB向量存储
            long vectorStorageQuota = 1073741824L; // 默认1GB
            // 这里可以通过用户服务获取用户等级信息来设置不同的配额
            // 暂时使用默认值

            // 计算使用百分比
            double vectorStorageUsagePercentage = vectorStorageQuota > 0 ? 
                (double) vectorStorageUsed / vectorStorageQuota * 100 : 0.0;

            stats.put("totalCount", totalCount);
            stats.put("collectionExists", true);
            stats.put("collectionName", collectionName);
            stats.put("dimension", milvusConfig.getDimension());
            stats.put("fileCount", fileCount);
            stats.put("vectorStorageUsed", vectorStorageUsed);
            stats.put("vectorStorageQuota", vectorStorageQuota);
            stats.put("vectorStorageUsagePercentage", Math.round(vectorStorageUsagePercentage * 100.0) / 100.0);
            stats.put("formattedVectorStorageUsed", formatBytes(vectorStorageUsed));
            stats.put("formattedVectorStorageQuota", formatBytes(vectorStorageQuota));
            stats.put("formattedVectorStorageRemaining", formatBytes(Math.max(0, vectorStorageQuota - vectorStorageUsed)));
            stats.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            log.info("用户 {}({}) 获取向量统计信息完成，总数: {}, 文件数: {}, 向量存储: {}/{}", 
                username, userId, totalCount, fileCount, formatBytes(vectorStorageUsed), formatBytes(vectorStorageQuota));
            
            // 关闭客户端连接
            milvusClient.close();
            
            return stats;
        } catch (Exception e) {
            log.error("用户 {}({}) 获取向量统计信息失败: {}", username, userId, e.getMessage(), e);
            throw new RuntimeException("获取向量统计信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 格式化字节数为可读格式
     *
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    private String formatBytes(long bytes) {
        if (bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 从缓存中获取文档映射关系
     *
     * @param fileRecordId 文件记录ID
     * @return 映射关系列表
     */
    @Cacheable(value = "documentMappings", key = "#fileRecordId")
    public List<DocumentMilvusMapping> getDocumentMappingsFromCache(Long fileRecordId) {
        return documentMilvusMappingRepository.findByFileRecordIdAndIsDeleted(fileRecordId, 0);
    }

    /**
     * 从缓存中获取用户映射关系
     *
     * @param userId 用户ID
     * @return 映射关系列表
     */
    @Cacheable(value = "userMappings", key = "#userId")
    public List<DocumentMilvusMapping> getUserMappingsFromCache(Long userId) {
        return documentMilvusMappingRepository.findByUserIdAndIsDeleted(userId, 0);
    }
}