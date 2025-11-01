package com.aliyun.rag.controller;

import com.aliyun.rag.model.*;
import com.aliyun.rag.model.dto.UserDTO;
import com.aliyun.rag.service.RAGService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.io.IOException;

/**
 * 搜索控制器
 * <p>
 * 提供知识库搜索和智能问答功能
 * 支持语义搜索、关键词搜索、混合搜索和问答接口
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
@RestController
@RequestMapping("/api/search")

public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    
    private final RAGService ragService;

    public SearchController(RAGService ragService) {
        this.ragService = ragService;
    }

    /**
     * 搜索知识库（支持分页）
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<R<PageResult<SearchResult>>> searchKnowledgeBase(@Valid @RequestBody SearchRequest request, 
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                HttpServletRequest httpRequest) {
        // 获取当前用户
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        
        // 转换为UserDTO以避免敏感信息泄露
        UserDTO currentUserDTO = UserDTO.fromUser(currentUser);
        
        // 创建一个User实体用于兼容现有服务方法（仅包含必要信息）
        User user = new User();
        user.setId(currentUserDTO.getId());
        user.setUsername(currentUserDTO.getUsername());
        
        PageResult<SearchResult> pageResult = ragService.searchKnowledgeBase(request, user, page, size);
        return ResponseEntity.ok(R.success(pageResult));
    }

    /**
     * 问答接口
     */
    @PostMapping("/ask")
    public ResponseEntity<R<Map<String, Object>>> askQuestion(@RequestBody QuestionRequest questionRequest,
                                         HttpServletRequest httpRequest) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(questionRequest.getQuestion());
        searchRequest.setSearchType(SearchRequest.SearchType.valueOf(questionRequest.getSearchType()));
        searchRequest.setMaxResults(questionRequest.getMaxResults());
        searchRequest.setMinScore(questionRequest.getMinScore());
        
        // 获取当前用户
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        
        // 转换为UserDTO以避免敏感信息泄露
        UserDTO currentUserDTO = UserDTO.fromUser(currentUser);
        
        // 创建一个User实体用于兼容现有服务方法（仅包含必要信息）
        User user = new User();
        user.setId(currentUserDTO.getId());
        user.setUsername(currentUserDTO.getUsername());

        Map<String, Object> response = ragService.askQuestion(questionRequest.getQuestion(), searchRequest, user);
        return ResponseEntity.ok(R.success(response));
    }

    /**
     * 流式问答接口
     */
    @PostMapping("/ask/streaming")
    public SseEmitter askQuestionStreaming(@RequestBody QuestionRequest questionRequest,
                                         HttpServletRequest httpRequest) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(questionRequest.getQuestion());
        searchRequest.setSearchType(SearchRequest.SearchType.valueOf(questionRequest.getSearchType()));
        searchRequest.setMaxResults(questionRequest.getMaxResults());
        searchRequest.setMinScore(questionRequest.getMinScore());
        
        // 获取当前用户
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        
        // 转换为UserDTO以避免敏感信息泄露
        UserDTO currentUserDTO = UserDTO.fromUser(currentUser);
        
        // 创建一个User实体用于兼容现有服务方法（仅包含必要信息）
        User user = new User();
        user.setId(currentUserDTO.getId());
        user.setUsername(currentUserDTO.getUsername());

        // 创建SseEmitter用于流式响应
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // 调用流式问答服务
        ragService.askQuestionStreaming(questionRequest.getQuestion(), searchRequest, user, 
            new dev.langchain4j.model.StreamingResponseHandler<dev.langchain4j.data.message.AiMessage>() {
                @Override
                public void onNext(String token) {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(token));
                    } catch (IOException e) {
                        log.error("发送流式响应失败: {}", e.getMessage(), e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onComplete(dev.langchain4j.model.output.Response<dev.langchain4j.data.message.AiMessage> response) {
                    try {
                        // 发送完成信号
                        emitter.send(SseEmitter.event().name("complete").data("completed"));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("发送完成信号失败: {}", e.getMessage(), e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("流式问答出错: {}", throwable.getMessage(), throwable);
                    emitter.completeWithError(throwable);
                }
            });

        return emitter;
    }

    /**
     * 简单搜索接口（支持分页）
     */
    @GetMapping("/simple")
    public ResponseEntity<R<PageResult<SearchResult>>> simpleSearch(@RequestParam String query,
                                         @RequestParam(defaultValue = "SEMANTIC") String type,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         HttpServletRequest httpRequest) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setSearchType(SearchRequest.SearchType.valueOf(type.toUpperCase()));
        request.setMaxResults(size);
        
        // 获取当前用户
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        
        // 转换为UserDTO以避免敏感信息泄露
        UserDTO currentUserDTO = UserDTO.fromUser(currentUser);
        
        // 创建一个User实体用于兼容现有服务方法（仅包含必要信息）
        User user = new User();
        user.setId(currentUserDTO.getId());
        user.setUsername(currentUserDTO.getUsername());
        
        PageResult<SearchResult> pageResult = ragService.searchKnowledgeBase(request, user, page, size);
        return ResponseEntity.ok(R.success(pageResult));
    }
}