package com.aliyun.rag.service;

import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.SearchResult;
import com.aliyun.rag.model.User;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 高级RAG服务
 * <p>
 * 提供高级RAG功能，包括多轮对话、文档摘要、相似文档推荐和知识图谱构建
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-10-12
 */
@Service
public class AdvancedRAGService {

    private static final Logger log = LoggerFactory.getLogger(AdvancedRAGService.class);

    private final RAGService ragService;
    private final ChatModel qwenChatModel;
    private final StreamingChatModel qwenStreamingChatModel;
    private final OptimizedVectorStoreService optimizedVectorStoreService;

    public AdvancedRAGService(RAGService ragService,
                              ChatModel qwenChatModel,
                              StreamingChatModel qwenStreamingChatModel,
                              OptimizedVectorStoreService optimizedVectorStoreService) {
        this.ragService = ragService;
        this.qwenChatModel = qwenChatModel;
        this.qwenStreamingChatModel = qwenStreamingChatModel;
        this.optimizedVectorStoreService = optimizedVectorStoreService;
    }

    /**
     * 多轮对话支持
     * 
     * @param question 用户问题
     * @param history 对话历史
     * @param user 用户信息
     * @return AI回答
     */
    public String multiTurnConversation(String question, List<ChatMessage> history, User user) {
        try {
            log.info("执行多轮对话: 用户={}, 问题={}", user.getUsername(), question);

            // 构建搜索请求（使用混合搜索）
            List<SearchResult> searchResults = optimizedVectorStoreService.advancedHybridSearch(
                    question, user.getId(), user.getUsername(), 10, 0.7);

            // 构建上下文
            StringBuilder context = new StringBuilder();
            for (SearchResult result : searchResults) {
                context.append("【").append(result.getTitle()).append("】")
                        .append(result.getContent()).append("\n\n");
            }

            // 构建完整的对话历史
            List<ChatMessage> fullHistory = new ArrayList<>();
            
            // 添加系统提示
            fullHistory.add(new SystemMessage(
                    "你是一个智能知识库助手，请基于提供的知识库内容和对话历史来回答用户问题。" +
                    "知识库内容：\n" + context.toString()));

            // 添加历史对话
            fullHistory.addAll(history);
            
            // 添加当前问题
            fullHistory.add(new UserMessage(question));

            // 生成回答
            String answer = qwenChatModel.chat(fullHistory).aiMessage().text();

            log.info("多轮对话完成: 用户={}, 问题={}", user.getUsername(), question);
            return answer;

        } catch (Exception e) {
            log.error("多轮对话失败: {}", e.getMessage(), e);
            throw new RuntimeException("多轮对话失败: " + e.getMessage(), e);
        }
    }

    /**
     * 文档摘要生成
     * 
     * @param documentId 文档ID
     * @param user 用户信息
     * @return 文档摘要
     */
    public String generateDocumentSummary(String documentId, User user) {
        try {
            log.info("生成文档摘要: 用户={}, 文档ID={}", user.getUsername(), documentId);

            // 获取文档信息
            DocumentInfo documentInfo = ragService.getDocumentById(documentId, user);
            if (documentInfo == null) {
                throw new RuntimeException("文档不存在");
            }

            // 构建摘要生成提示
            String prompt = String.format(
                    """
                    请为以下文档生成一个简洁的摘要：
                    
                    文档标题：%s
                    
                    文档内容：
                    %s
                    
                    请提供一个不超过200字的摘要，突出文档的核心内容和要点。
                    """,
                    documentInfo.getTitle(),
                    "文档内容需要从向量数据库中检索"            );

            // 生成摘要
            String summary = qwenChatModel.chat(prompt);

            log.info("文档摘要生成完成: 用户={}, 文档ID={}", user.getUsername(), documentId);
            return summary;

        } catch (Exception e) {
            log.error("文档摘要生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("文档摘要生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 相似文档推荐
     * 
     * @param documentId 文档ID
     * @param user 用户信息
     * @param maxResults 最大推荐数量
     * @return 相似文档列表
     */
    public List<DocumentInfo> recommendSimilarDocuments(String documentId, User user, int maxResults) {
        try {
            log.info("推荐相似文档: 用户={}, 文档ID={}, 最大推荐数量={}", user.getUsername(), documentId, maxResults);

            // 获取文档信息
            DocumentInfo documentInfo = ragService.getDocumentById(documentId, user);
            if (documentInfo == null) {
                throw new RuntimeException("文档不存在");
            }

            // 使用文档标题进行相似性搜索
            String query = documentInfo.getTitle();

            // 执行语义搜索
            List<SearchResult> searchResults = optimizedVectorStoreService.cachedSemanticSearch(
                    query, user.getId(), user.getUsername(), maxResults * 2, 0.5);

            // 过滤掉当前文档本身
            List<SearchResult> filteredResults = searchResults.stream()
                    .filter(result -> !documentId.equals(result.getFileRecordId()))
                    .limit(maxResults)
                    .collect(Collectors.toList());

            // 转换为DocumentInfo列表
            List<DocumentInfo> recommendations = new ArrayList<>();
            for (SearchResult result : filteredResults) {
                DocumentInfo docInfo = new DocumentInfo();
                docInfo.setId(result.getFileRecordId());
                docInfo.setTitle(result.getTitle());
                docInfo.setFileName(result.getSource());
                recommendations.add(docInfo);
            }

            log.info("相似文档推荐完成: 用户={}, 文档ID={}, 推荐数量={}", user.getUsername(), documentId, recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("相似文档推荐失败: {}", e.getMessage(), e);
            throw new RuntimeException("相似文档推荐失败: " + e.getMessage(), e);
        }
    }

    /**
     * 知识图谱构建
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return 知识图谱数据
     */
    public Map<String, Object> buildKnowledgeGraph(Long userId, String username) {
        try {
            log.info("构建知识图谱: 用户ID={}, 用户名={}", userId, username);

            // 构建知识图谱
            // 1. 获取用户的所有文档
            List<DocumentInfo> documents = ragService.getAllDocuments(new User() {{
                setId(userId);
                setUsername(username);
            }});

            // 2. 获取用户的所有搜索结果作为关系数据
            List<SearchResult> searchResults = optimizedVectorStoreService.cachedSemanticSearch(
                    "", userId, username, 100, 0.0);

            // 3. 构建节点和边
            Map<String, Object> knowledgeGraph = new HashMap<>();
            knowledgeGraph.put("userId", userId);
            knowledgeGraph.put("username", username);
            knowledgeGraph.put("buildTime", System.currentTimeMillis());

            // 构建节点
            List<Map<String, Object>> nodes = new ArrayList<>();
            List<Map<String, Object>> edges = new ArrayList<>();

            // 为每个文档创建节点
            for (DocumentInfo doc : documents) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", "doc_" + doc.getId());
                node.put("label", "文档");
                Map<String, Object> properties = new HashMap<>();
                properties.put("title", doc.getTitle());
                properties.put("fileType", doc.getFileType());
                properties.put("uploadTime", doc.getUploadTime());
                node.put("properties", properties);
                nodes.add(node);
            }

            // 为搜索结果中的概念创建节点和关系
            for (SearchResult result : searchResults) {
                // 创建文档节点（如果不存在）
                Map<String, Object> docNode = new HashMap<>();
                docNode.put("id", "doc_" + result.getFileRecordId());
                docNode.put("label", "文档");
                Map<String, Object> properties = new HashMap<>();
                properties.put("title", result.getTitle());
                docNode.put("properties", properties);
                
                // 检查节点是否已存在
                boolean exists = false;
                for (Map<String, Object> existingNode : nodes) {
                    if (existingNode.get("id").equals(docNode.get("id"))) {
                        exists = true;
                        break;
                    }
                }
                
                if (!exists) {
                    nodes.add(docNode);
                }

                // 创建概念节点和边（示例：基于关键词）
                String[] keywords = result.getContent().split("\\s+");
                for (int i = 0; i < Math.min(3, keywords.length); i++) {
                    String keyword = keywords[i];
                    if (keyword.length() > 2) { // 过滤短词
                        // 创建概念节点
                        Map<String, Object> conceptNode = new HashMap<>();
                        conceptNode.put("id", "concept_" + keyword);
                        conceptNode.put("label", "概念");
                        Map<String, Object> conceptProps = new HashMap<>();
                        conceptProps.put("name", keyword);
                        conceptNode.put("properties", conceptProps);
                        
                        // 检查概念节点是否已存在
                        boolean conceptExists = false;
                        for (Map<String, Object> existingNode : nodes) {
                            if (existingNode.get("id").equals(conceptNode.get("id"))) {
                                conceptExists = true;
                                break;
                            }
                        }
                        
                        if (!conceptExists) {
                            nodes.add(conceptNode);
                        }

                        // 创建边
                        Map<String, Object> edge = new HashMap<>();
                        edge.put("source", "doc_" + result.getFileRecordId());
                        edge.put("target", "concept_" + keyword);
                        edge.put("label", "包含");
                        edges.add(edge);
                    }
                }
            }

            knowledgeGraph.put("nodes", nodes);
            knowledgeGraph.put("edges", edges);

            log.info("知识图谱构建完成: 用户ID={}, 用户名={}", userId, username);
            return knowledgeGraph;

        } catch (Exception e) {
            log.error("知识图谱构建失败: {}", e.getMessage(), e);
            throw new RuntimeException("知识图谱构建失败: " + e.getMessage(), e);
        }
    }
}