package com.aliyun.rag.service;

import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.DocumentRequest;
import com.aliyun.rag.model.SearchRequest;
import com.aliyun.rag.model.SearchResult;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * RAG服务
 * <p>
 * 提供完整的检索增强生成服务，包括文档处理、向量存储、语义搜索和智能问答
 * 集成LangChain4j框架，支持多种搜索模式和AI模型
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
@Service
public class RAGService {
    
    private static final Logger log = LoggerFactory.getLogger(RAGService.class);

    private final DocumentProcessor documentProcessor;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final ChatModel qwenChatModel;

    public RAGService(DocumentProcessor documentProcessor,
                     EmbeddingService embeddingService,
                     VectorStoreService vectorStoreService,
                     ChatModel qwenChatModel) {
        this.documentProcessor = documentProcessor;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.qwenChatModel = qwenChatModel;
    }

    /**
     * 上传并处理文档
     */
    public DocumentInfo uploadDocument(DocumentRequest request) {
        try {
            MultipartFile file = request.getFile();
            
            // 创建文档信息
            DocumentInfo documentInfo = new DocumentInfo();
            documentInfo.setId(UUID.randomUUID().toString());
            documentInfo.setTitle(request.getTitle() != null ? request.getTitle() : file.getOriginalFilename());
            documentInfo.setDescription(request.getDescription());
            documentInfo.setTags(request.getTags());
            documentInfo.setUploadTime(LocalDateTime.now());
            
            // 处理文档
            String content = documentProcessor.processDocument(file, documentInfo);
            
            // 分块
            String[] chunks = documentProcessor.chunkText(content);
            documentInfo.setChunkCount(chunks.length);
            
            // 生成嵌入向量
            List<Embedding> embeddings = embeddingService.embedTextChunks(chunks);
            
            // 存储到向量数据库
            vectorStoreService.storeDocument(documentInfo.getId(), chunks, embeddings, documentInfo);
            
            log.info("文档上传成功: {}, 分块数量: {}", documentInfo.getId(), chunks.length);
            
            return documentInfo;
            
        } catch (Exception e) {
            log.error("文档上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文档上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 搜索知识库
     */
    public List<SearchResult> searchKnowledgeBase(SearchRequest request) {
        try {
            String query = request.getQuery();
            
            // 生成查询的嵌入向量
            Embedding queryEmbedding = embeddingService.embedText(query);
            
            List<SearchResult> results;
            
            switch (request.getSearchType()) {
                case SEMANTIC:
                    results = vectorStoreService.semanticSearch(
                        query, queryEmbedding, request.getMaxResults(), request.getMinScore());
                    break;
                case KEYWORD:
                    results = vectorStoreService.keywordSearch(query, request.getMaxResults());
                    break;
                case HYBRID:
                    results = vectorStoreService.hybridSearch(
                        query, queryEmbedding, request.getMaxResults(), request.getMinScore());
                    break;
                default:
                    throw new IllegalArgumentException("不支持的搜索类型: " + request.getSearchType());
            }
            
            log.info("搜索完成: {}, 结果数量: {}", query, results.size());
            return results;
            
        } catch (Exception e) {
            log.error("搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成回答
     */
    public String generateAnswer(String question, List<SearchResult> searchResults) {
        try {
            if (searchResults.isEmpty()) {
                return "抱歉，没有找到相关的知识库内容来回答您的问题。";
            }

            // 构建上下文
            StringBuilder context = new StringBuilder();
            for (SearchResult result : searchResults) {
                context.append("【").append(result.getTitle()).append("】")
                       .append(result.getContent()).append("\n\n");
            }

            // 构建提示词
            String prompt = String.format(
                """
                基于以下知识库内容，请回答用户的问题：
                
                知识库内容：
                %s
                
                用户问题：%s
                
                请提供准确、简洁的回答，并引用相关的知识库内容。
                """,
                context.toString(),
                question
            );

            // 生成回答
            String answer = qwenChatModel.chat(prompt);
            
            log.info("回答生成完成: {}", question);
            return answer;
            
        } catch (Exception e) {
            log.error("回答生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("回答生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 问答接口
     */
    public Map<String, Object> askQuestion(String question, SearchRequest searchRequest) {
        try {
            // 搜索相关知识
            List<SearchResult> searchResults = searchKnowledgeBase(searchRequest);
            
            // 生成回答
            String answer = generateAnswer(question, searchResults);
            
            Map<String, Object> response = new HashMap<>();
            response.put("question", question);
            response.put("answer", answer);
            response.put("sources", searchResults);
            response.put("sourceCount", searchResults.size());
            
            return response;
            
        } catch (Exception e) {
            log.error("问答失败: {}", e.getMessage(), e);
            throw new RuntimeException("问答失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有文档
     */
    public List<DocumentInfo> getAllDocuments() {
        // 这里简化实现，实际应该从数据库获取
        return new ArrayList<>();
    }

    /**
     * 删除文档
     */
    public void deleteDocument(String documentId) {
        try {
            vectorStoreService.deleteDocument(documentId);
            log.info("文档删除成功: {}", documentId);
        } catch (Exception e) {
            log.error("文档删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文档删除失败: " + e.getMessage(), e);
        }
    }
}