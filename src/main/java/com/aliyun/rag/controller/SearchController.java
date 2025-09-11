package com.aliyun.rag.controller;

import com.aliyun.rag.model.SearchRequest;
import com.aliyun.rag.model.SearchResult;
import com.aliyun.rag.model.User;
import com.aliyun.rag.service.RAGService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

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
@CrossOrigin(origins = "*")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    
    private final RAGService ragService;

    public SearchController(RAGService ragService) {
        this.ragService = ragService;
    }

    /**
     * 搜索知识库
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<?> searchKnowledgeBase(@Valid @RequestBody SearchRequest request, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户
            User currentUser = (User) httpRequest.getAttribute("currentUser");
            
            List<SearchResult> results = ragService.searchKnowledgeBase(request, currentUser);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("搜索失败", e);
            return ResponseEntity.badRequest().body("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 问答接口
     */
    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestParam String question,
                                        @RequestParam(defaultValue = "SEMANTIC") String searchType,
                                        @RequestParam(defaultValue = "10") Integer maxResults,
                                        @RequestParam(defaultValue = "0.7") Double minScore,
                                        HttpServletRequest httpRequest) {
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setQuery(question);
            searchRequest.setSearchType(SearchRequest.SearchType.valueOf(searchType));
            searchRequest.setMaxResults(maxResults);
            searchRequest.setMinScore(minScore);
            
            // 获取当前用户
            User currentUser = (User) httpRequest.getAttribute("currentUser");

            Map<String, Object> response = ragService.askQuestion(question, searchRequest, currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("问答失败", e);
            return ResponseEntity.badRequest().body("问答失败: " + e.getMessage());
        }
    }

    /**
     * 简单搜索接口
     */
    @GetMapping("/simple")
    public ResponseEntity<?> simpleSearch(@RequestParam String query,
                                         @RequestParam(defaultValue = "SEMANTIC") String type,
                                         @RequestParam(defaultValue = "10") Integer limit,
                                         HttpServletRequest httpRequest) {
        try {
            SearchRequest request = new SearchRequest();
            request.setQuery(query);
            request.setSearchType(SearchRequest.SearchType.valueOf(type.toUpperCase()));
            request.setMaxResults(limit);
            
            // 获取当前用户
            User currentUser = (User) httpRequest.getAttribute("currentUser");
            
            List<SearchResult> results = ragService.searchKnowledgeBase(request, currentUser);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("简单搜索失败", e);
            return ResponseEntity.badRequest().body("搜索失败: " + e.getMessage());
        }
    }
}