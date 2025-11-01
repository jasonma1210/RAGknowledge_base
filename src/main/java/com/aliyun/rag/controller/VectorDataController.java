package com.aliyun.rag.controller;

import com.aliyun.rag.model.PageResult;
import com.aliyun.rag.model.User;
import com.aliyun.rag.model.VectorData;
import com.aliyun.rag.model.R;
import com.aliyun.rag.model.dto.UserDTO;
import com.aliyun.rag.service.AuthService;
import com.aliyun.rag.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 向量数据控制器
 * <p>
 * 提供向量数据查询、统计等RESTful接口
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-15
 */
@RestController
@RequestMapping("/api/vector-data")

public class VectorDataController {

    private static final Logger log = LoggerFactory.getLogger(VectorDataController.class);

    private final VectorStoreService vectorStoreService;
    private final AuthService authService;

    public VectorDataController(VectorStoreService vectorStoreService, AuthService authService) {
        this.vectorStoreService = vectorStoreService;
        this.authService = authService;
    }

    /**
     * 分页获取用户的向量集合列表
     */
    @GetMapping
    public ResponseEntity<R<PageResult<VectorData>>> getUserVectors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        // 获取当前用户
        User currentUser = (User) httpRequest.getAttribute("currentUser");

        // 获取用户的向量集合列表
        PageResult<VectorData> pageResult = vectorStoreService.getUserVectors(
                currentUser.getId(), currentUser.getUsername(), page, size);

        return ResponseEntity.ok(R.success(pageResult));
    }

    /**
     * 获取用户的向量统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<R<Map<String, Object>>> getUserVectorStats(HttpServletRequest httpRequest) {
        // 获取当前用户
        User currentUser = (User) httpRequest.getAttribute("currentUser");

        // 转换为UserDTO以避免敏感信息泄露
        UserDTO currentUserDTO = UserDTO.fromUser(currentUser);

        // 获取用户的向量统计信息
        Map<String, Object> stats = vectorStoreService.getUserVectorStats(
                currentUserDTO.getId(), currentUserDTO.getUsername());
        return ResponseEntity.ok(R.success(stats));
    }
}