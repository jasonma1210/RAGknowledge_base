package com.aliyun.rag.controller;

import com.aliyun.rag.model.PageResult;
import com.aliyun.rag.model.User;
import com.aliyun.rag.model.Conversation;
import com.aliyun.rag.model.ConversationMessage;
import com.aliyun.rag.model.SearchRequest;
import com.aliyun.rag.model.SearchResult;
import com.aliyun.rag.model.dto.*;
import com.aliyun.rag.repository.ConversationRepository;
import com.aliyun.rag.repository.ConversationMessageRepository;
import com.aliyun.rag.service.ConversationService;
import com.aliyun.rag.service.RAGService;
import com.aliyun.rag.util.UserContextHelper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * AI对话会话控制器
 * <p>
 * 提供会话管理、消息历史、AI问答等API接口
 * </p>
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ConversationController.class);
    private final ConversationService conversationService;
    private final RAGService ragService;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;

    public ConversationController(ConversationService conversationService, RAGService ragService, 
                                 ConversationRepository conversationRepository,
                                 ConversationMessageRepository messageRepository) {
        this.conversationService = conversationService;
        this.ragService = ragService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * 获取会话列表（分页）
     *
     * @param request HTTP请求
     * @param page    页码（从0开始）
     * @param size    每页大小
     * @return 会话分页列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getConversations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User user = UserContextHelper.createUserFromRequest(request);
        PageResult<ConversationDTO> pageResult = conversationService.getConversations(user, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", pageResult.getData());
        response.put("page", pageResult.getPage());
        response.put("size", pageResult.getSize());
        response.put("total", pageResult.getTotal());
        response.put("totalPages", pageResult.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * 获取会话详情（包含所有消息）
     *
     * @param request        HTTP请求
     * @param conversationId 会话ID
     * @return 会话消息列表
     */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> getConversationMessages(
            HttpServletRequest request,
            @PathVariable Long conversationId) {

        User user = UserContextHelper.createUserFromRequest(request);
        List<ConversationMessageDTO> messages = conversationService
                .getConversationMessages(user, conversationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", messages);
        response.put("count", messages.size());

        return ResponseEntity.ok(response);
    }

    /**
     * AI问答（支持上下文记忆）
     *
     * @param request     HTTP请求
     * @param askRequest  问答请求
     * @return 问答响应
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askWithContext(
            HttpServletRequest request,
            @RequestBody ConversationAskRequest askRequest) {

        User user = UserContextHelper.createUserFromRequest(request);
        ConversationAskResponse askResponse = conversationService.askWithContext(user, askRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", askResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * 创建新会话
     *
     * @param request HTTP请求
     * @param body    请求体（可选参数）
     * @return 新会话信息
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createConversation(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {

        User user = UserContextHelper.createUserFromRequest(request);
        
        // 创建空会话，生成带时间戳的会话名称
        String sessionName = "对话 " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm:ss"));
        com.aliyun.rag.model.Conversation conversation = conversationService
                .createConversation(user, sessionName);

        // 返回会话信息
        ConversationDTO dto = ConversationDTO.fromEntity(conversation);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dto);

        return ResponseEntity.ok(response);
    }

    /**
     * 删除会话（软删除）
     *
     * @param request        HTTP请求
     * @param conversationId 会话ID
     * @return 删除结果
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Map<String, Object>> deleteConversation(
            HttpServletRequest request,
            @PathVariable Long conversationId) {

        User user = UserContextHelper.createUserFromRequest(request);
        conversationService.deleteConversation(user, conversationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "会话删除成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 流式问答接口（同步阻塞方案）
     */
    @PostMapping("/ask/streaming")
    public SseEmitter askQuestionStreaming(@RequestBody ConversationAskRequest askRequest,
                                          HttpServletRequest httpRequest) {
        User user = UserContextHelper.createUserFromRequest(httpRequest);
        
        // 创建SseEmitter用于流式响应
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        try {
            log.info("开始处理流式问答请求: conversationId={}, question={}", 
                askRequest.getConversationId(), askRequest.getQuestion());
                
            // 1. 获取或创建会话
            Conversation conversation;
            if (askRequest.getConversationId() != null) {
                conversation = conversationService.getConversationById(askRequest.getConversationId(), user.getId())
                        .orElseThrow(() -> new RuntimeException("会话不存在"));
            } else {
                conversation = conversationService.createConversation(user, askRequest.getQuestion());
            }

            // 2. 保存用户问题（在事务中）
            ConversationMessage userMessage = conversationService.saveMessage(
                    conversation.getId(),
                    user.getId(),
                    ConversationMessage.MessageType.USER,
                    askRequest.getQuestion(),
                    null,
                    null
            );
            log.info("用户问题已保存: conversationId={}, messageId={}", 
                conversation.getId(), userMessage.getId());

            // 3. 构建搜索请求
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setQuery(askRequest.getQuestion());
            searchRequest.setSearchType(askRequest.getSearchType());
            searchRequest.setMaxResults(askRequest.getMaxResults());
            searchRequest.setMinScore(askRequest.getMinScore());

            // 4. 搜索知识库
            List<SearchResult> searchResults = ragService.searchKnowledgeBase(searchRequest, user);

            // 5. 构建上下文
            List<ChatMessage> contextMessages = new ArrayList<>();
            if (Boolean.TRUE.equals(askRequest.getEnableContext())) {
                contextMessages = conversationService.buildContextMessages(conversation.getId(),
                        askRequest.getContextMessageCount());
            }

            // 6. 使用CompletableFuture处理流式响应，然后在HTTP请求线程中保存
            processStreamingResponseWithSave(
                    conversation,
                    user,
                    askRequest,
                    searchResults,
                    contextMessages,
                    emitter);

        } catch (Exception e) {
            log.error("流式问答处理失败: conversationId={}, question={}, error={}", 
                askRequest.getConversationId(), askRequest.getQuestion(), e.getMessage(), e);
            try {
                emitter.send(SseEmitter.event().name("error").data("流式问答处理失败: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ioException) {
                log.error("发送错误信号失败: {}", ioException.getMessage(), ioException);
                emitter.completeWithError(ioException);
            }
        }

        return emitter;
    }

    /**
     * 获取会话统计信息
     *
     * @param request HTTP请求
     * @return 统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(HttpServletRequest request) {
        User user = UserContextHelper.createUserFromRequest(request);
        ConversationService.ConversationStats stats = conversationService.getConversationStats(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);

        return ResponseEntity.ok(response);
    }

    /**
     * 处理流式响应并保存到数据库（同步阻塞方案）
     */
    private void processStreamingResponseWithSave(
            Conversation conversation,
            User user,
            ConversationAskRequest askRequest,
            List<SearchResult> searchResults,
            List<ChatMessage> contextMessages,
            SseEmitter emitter) {
        
        // 使用CountDownLatch来同步流式响应和保存操作
        CountDownLatch completionLatch = new CountDownLatch(1);
        StringBuilder fullAnswer = new StringBuilder();
        Exception[] errorHolder = new Exception[1];
        
        // 启动流式响应处理
        ragService.askQuestionStreamingWithContext(
                askRequest.getQuestion(),
                searchResults,
                contextMessages,
                user,
                new StreamingResponseHandler<AiMessage>() {

                    @Override
                    public void onNext(String token) {
                        try {
                            fullAnswer.append(token);
                            emitter.send(SseEmitter.event().name("message").data(token));
                        } catch (IOException e) {
                            log.error("发送流式响应失败: {}", e.getMessage(), e);
                            errorHolder[0] = e;
                            emitter.completeWithError(e);
                            completionLatch.countDown();
                        }
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        try {
                            log.info("流式响应完成，准备在HTTP请求线程中保存AI回答: conversationId={}, answerLength={}", 
                                conversation.getId(), fullAnswer.length());
                            
                            // 流式响应完成，释放锁，让HTTP请求线程继续处理保存
                            completionLatch.countDown();
                            
                        } catch (Exception e) {
                            log.error("处理流式响应完成事件失败: {}", e.getMessage(), e);
                            errorHolder[0] = e;
                            completionLatch.countDown();
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("流式问答出错: {}", error.getMessage(), error);
                        errorHolder[0] = new RuntimeException("流式问答出错: " + error.getMessage(), error);
                        completionLatch.countDown();
                    }
                });
        
        // 直接在HTTP请求线程中等待流式响应完成，然后处理保存
        try {
            // 等待流式响应完成，最多等待60秒
            boolean completed = completionLatch.await(60, TimeUnit.SECONDS);
            
            if (!completed) {
                throw new RuntimeException("流式响应超时");
            }
            
            if (errorHolder[0] != null) {
                throw errorHolder[0];
            }
            
            // 在HTTP请求线程的上下文中保存AI回答
            String answer = fullAnswer.toString();
            log.info("开始在HTTP请求线程中保存AI回答: conversationId={}, answerLength={}", 
                conversation.getId(), answer.length());
            
            ConversationMessage assistantMessage = saveAIResponseDirectly(
                    conversation.getId(),
                    user.getId(),
                    answer,
                    searchResults.size(),
                    askRequest.getSearchType().name(),
                    searchResults
            );
            
            log.info("AI回答已保存到数据库: conversationId={}, messageId={}", 
                conversation.getId(), assistantMessage.getId());

            // 发送完成信号（包含保存的消息ID）
            emitter.send(SseEmitter.event().name("complete").data(assistantMessage.getId().toString()));
            emitter.complete();
            
        } catch (Exception e) {
            log.error("保存会话消息失败: {}", e.getMessage(), e);
            try {
                emitter.send(SseEmitter.event().name("error").data("保存消息失败: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ioException) {
                emitter.completeWithError(ioException);
            }
        }
    }

    /**
     * 直接保存AI回答到数据库（在HTTP请求线程中执行）
     */
    @Transactional(rollbackFor = Exception.class)
    private ConversationMessage saveAIResponseDirectly(Long conversationId, Long userId, String answer, 
                                                    Integer sourceCount, String searchType,
                                                    List<SearchResult> searchResults) {
        log.info("直接保存AI回答: conversationId={}, answerLength={}", conversationId, answer.length());
        
        try {
            // 创建AI回答消息
            ConversationMessage assistantMessage = new ConversationMessage();
            assistantMessage.setConversationId(conversationId);
            assistantMessage.setUserId(userId);
            assistantMessage.setMessageType(ConversationMessage.MessageType.ASSISTANT);
            assistantMessage.setContent(answer);
            assistantMessage.setSourceCount(sourceCount);
            assistantMessage.setSearchType(searchType);
            
            // 使用repository保存消息
            ConversationMessage saved = messageRepository.save(assistantMessage);
            
            // 立即刷新确保ID生成
            messageRepository.flush();
            
            log.info("直接保存AI回答成功: conversationId={}, messageId={}", 
                conversationId, saved.getId());
            
            if (saved.getId() == null) {
                throw new RuntimeException("直接保存AI回答失败：ID为空");
            }
            
            // 保存引用来源
            conversationService.saveMessageSources(saved.getId(), conversationId, searchResults);
            
            // 更新会话信息
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("会话不存在"));
            conversationService.updateConversationAfterMessage(conversation);
            
            log.info("直接保存AI回答完成: conversationId={}, messageId={}", conversationId, saved.getId());
            return saved;
            
        } catch (Exception e) {
            log.error("直接保存AI回答失败: {}", e.getMessage(), e);
            throw new RuntimeException("直接保存AI回答失败: " + e.getMessage(), e);
        }
    }

    
}
