package com.aliyun.rag.util;

import com.aliyun.rag.model.User;
import com.aliyun.rag.model.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 用户上下文辅助工具
 * <p>
 * 统一处理用户对象的获取和转换，消除Controller层的重复代码
 * </p>
 *
 * @author Claude Code
 * @version 1.0.0
 * @since 2025-10-31
 */
@Component
public class UserContextHelper {

    private static final String CURRENT_USER_ATTR = "currentUser";

    /**
     * 从请求中获取当前用户
     *
     * @param request HTTP请求对象
     * @return User对象，如果不存在则返回null
     */
    public static User getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getAttribute(CURRENT_USER_ATTR);
        return userObj instanceof User ? (User) userObj : null;
    }

    /**
     * 从请求中获取当前用户DTO
     *
     * @param request HTTP请求对象
     * @return UserDTO对象，如果不存在则返回null
     */
    public static UserDTO getCurrentUserDTO(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null ? UserDTO.fromUser(user) : null;
    }

    /**
     * 从DTO创建User对象（用于Service层）
     * <p>
     * 注意：此方法创建的User对象只包含基本信息，不包含完整的数据库记录
     * 如需完整信息，应通过UserRepository查询
     * </p>
     *
     * @param dto UserDTO对象
     * @return User对象
     */
    public static User createUserFromDTO(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setLevel(dto.getLevel());
        user.setStorageQuota(dto.getStorageQuota());
        user.setUsedStorage(dto.getUsedStorage());
        return user;
    }

    /**
     * 从请求中创建User对象（一步到位）
     * <p>
     * 这是最常用的方法，直接从HttpServletRequest创建可用的User对象
     * </p>
     *
     * @param request HTTP请求对象
     * @return User对象，如果请求中不包含用户信息则返回null
     */
    public static User createUserFromRequest(HttpServletRequest request) {
        UserDTO dto = getCurrentUserDTO(request);
        return createUserFromDTO(dto);
    }

    /**
     * 检查请求中是否包含用户信息
     *
     * @param request HTTP请求对象
     * @return 如果包含用户信息返回true，否则返回false
     */
    public static boolean hasUser(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    /**
     * 获取当前用户ID
     *
     * @param request HTTP请求对象
     * @return 用户ID，如果不存在返回null
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户名
     *
     * @param request HTTP请求对象
     * @return 用户名，如果不存在返回null
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null ? user.getUsername() : null;
    }
}
