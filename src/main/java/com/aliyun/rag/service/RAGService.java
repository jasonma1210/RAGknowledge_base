package com.aliyun.rag.service;

import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.DocumentRequest;
import com.aliyun.rag.model.SearchRequest;
import com.aliyun.rag.model.SearchResult;
import com.aliyun.rag.model.User;
import com.aliyun.rag.model.UserFileRecord;
import com.aliyun.rag.repository.UserFileRecordRepository;
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
    private final UserFileRecordRepository userFileRecordRepository;

    public RAGService(DocumentProcessor documentProcessor,
                     EmbeddingService embeddingService,
                     VectorStoreService vectorStoreService,
                     ChatModel qwenChatModel,
                     UserFileRecordRepository userFileRecordRepository) {
        this.documentProcessor = documentProcessor;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.qwenChatModel = qwenChatModel;
        this.userFileRecordRepository = userFileRecordRepository;
    }

    /**
     * 上传并处理文档
     */
    public DocumentInfo uploadDocument(DocumentRequest request, User user, String fileUrl) {
        try {
            MultipartFile file = request.getFile();
            
            // 创建文档信息
            DocumentInfo documentInfo = new DocumentInfo();
            String documentId = UUID.randomUUID().toString();
            documentInfo.setId(documentId);
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
            
            // 保存用户文件记录到数据库
            UserFileRecord userFileRecord = new UserFileRecord();
            userFileRecord.setUserId(user.getId());
            userFileRecord.setFileName(file.getOriginalFilename());
            userFileRecord.setFilePath(fileUrl);
            userFileRecord.setFileSize(file.getSize());
            userFileRecord.setFileType(getFileExtension(file.getOriginalFilename()));
            userFileRecord.setUploadTime(LocalDateTime.now());
            userFileRecord.setGmtCreate(LocalDateTime.now());
            userFileRecord.setGmtModified(LocalDateTime.now());
            userFileRecord.setIsDeleted(0);
            
            userFileRecordRepository.save(userFileRecord);
            
            // 存储到向量数据库（使用数据库记录ID、用户ID和用户名）
            vectorStoreService.storeDocument(userFileRecord.getId(), user.getId(), user.getUsername(), chunks, embeddings, documentInfo);
            
            log.info("文档上传成功: {}, 分块数量: {}", documentId, chunks.length);
            
            return documentInfo;
            
        } catch (Exception e) {
            log.error("文档上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文档上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 搜索知识库
     */
    public List<SearchResult> searchKnowledgeBase(SearchRequest request, User user) {
        try {
            String query = request.getQuery();
            
            // 生成查询的嵌入向量
            Embedding queryEmbedding = embeddingService.embedText(query);
            
            List<SearchResult> results = switch (request.getSearchType()) {
                case SEMANTIC -> vectorStoreService.semanticSearch(
                        query, queryEmbedding, request.getMaxResults(), request.getMinScore(), user.getId(), user.getUsername());
                case KEYWORD -> vectorStoreService.keywordSearch(query, request.getMaxResults(), user.getId(), user.getUsername());
                case HYBRID -> vectorStoreService.hybridSearch(
                        query, queryEmbedding, request.getMaxResults(), request.getMinScore(), user.getId(), user.getUsername());
//                default -> throw new IllegalArgumentException("不支持的搜索类型: " + request.getSearchType());
            };

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
                context,
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
    public Map<String, Object> askQuestion(String question, SearchRequest searchRequest, User user) {
        try {
            // 搜索相关知识
            List<SearchResult> searchResults = searchKnowledgeBase(searchRequest, user);
            
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
    public List<DocumentInfo> getAllDocuments(User user) {
        try {
            // 从数据库获取用户的所有文件记录
            List<UserFileRecord> userFileRecords = userFileRecordRepository.findByUserIdAndIsDeleted(user.getId(), 0);
            
            // 转换为DocumentInfo列表
            List<DocumentInfo> documentInfos = new ArrayList<>();
            for (UserFileRecord record : userFileRecords) {
                DocumentInfo documentInfo = new DocumentInfo();
                documentInfo.setId(record.getId().toString()); // 使用数据库记录ID
                documentInfo.setTitle(record.getFileName());
                documentInfo.setFileName(record.getFilePath());
                documentInfo.setFileSize(record.getFileSize());
                documentInfo.setFileType(record.getFileType());
                documentInfo.setUploadTime(record.getUploadTime());
                documentInfos.add(documentInfo);
            }
            
            return documentInfos;
        } catch (Exception e) {
            log.error("获取文档列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取文档列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文档
     */
    public void deleteDocument(String documentId, User user) {
        try {
            // 删除向量数据库中的文档（使用数据库记录ID、用户ID和用户名）
            vectorStoreService.deleteDocument(Long.valueOf(documentId), user.getId(), user.getUsername());
            
            // 删除数据库中的文件记录
            Optional<UserFileRecord> optionalRecord = userFileRecordRepository.findByIdAndUserIdAndIsDeleted(
                Long.valueOf(documentId), user.getId(), 0);
            
            if (optionalRecord.isPresent()) {
                UserFileRecord record = optionalRecord.get();
                record.setIsDeleted(1); // 标记为已删除
                record.setGmtModified(LocalDateTime.now());
                userFileRecordRepository.save(record);
            }
            
            log.info("文档删除成功: {}", documentId);
        } catch (Exception e) {
            log.error("文档删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文档删除失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}