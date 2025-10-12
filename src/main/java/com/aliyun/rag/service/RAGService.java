package com.aliyun.rag.service;

import com.aliyun.rag.model.DocumentInfo;
import com.aliyun.rag.model.DocumentRequest;
import com.aliyun.rag.model.DocumentProcessResult;
import com.aliyun.rag.model.PageResult;
import com.aliyun.rag.model.SearchRequest;
import com.aliyun.rag.model.SearchResult;
import com.aliyun.rag.model.User;
import com.aliyun.rag.model.UserFileRecord;
import com.aliyun.rag.repository.UserFileRecordRepository;
import com.aliyun.rag.repository.UserRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
    private final StreamingChatModel qwenStreamingChatModel;
    private final UserFileRecordRepository userFileRecordRepository;
    private final UserRepository userRepository;
    private final QiniuUploadService qiniuUploadService;
    private final Executor fileProcessExecutor;

    public RAGService(DocumentProcessor documentProcessor,
                      EmbeddingService embeddingService,
                      VectorStoreService vectorStoreService,
                      ChatModel qwenChatModel,
                      StreamingChatModel qwenStreamingChatModel,
                      UserFileRecordRepository userFileRecordRepository,
                      UserRepository userRepository,
                      QiniuUploadService qiniuUploadService,
                      Executor fileProcessExecutor) {
        this.documentProcessor = documentProcessor;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.qwenChatModel = qwenChatModel;
        this.qwenStreamingChatModel = qwenStreamingChatModel;
        this.userFileRecordRepository = userFileRecordRepository;
        this.userRepository = userRepository;
        this.qiniuUploadService = qiniuUploadService;
        this.fileProcessExecutor = fileProcessExecutor;
    }

    /**
     * 上传并处理文档
     * <p>
     * 先处理文件（不在事务中），再执行数据库操作（事务中）
     * </p>
     */
    public DocumentInfo uploadDocument(DocumentRequest request, User user, String fileUrl) {
        try {
            MultipartFile file = request.getFile();
            
            // 1. 预处理检查（非事务操作）
            if (!checkStorageQuota(user, file.getSize())) {
                throw new RuntimeException("存储空间不足，请联系管理员");
            }
            
            // 2. 处理文档内容（非事务操作）
            DocumentProcessResult processResult = processDocumentFile(request, file, fileUrl);
            
            // 3. 执行数据库操作（事务中）
            return saveDocumentInfo(processResult, user, file);
            
        } catch (Exception e) {
            log.error("文档上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文档上传失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理文档文件（非事务操作）
     */
    private DocumentProcessResult processDocumentFile(DocumentRequest request, MultipartFile file, String fileUrl) {
        try {
            // 创建文档信息
            DocumentInfo documentInfo = new DocumentInfo();
            String documentId = UUID.randomUUID().toString();
            documentInfo.setId(documentId);
            documentInfo.setTitle(request.getTitle() != null ? request.getTitle() : file.getOriginalFilename());
            documentInfo.setDescription(request.getDescription());
            documentInfo.setTags(request.getTags());
            documentInfo.setUploadTime(LocalDateTime.now());
            
            // 处理文档内容
            String content = documentProcessor.processDocument(file, documentInfo);
            
            // 分块（使用智能分块策略）
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String[] chunks = documentProcessor.chunkText(content, fileExtension);
            documentInfo.setChunkCount(chunks.length);
            
            // 生成嵌入向量
            List<Embedding> embeddings = embeddingService.embedTextChunks(chunks);
            
            log.info("文档处理完成: {}, 分块数量: {}", documentId, chunks.length);
            
            return new DocumentProcessResult(documentInfo, chunks, embeddings, fileUrl);
            
        } catch (Exception e) {
            log.error("文档处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("文档处理失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 保存文档信息到数据库（事务操作）
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public DocumentInfo saveDocumentInfo(DocumentProcessResult processResult, User user, MultipartFile file) {
        try {
            DocumentInfo documentInfo = processResult.getDocumentInfo();
            String[] chunks = processResult.getChunks();
            List<Embedding> embeddings = processResult.getEmbeddings();
            String fileUrl = processResult.getFileUrl();
            
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
            
            // 更新用户存储容量
            updateUserStorageAfterUpload(user, file.getSize());
            
            // 存储到向量数据库（使用数据库记录ID、用户ID和用户名）
            vectorStoreService.storeDocument(userFileRecord.getId(), user.getId(), user.getUsername(), chunks, embeddings, documentInfo);
            
            log.info("文档保存成功: {}, 分块数量: {}", documentInfo.getId(), chunks.length);
            
            return documentInfo;
            
        } catch (Exception e) {
            log.error("文档保存失败: {}", e.getMessage(), e);
            throw e; // 重新抛出以触发事务回滚
        }
    }

    /**
     * 更新用户存储容量（上传文件后）
     *
     * @param user 用户
     * @param fileSize 文件大小（字节）
     */
    public void updateUserStorageAfterUpload(User user, Long fileSize) {
        try {
            // 查找用户最新信息
            Optional<User> optionalUser = userRepository.findById(user.getId());
            if (optionalUser.isPresent()) {
                User updatedUser = optionalUser.get();

                // 更新已使用存储空间（增加文件大小）
                long newUsedStorage = updatedUser.getUsedStorage() + fileSize;
                updatedUser.setUsedStorage(newUsedStorage);
                updatedUser.setGmtModified(LocalDateTime.now());

                // 保存更新后的用户信息
                userRepository.save(updatedUser);

                log.info("用户 {} 存储容量更新成功，已使用存储: {} 字节", updatedUser.getUsername(), newUsedStorage);
            }
        } catch (Exception e) {
            log.error("更新用户存储容量失败: {}", e.getMessage(), e);
            // 不抛出异常，因为这不影响文档上传的主要功能
        }
    }

    /**
     * 搜索知识库（支持分页）
     */
    public PageResult<SearchResult> searchKnowledgeBase(SearchRequest request, User user, int page, int size) {
        try {
            String query = request.getQuery();

            // 生成查询的嵌入向量
            Embedding queryEmbedding = embeddingService.embedText(query);

            List<SearchResult> allResults = switch (request.getSearchType()) {
                case SEMANTIC -> vectorStoreService.semanticSearch(
                        query, queryEmbedding, request.getMaxResults(), request.getMinScore(), user.getId(), user.getUsername());
                case KEYWORD -> vectorStoreService.keywordSearch(query, request.getMaxResults(), user.getId(), user.getUsername());
                case HYBRID -> vectorStoreService.hybridSearch(
                        query, queryEmbedding, request.getMaxResults(), request.getMinScore(), user.getId(), user.getUsername());
                default -> new ArrayList<>();
            };

            // 分页处理
            int total = allResults.size();
            int start = page * size;
            int end = Math.min(start + size, total);

            List<SearchResult> pagedResults = new ArrayList<>();
            if (start < total) {
                pagedResults = allResults.subList(start, end);
            }

            PageResult<SearchResult> pageResult = new PageResult<>(pagedResults, page, size, total);

            log.info("搜索完成: {}, 总结果数: {}, 当前页: {}, 当前页结果数: {}", query, total, page, pagedResults.size());
            return pageResult;

        } catch (Exception e) {
            log.error("搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 搜索知识库（不支持分页，保持向后兼容）
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
                default -> new ArrayList<>();
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
     * 流式问答接口
     */
    public void askQuestionStreaming(String question, SearchRequest searchRequest, User user, dev.langchain4j.model.StreamingResponseHandler<AiMessage> handler) {
        try {
            // 搜索相关知识
            List<SearchResult> searchResults = searchKnowledgeBase(searchRequest, user);

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

            // 使用流式模型生成回答
            log.info("开始流式生成回答: {}", question);
            
            // 使用流式处理方式
            qwenStreamingChatModel.chat(prompt, new dev.langchain4j.model.chat.response.StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String token) {
                    handler.onNext(token);
                }

                @Override
                public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                    handler.onComplete(new Response<>(response.aiMessage()));
                }

                @Override
                public void onError(Throwable throwable) {
                    handler.onError(throwable);
                }
            });

        } catch (Exception e) {
            log.error("流式问答失败: {}", e.getMessage(), e);
            handler.onError(e);
        }
    }

    /**
     * 流式响应处理器接口
     */
    public interface StreamingResponseHandler {
        void onStart();
        void onNext(String token);
        void onComplete(List<SearchResult> sources);
        void onError(Throwable throwable);
    }

    /**
     * 分页获取文档列表
     */
    public PageResult<DocumentInfo> getDocuments(User user, int page, int size) {
        try {
            // 使用数据库分页查询获取用户的文件记录
            Pageable pageable = PageRequest.of(page, size);
            Page<UserFileRecord> userFileRecordPage = userFileRecordRepository.findByUserIdAndIsDeleted(user.getId(), 0, pageable);

            // 转换为DocumentInfo列表
            List<DocumentInfo> documentInfos = userFileRecordPage.getContent().stream().map(record -> {
                DocumentInfo documentInfo = new DocumentInfo();
                documentInfo.setId(record.getId().toString()); // 使用数据库记录ID
                documentInfo.setTitle(record.getFileName());
                documentInfo.setFileName(record.getFilePath());
                documentInfo.setFileSize(record.getFileSize());
                documentInfo.setFileType(record.getFileType());
                documentInfo.setUploadTime(record.getUploadTime());
                return documentInfo;
            }).collect(Collectors.toList());

            PageResult<DocumentInfo> pageResult = new PageResult<>(documentInfos, page, size, userFileRecordPage.getTotalElements());
            pageResult.setTotalPages(userFileRecordPage.getTotalPages());

            return pageResult;
        } catch (Exception e) {
            log.error("获取文档列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取文档列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 搜索文档列表（支持模糊匹配）
     */
    public PageResult<DocumentInfo> searchDocuments(User user, String keyword, int page, int size) {
        try {
            // 使用数据库分页查询获取用户的文件记录，支持模糊匹配
            Pageable pageable = PageRequest.of(page, size);
            Page<UserFileRecord> userFileRecordPage = userFileRecordRepository.findByUserIdAndFileNameContainingAndIsDeleted(
                    user.getId(), keyword, 0, pageable);

            // 转换为DocumentInfo列表
            List<DocumentInfo> documentInfos = userFileRecordPage.getContent().stream().map(record -> {
                DocumentInfo documentInfo = new DocumentInfo();
                documentInfo.setId(record.getId().toString()); // 使用数据库记录ID
                documentInfo.setTitle(record.getFileName());
                documentInfo.setFileName(record.getFilePath());
                documentInfo.setFileSize(record.getFileSize());
                documentInfo.setFileType(record.getFileType());
                documentInfo.setUploadTime(record.getUploadTime());
                return documentInfo;
            }).collect(Collectors.toList());

            PageResult<DocumentInfo> pageResult = new PageResult<>(documentInfos, page, size, userFileRecordPage.getTotalElements());
            pageResult.setTotalPages(userFileRecordPage.getTotalPages());

            return pageResult;
        } catch (Exception e) {
            log.error("搜索文档列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("搜索文档列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用户文档数量
     */
    public long getDocumentCount(User user) {
        try {
            // 从数据库获取用户的所有文件记录
            List<UserFileRecord> userFileRecords = userFileRecordRepository.findByUserIdAndIsDeleted(user.getId(), 0);
            return userFileRecords.size();
        } catch (Exception e) {
            log.error("获取文档数量失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取文档数量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有文档（保持向后兼容）
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
            // 查找文件记录
            Optional<UserFileRecord> optionalRecord = userFileRecordRepository.findByIdAndUserIdAndIsDeleted(
                    Long.valueOf(documentId), user.getId(), 0);

            if (optionalRecord.isPresent()) {
                UserFileRecord record = optionalRecord.get();

                // 从七牛云删除文件
                try {
                    qiniuUploadService.deleteFile(record.getFilePath());
                } catch (Exception e) {
                    log.warn("从七牛云删除文件失败: {}, 错误: {}", record.getFilePath(), e.getMessage());
                    // 即使删除失败也继续执行其他删除操作
                }

                // 删除向量数据库中的文档（使用数据库记录ID、用户ID和用户名）
                vectorStoreService.deleteDocument(Long.valueOf(documentId), user.getId(), user.getUsername());

                // 删除数据库中的文件记录
                record.setIsDeleted(1); // 标记为已删除
                record.setGmtModified(LocalDateTime.now());
                userFileRecordRepository.save(record);

                // 更新用户存储容量
                updateUserStorageAfterDelete(user, record.getFileSize());
            }

            log.info("文档删除成功: {}", documentId);
        } catch (Exception e) {
            log.error("文档删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文档删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取文档信息
     */
    public DocumentInfo getDocumentById(String documentId, User user) {
        try {
            // 查找文件记录
            Optional<UserFileRecord> optionalRecord = userFileRecordRepository.findByIdAndUserIdAndIsDeleted(
                    Long.valueOf(documentId), user.getId(), 0);

            if (optionalRecord.isPresent()) {
                UserFileRecord record = optionalRecord.get();

                // 转换为DocumentInfo
                DocumentInfo documentInfo = new DocumentInfo();
                documentInfo.setId(record.getId().toString());
                documentInfo.setTitle(record.getFileName());
                documentInfo.setFileName(record.getFilePath());
                documentInfo.setFileSize(record.getFileSize());
                documentInfo.setFileType(record.getFileType());
                documentInfo.setUploadTime(record.getUploadTime());

                return documentInfo;
            }

            return null;
        } catch (Exception e) {
            log.error("获取文档信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取文档信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新用户存储容量（删除文件后）
     *
     * @param user 用户
     * @param fileSize 文件大小（字节）
     */
    public void updateUserStorageAfterDelete(User user, Long fileSize) {
        try {
            // 查找用户最新信息
            Optional<User> optionalUser = userRepository.findById(user.getId());
            if (optionalUser.isPresent()) {
                User updatedUser = optionalUser.get();

                // 更新已使用存储空间（减少文件大小）
                long newUsedStorage = Math.max(0, updatedUser.getUsedStorage() - fileSize);
                updatedUser.setUsedStorage(newUsedStorage);
                updatedUser.setGmtModified(LocalDateTime.now());

                // 保存更新后的用户信息
                userRepository.save(updatedUser);

                log.info("用户 {} 存储容量更新成功，已使用存储: {} 字节", updatedUser.getUsername(), newUsedStorage);
            }
        } catch (Exception e) {
            log.error("更新用户存储容量失败: {}", e.getMessage(), e);
            // 不抛出异常，因为这不影响文档删除的主要功能
        }
    }

    /**
     * 检查用户存储容量是否足够
     *
     * @param user 用户
     * @param fileSize 文件大小（字节）
     * @return 是否足够
     */
    public boolean checkStorageQuota(User user, Long fileSize) {
        try {
            // 查找用户最新信息
            Optional<User> optionalUser = userRepository.findById(user.getId());
            if (optionalUser.isPresent()) {
                User updatedUser = optionalUser.get();

                // 检查存储容量是否足够
                long newUsedStorage = updatedUser.getUsedStorage() + fileSize;
                return newUsedStorage <= updatedUser.getStorageQuota();
            }
        } catch (Exception e) {
            log.error("检查用户存储容量失败: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * 并行处理文件上传和向量库操作
     *
     * @param request 文档请求
     * @param user    用户信息
     * @param fileUrl 文件URL
     * @return DocumentInfo
     * @throws Exception 处理异常
     */
    public DocumentInfo processFileInParallel(DocumentRequest request, User user, String fileUrl) throws Exception {
        // 处理文档
        DocumentInfo documentInfo = new DocumentInfo();
        String documentId = UUID.randomUUID().toString();
        documentInfo.setId(documentId);
        documentInfo.setTitle(request.getTitle() != null ? request.getTitle() : request.getFile().getOriginalFilename());
        documentInfo.setDescription(request.getDescription());
        documentInfo.setTags(request.getTags());
        documentInfo.setUploadTime(LocalDateTime.now());
        documentInfo.setFileName(fileUrl);

        String content = documentProcessor.processDocument(request.getFile(), documentInfo);

        // 分块
        String[] chunks = documentProcessor.chunkText(content);
        documentInfo.setChunkCount(chunks.length);

        // 生成嵌入向量
        List<Embedding> embeddings = embeddingService.embedTextChunks(chunks);

        // 检查用户存储容量是否足够
        if (!checkStorageQuota(user, request.getFile().getSize())) {
            throw new RuntimeException("存储空间不足，请联系管理员");
        }

        // 用于存储操作结果和状态
        UserFileRecord userFileRecord = null;
        boolean databaseSaveSuccess = false;
        boolean vectorStoreSuccess = false;

        try {
            // 并行执行数据库保存和向量库操作
            CompletableFuture<UserFileRecord> databaseFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    log.info("开始保存用户文件记录到数据库: {}", request.getFile().getOriginalFilename());
                    // 保存用户文件记录到数据库
                    UserFileRecord record = new UserFileRecord();
                    record.setUserId(user.getId());
                    record.setFileName(request.getFile().getOriginalFilename());
                    record.setFileSize(request.getFile().getSize());
                    record.setFileType(getFileExtension(request.getFile().getOriginalFilename()));
                    record.setFilePath(fileUrl);
                    record.setUploadTime(LocalDateTime.now());
                    record.setGmtCreate(LocalDateTime.now());
                    record.setGmtModified(LocalDateTime.now());
                    record.setIsDeleted(0);

                    return userFileRecordRepository.save(record);
                } catch (Exception e) {
                    log.error("保存用户文件记录到数据库失败: {}", e.getMessage(), e);
                    throw new RuntimeException("保存用户文件记录到数据库失败: " + e.getMessage(), e);
                }
            }, fileProcessExecutor);

            // 等待数据库保存完成，设置超时时间为30秒
            try {
                userFileRecord = databaseFuture.get(30, TimeUnit.SECONDS);
                databaseSaveSuccess = true;
                log.info("用户文件记录保存到数据库成功: {}", userFileRecord.getId());
            } catch (TimeoutException e) {
                log.error("数据库保存超时: {}", e.getMessage(), e);
                throw new RuntimeException("数据库保存超时: " + e.getMessage(), e);
            }

            // 更新用户存储容量
            updateUserStorageAfterUpload(user, request.getFile().getSize());

            // 存储到向量数据库
            vectorStoreService.storeDocument(userFileRecord.getId(), user.getId(), user.getUsername(), chunks, embeddings, documentInfo);
            vectorStoreSuccess = true;

            log.info("文档处理完成: {}", documentId);

            return documentInfo;
        } catch (Exception e) {
            log.error("并行处理文件失败: {}", e.getMessage(), e);

            // 执行回滚操作
            rollbackOperations(userFileRecord, databaseSaveSuccess, vectorStoreSuccess, user, fileUrl);

            throw new RuntimeException("文件处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 回滚操作
     *
     * @param userFileRecord       用户文件记录
     * @param databaseSaveSuccess  数据库保存是否成功
     * @param vectorStoreSuccess   向量库存储是否成功
     * @param user                 用户信息
     * @param fileUrl              文件URL
     */
    private void rollbackOperations(UserFileRecord userFileRecord,
                                  boolean databaseSaveSuccess,
                                  boolean vectorStoreSuccess, User user, String fileUrl) {
        try {
            // 回滚向量库操作（如果已成功）
            if (vectorStoreSuccess && userFileRecord != null) {
                try {
                    log.info("回滚向量库操作: {}", userFileRecord.getId());
                    vectorStoreService.deleteDocument(userFileRecord.getId(), user.getId(), user.getUsername());
                } catch (Exception e) {
                    log.warn("回滚向量库操作失败: {}", e.getMessage());
                }
            }

            // 回滚用户存储容量更新（如果已成功）
            if (databaseSaveSuccess && userFileRecord != null) {
                try {
                    log.info("回滚用户存储容量更新");
                    updateUserStorageAfterDelete(user, userFileRecord.getFileSize());
                } catch (Exception e) {
                    log.warn("回滚用户存储容量更新失败: {}", e.getMessage());
                }
            }

            // 回滚数据库记录（如果已成功）
            if (databaseSaveSuccess && userFileRecord != null) {
                try {
                    log.info("回滚数据库记录: {}", userFileRecord.getId());
                    // 标记为已删除
                    userFileRecord.setIsDeleted(1);
                    userFileRecord.setGmtModified(LocalDateTime.now());
                    userFileRecordRepository.save(userFileRecord);
                } catch (Exception e) {
                    log.warn("回滚数据库记录失败: {}", e.getMessage());
                }
            }

            // 回滚七牛云文件上传（如果已成功）
            if (fileUrl != null) {
                try {
                    log.info("回滚七牛云文件上传: {}", fileUrl);
                    qiniuUploadService.deleteFile(fileUrl);
                } catch (Exception e) {
                    log.warn("回滚七牛云文件上传失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("回滚操作失败: {}", e.getMessage());
        }
    }
}