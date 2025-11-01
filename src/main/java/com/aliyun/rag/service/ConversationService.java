package com.aliyun.rag.service;

import com.aliyun.rag.exception.BusinessException;
import com.aliyun.rag.model.*;
import com.aliyun.rag.model.dto.*;
import com.aliyun.rag.repository.*;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI对话会话服务
 * <p>
 * 提供会话管理、消息历史记录、上下文记忆等功能
 * </p>
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Service
@EnableAsync
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;
    private final ConversationMessageSourceRepository messageSourceRepository;
    private final RAGService ragService;
    private final AuditLogService auditLogService;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public ConversationService(ConversationRepository conversationRepository,
                               ConversationMessageRepository messageRepository,
                               ConversationMessageSourceRepository messageSourceRepository,
                               RAGService ragService,
                               AuditLogService auditLogService,
                               EntityManager entityManager,
                               TransactionTemplate transactionTemplate) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messageSourceRepository = messageSourceRepository;
        this.ragService = ragService;
        this.auditLogService = auditLogService;
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * 根据ID获取会话
     *
     * @param conversationId 会话ID
     * @param userId        用户ID
     * @return 会话信息
     */
    public Optional<Conversation> getConversationById(Long conversationId, Long userId) {
        return conversationRepository.findByIdAndUserIdAndIsDeleted(conversationId, userId, 0);
    }

    /**
     * 创建新会话
     *
     * @param user         用户信息
     * @param firstQuestion 首个问题
     * @return 创建的会话
     */
    public Conversation createConversation(User user, String firstQuestion) {
        try {
            Conversation conversation = new Conversation();
            conversation.setUserId(user.getId());
            conversation.setSessionName(Conversation.generateSessionName(firstQuestion));
            conversation.setMessageCount(0);
            conversation.setLastMessageTime(LocalDateTime.now());

            Conversation saved = conversationRepository.save(conversation);

            log.info("创建新会话成功: conversationId={}, userId={}, sessionName={}",
                    saved.getId(), user.getId(), saved.getSessionName());

            return saved;

        } catch (Exception e) {
            log.error("创建会话失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "创建会话失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用户的会话列表（分页）
     *
     * @param user 用户
     * @param page 页码
     * @param size 每页大小
     * @return 会话分页列表
     */
    public PageResult<ConversationDTO> getConversations(User user, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Conversation> conversationPage = conversationRepository
                    .findByUserIdAndIsDeletedOrderByLastMessageTimeDesc(user.getId(), 0, pageable);

            List<ConversationDTO> dtoList = conversationPage.getContent().stream()
                    .map(ConversationDTO::fromEntity)
                    .collect(Collectors.toList());

            PageResult<ConversationDTO> result = new PageResult<>(
                    dtoList, page, size, conversationPage.getTotalElements());
            result.setTotalPages(conversationPage.getTotalPages());

            return result;

        } catch (Exception e) {
            log.error("获取会话列表失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "获取会话列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取会话的所有消息
     *
     * @param user           用户
     * @param conversationId 会话ID
     * @return 消息列表
     */
    public List<ConversationMessageDTO> getConversationMessages(User user, Long conversationId) {
        try {
            // 验证会话所有权
            Conversation conversation = conversationRepository
                    .findByIdAndUserIdAndIsDeleted(conversationId, user.getId(), 0)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND, "会话不存在"));

            List<ConversationMessage> messages = messageRepository
                    .findByConversationIdAndIsDeletedOrderByGmtCreateAsc(conversationId, 0);

            return messages.stream()
                    .map(ConversationMessageDTO::fromEntity)
                    .collect(Collectors.toList());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取会话消息失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "获取会话消息失败: " + e.getMessage(), e);
        }
    }

    /**
     * AI问答（支持上下文记忆）
     *
     * @param user    用户
     * @param request 问答请求
     * @return 问答响应
     */
    @Transactional(rollbackFor = Exception.class)
    public ConversationAskResponse askWithContext(User user, ConversationAskRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取或创建会话
            Conversation conversation;
            if (request.getConversationId() != null) {
                conversation = conversationRepository
                        .findByIdAndUserIdAndIsDeleted(request.getConversationId(), user.getId(), 0)
                        .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND, "会话不存在"));
            } else {
                conversation = createConversation(user, request.getQuestion());
            }

            // 2. 保存用户问题
            ConversationMessage userMessage = saveMessage(
                    conversation.getId(),
                    user.getId(),
                    ConversationMessage.MessageType.USER,
                    request.getQuestion(),
                    null,
                    null
            );

            // 3. 构建上下文（如果启用）
            List<ChatMessage> contextMessages = new ArrayList<>();
            if (Boolean.TRUE.equals(request.getEnableContext())) {
                contextMessages = buildContextMessages(conversation.getId(),
                        request.getContextMessageCount());
            }

            // 4. 调用RAG服务搜索相关知识
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setQuery(request.getQuestion());
            searchRequest.setSearchType(request.getSearchType());
            searchRequest.setMaxResults(request.getMaxResults());
            searchRequest.setMinScore(request.getMinScore());

            List<SearchResult> searchResults = ragService.searchKnowledgeBase(searchRequest, user);

            // 5. 生成AI回答（带上下文）
            String answer = ragService.generateAnswerWithContext(
                    request.getQuestion(),
                    searchResults,
                    contextMessages
            );

            // 6. 保存AI回答
            ConversationMessage assistantMessage = saveMessage(
                    conversation.getId(),
                    user.getId(),
                    ConversationMessage.MessageType.ASSISTANT,
                    answer,
                    searchResults.size(),
                    request.getSearchType().name()
            );

            // 7. 保存引用来源
            saveMessageSources(assistantMessage.getId(), conversation.getId(), searchResults);

            // 8. 更新会话信息
            updateConversationAfterMessage(conversation);

            // 9. 记录审计日志
            auditLogService.logAIQuestion(user, request.getQuestion(),
                    answer.length(), searchResults.size());

            // 10. 构建响应
            long responseTime = System.currentTimeMillis() - startTime;
            return buildAskResponse(conversation, assistantMessage, request.getQuestion(),
                    answer, searchResults, (int) responseTime);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI问答失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED,
                    "AI问答失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除会话（软删除）
     *
     * @param user           用户
     * @param conversationId 会话ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(User user, Long conversationId) {
        try {
            // 验证会话所有权
            Conversation conversation = conversationRepository
                    .findByIdAndUserIdAndIsDeleted(conversationId, user.getId(), 0)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND, "会话不存在"));

            // 软删除会话
            conversation.setIsDeleted(1);
            conversation.setGmtModified(LocalDateTime.now());
            conversationRepository.save(conversation);

            // 软删除所有消息
            messageRepository.softDeleteByConversationId(conversationId);

            log.info("删除会话成功: conversationId={}, userId={}", conversationId, user.getId());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除会话失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "删除会话失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取会话统计信息
     *
     * @param user 用户
     * @return 统计信息
     */
    public ConversationStats getConversationStats(User user) {
        try {
            long totalConversations = conversationRepository
                    .countByUserIdAndIsDeleted(user.getId(), 0);

            List<ConversationMessage> allMessages = messageRepository
                    .findByUserIdAndIsDeletedOrderByGmtCreateDesc(user.getId(), 0);

            long totalMessages = allMessages.size();
            long totalQuestions = allMessages.stream()
                    .filter(m -> m.getMessageType() == ConversationMessage.MessageType.USER)
                    .count();

            ConversationStats stats = new ConversationStats();
            stats.setTotalConversations(totalConversations);
            stats.setTotalMessages(totalMessages);
            stats.setTotalQuestions(totalQuestions);

            return stats;

        } catch (Exception e) {
            log.error("获取会话统计失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "获取会话统计失败: " + e.getMessage(), e);
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 保存消息到数据库
     */
    @Transactional
    public ConversationMessage saveMessage(Long conversationId, Long userId,
                                            ConversationMessage.MessageType messageType,
                                            String content,
                                            Integer sourceCount,
                                            String searchType) {
        ConversationMessage message = new ConversationMessage();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setSourceCount(sourceCount != null ? sourceCount : 0);
        message.setSearchType(searchType);

        ConversationMessage saved = messageRepository.saveAndFlush(message);
        
        log.info("消息保存成功: messageId={}, conversationId={}, messageType={}", 
            saved.getId(), conversationId, messageType);
        
        // 验证ID不为空
        if (saved.getId() == null) {
            throw new RuntimeException("消息保存失败：ID为空");
        }
        
        return saved;
    }

    /**
     * 保存消息来源引用
     */
    public void saveMessageSources(Long messageId, Long conversationId,
                                    List<SearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return;
        }

        log.info("开始保存消息来源引用: messageId={}, sourceCount={}", messageId, searchResults.size());
        
        for (SearchResult result : searchResults) {
            ConversationMessageSource source = new ConversationMessageSource();
            source.setMessageId(messageId);
            source.setConversationId(conversationId);
            source.setDocumentId(result.getFileRecordId());
            source.setDocumentTitle(result.getTitle());
            source.setChunkContent(result.getContent());
            source.setRelevanceScore(result.getScore());

            messageSourceRepository.save(source);
        }
        
        log.info("消息来源引用保存完成: messageId={}", messageId);
    }

    /**
     * 构建上下文消息列表（用于LangChain4j）
     */
    public List<ChatMessage> buildContextMessages(Long conversationId, Integer limit) {
        List<ConversationMessage> recentMessages = messageRepository
                .findRecentMessages(conversationId, 0, limit != null ? limit : 10);

        List<ChatMessage> contextMessages = new ArrayList<>();
        // 反转顺序（从旧到新）
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            ConversationMessage message = recentMessages.get(i);
            if (message.getMessageType() == ConversationMessage.MessageType.USER) {
                contextMessages.add(new UserMessage(message.getContent()));
            } else {
                contextMessages.add(new AiMessage(message.getContent()));
            }
        }

        return contextMessages;
    }

    /**
     * 更新会话信息（消息数量、最后消息时间）
     */
    public void updateConversationAfterMessage(Conversation conversation) {
        long messageCount = messageRepository
                .countByConversationIdAndIsDeleted(conversation.getId(), 0);

        conversation.setMessageCount((int) messageCount);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        log.info("会话信息更新完成: conversationId={}, messageCount={}", 
            conversation.getId(), messageCount);
    }

    /**
     * 保存AI回答到数据库（声明式事务）
     */
    @Transactional(rollbackFor = Exception.class)
    public ConversationMessage saveAIResponse(Long conversationId, Long userId, String answer, 
                                            Integer sourceCount, String searchType,
                                            List<SearchResult> searchResults) {
        log.info("开始保存AI回答: conversationId={}, answerLength={}", conversationId, answer.length());
        
        try {
            // 创建AI回答消息
            ConversationMessage assistantMessage = new ConversationMessage();
            assistantMessage.setConversationId(conversationId);
            assistantMessage.setUserId(userId);
            assistantMessage.setMessageType(ConversationMessage.MessageType.ASSISTANT);
            assistantMessage.setContent(answer);
            assistantMessage.setSourceCount(sourceCount);
            assistantMessage.setSearchType(searchType);
            
            // 使用repository保存消息，确保事务管理
            ConversationMessage saved = messageRepository.save(assistantMessage);
            
            log.info("AI回答已保存: conversationId={}, messageId={}", 
                conversationId, saved.getId());
            
            if (saved.getId() == null) {
                throw new RuntimeException("保存AI回答失败：ID为空");
            }
            
            // 保存引用来源
            saveMessageSources(saved.getId(), conversationId, searchResults);
            
            // 更新会话信息
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("会话不存在"));
            updateConversationAfterMessage(conversation);
            
            log.info("AI回答保存完成: conversationId={}, messageId={}", conversationId, saved.getId());
            return saved;
            
        } catch (Exception e) {
            log.error("保存AI回答失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存AI回答失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步保存AI回答到数据库（简化版本，确保在HTTP请求线程中执行）
     */
    @Transactional(rollbackFor = Exception.class)
    public ConversationMessage saveAIResponseSync(Long conversationId, Long userId, String answer, 
                                                Integer sourceCount, String searchType,
                                                List<SearchResult> searchResults) {
        log.info("开始同步保存AI回答: conversationId={}, answerLength={}", conversationId, answer.length());
        
        try {
            // 创建AI回答消息
            ConversationMessage assistantMessage = new ConversationMessage();
            assistantMessage.setConversationId(conversationId);
            assistantMessage.setUserId(userId);
            assistantMessage.setMessageType(ConversationMessage.MessageType.ASSISTANT);
            assistantMessage.setContent(answer);
            assistantMessage.setSourceCount(sourceCount);
            assistantMessage.setSearchType(searchType);
            
            // 使用简单的save方法，让Spring管理事务
            ConversationMessage saved = messageRepository.save(assistantMessage);
            
            // 立即刷新确保ID生成
            messageRepository.flush();
            
            log.info("同步保存AI回答成功: conversationId={}, messageId={}", 
                conversationId, saved.getId());
            
            if (saved.getId() == null) {
                throw new RuntimeException("同步保存AI回答失败：ID为空");
            }
            
            // 保存引用来源
            saveMessageSources(saved.getId(), conversationId, searchResults);
            
            // 更新会话信息
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("会话不存在"));
            updateConversationAfterMessage(conversation);
            
            log.info("同步保存AI回答完成: conversationId={}, messageId={}", conversationId, saved.getId());
            return saved;
            
        } catch (Exception e) {
            log.error("同步保存AI回答失败: {}", e.getMessage(), e);
            throw new RuntimeException("同步保存AI回答失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建问答响应
     */
    private ConversationAskResponse buildAskResponse(Conversation conversation,
                                                     ConversationMessage assistantMessage,
                                                     String question, String answer,
                                                     List<SearchResult> sources,
                                                     int responseTime) {
        ConversationAskResponse response = new ConversationAskResponse();
        response.setConversationId(conversation.getId());
        response.setSessionName(conversation.getSessionName());
        response.setMessageId(assistantMessage.getId());
        response.setQuestion(question);
        response.setAnswer(answer);
        response.setSources(sources);
        response.setSourceCount(sources.size());
        response.setResponseTime(responseTime);
        response.setTokenCount(0); // TODO: 实际计算token数量
        response.setCreateTime(assistantMessage.getGmtCreate());

        return response;
    }

    /**
     * 会话统计信息DTO
     */
    @lombok.Data
    public static class ConversationStats {
        private Long totalConversations;
        private Long totalMessages;
        private Long totalQuestions;
    }
}
