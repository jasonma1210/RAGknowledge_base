package com.aliyun.rag.service;

import com.aliyun.rag.model.User;
import com.aliyun.rag.model.LoginRequest;
import com.aliyun.rag.model.RegisterRequest;
import com.aliyun.rag.model.AuthResponse;
import com.aliyun.rag.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户认证服务
 * <p>
 * 提供用户注册、登录、认证等服务
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-10
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // 令牌过期时间（小时）
    private static final int TOKEN_EXPIRE_HOURS = 24;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 用户注册
     */
    public AuthResponse register(RegisterRequest request) {
        try {
            // 检查用户名是否已存在
            Optional<User> existingUser = userRepository.findByUsernameAndIsDeleted(request.getUsername(), 0);
            if (existingUser.isPresent()) {
                AuthResponse response = new AuthResponse();
                response.setSuccess(false);
                response.setMessage("用户名已存在");
                return response;
            }

            // 创建新用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword())); // 使用BCrypt加密存储
            user.setEmail(request.getEmail());
            user.setLevel(User.UserLevel.BASIC.getCode()); // 默认为普通用户
            user.setStorageQuota(User.UserLevel.BASIC.getQuota());
            user.setUsedStorage(0L);
            user.setGmtCreate(LocalDateTime.now());
            user.setGmtModified(LocalDateTime.now());
            user.setIsDeleted(0); // 未删除

            // 保存用户到数据库
            User savedUser = userRepository.save(user);

            // 返回认证信息
            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setUser(savedUser);
            response.setMessage("注册成功");

            log.info("用户注册成功: {}", savedUser.getUsername());
            return response;

        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage(), e);
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("注册失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // 查找用户
            Optional<User> optionalUser = userRepository.findByUsernameAndIsDeleted(request.getUsername(), 0);
            if (optionalUser.isEmpty()) {
                AuthResponse response = new AuthResponse();
                response.setSuccess(false);
                response.setMessage("用户名或密码错误");
                return response;
            }

            User user = optionalUser.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                AuthResponse response = new AuthResponse();
                response.setSuccess(false);
                response.setMessage("用户名或密码错误");
                return response;
            }

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            user.setGmtModified(LocalDateTime.now());
            userRepository.save(user);

            // 生成访问令牌
            String token = UUID.randomUUID().toString();

            // 将令牌与用户ID关联存储到Redis中
            redisTemplate.opsForValue().set(token, user.getId(), TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

            // 返回认证信息
            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setToken(token);
            response.setUser(user);
            response.setMessage("登录成功");

            log.info("用户登录成功: {}", user.getUsername());
            return response;

        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage(), e);
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("登录失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 验证访问令牌
     */
    public User validateToken(String token) {
        // 从Redis中查找用户ID
        Object userIdObj = redisTemplate.opsForValue().get(token);
        if (userIdObj == null) {
            return null;
        }

        // 根据用户ID查找用户
        return getUserById((Long) userIdObj);
    }

    /**
     * 根据用户ID获取用户信息
     */
    public User getUserById(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        return optionalUser.orElse(null);
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        redisTemplate.delete(token);
    }
    
    /**
     * 修改用户密码
     *
     * @param user 当前用户
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return AuthResponse
     */
    public AuthResponse changePassword(User user, String oldPassword, String newPassword) {
        try {
            // 验证原密码是否正确
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                AuthResponse response = new AuthResponse();
                response.setSuccess(false);
                response.setMessage("原密码错误");
                return response;
            }
            
            // 检查新密码是否与原密码相同
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                AuthResponse response = new AuthResponse();
                response.setSuccess(false);
                response.setMessage("新密码不能与原密码相同");
                return response;
            }
            
            // 更新用户密码
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setGmtModified(LocalDateTime.now());
            userRepository.save(user);
            
            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("密码修改成功");
            return response;
            
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage(), e);
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("修改密码失败: " + e.getMessage());
            return response;
        }
    }
}