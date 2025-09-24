package com.aliyun.rag.service;

import com.aliyun.rag.exception.BusinessException;
import com.aliyun.rag.model.User;
import com.aliyun.rag.model.LoginRequest;
import com.aliyun.rag.model.RegisterRequest;
import com.aliyun.rag.model.AuthResponse;
import com.aliyun.rag.model.ErrorCode;
import com.aliyun.rag.model.dto.UserDTO;
import com.aliyun.rag.repository.UserRepository;
import com.aliyun.rag.util.SensitiveDataMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 用户认证服务
 * <p>
 * 提供用户注册、登录、认证等服务
 * 使用JWT Token替代传统的UUID Token
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserRepository userRepository, 
                      RedisTemplate<String, Object> redisTemplate,
                      JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.redisTemplate = redisTemplate;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 用户注册
     */
    public AuthResponse register(RegisterRequest request) {
        try {
            // 检查用户名是否已存在
            Optional<User> existingUser = userRepository.findByUsernameAndIsDeleted(request.getUsername(), 0);
            if (existingUser.isPresent()) {
                return AuthResponse.failure(ErrorCode.USER_ALREADY_EXISTS);
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
            UserDTO userDTO = UserDTO.fromUser(savedUser);
            AuthResponse response = AuthResponse.success(null, userDTO, "注册成功");

            log.info("用户注册成功: {}", savedUser.getUsername());
            return response;

        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
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
                return AuthResponse.failure(ErrorCode.AUTH_FAILED);
            }

            User user = optionalUser.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return AuthResponse.failure(ErrorCode.AUTH_FAILED);
            }

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            user.setGmtModified(LocalDateTime.now());
            userRepository.save(user);

            // 生成JWT访问令牌
            String accessToken = jwtTokenService.generateAccessToken(user);
            String refreshToken = jwtTokenService.generateRefreshToken(user);

            // 将refresh token存储到Redis中
            String refreshTokenKey = "refresh_token:" + user.getId();
            redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 7, TimeUnit.DAYS);

            // 返回认证信息
            UserDTO userDTO = UserDTO.fromUser(user);
            AuthResponse response = AuthResponse.success(accessToken, userDTO, "登录成功");
            response.setRefreshToken(refreshToken);

            log.info("用户登录成功: {}", user.getUsername());
            return response;

        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
        }
    }

    /**
     * 验证JWT访问令牌
     */
    public User validateToken(String token) {
        try {
            // 验证JWT Token有效性
            if (!jwtTokenService.validateToken(token)) {
                return null;
            }

            // 从令牌中获取用户ID
            Long userId = jwtTokenService.getUserIdFromToken(token);
            if (userId == null) {
                return null;
            }

            // 根据用户ID查找用户
            return getUserById(userId);
        } catch (Exception e) {
            log.error("验证Token失败: {}", e.getMessage());
            return null;
        }
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
    public AuthResponse logout(String token) {
        try {
            // 从令牌中获取用户ID
            Long userId = jwtTokenService.getUserIdFromToken(token);
            if (userId != null) {
                // 删除Redis中的refresh token
                String refreshTokenKey = "refresh_token:" + userId;
                redisTemplate.delete(refreshTokenKey);
                log.info("用户 {} 登出成功", userId);
            }
            return AuthResponse.success(null, null, "登出成功");
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登出失败");
        }
    }
    
    /**
     * 获取用户信息(基于Token)
     */
    public User getProfile(String token) {
        if (token == null || token.isEmpty()) {
            throw new BusinessException(ErrorCode.TOKEN_MISSING);
        }
        
        User user = validateToken(token);
        if (user == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        
        return user;
    }
    
    /**
     * 修改用户密码(基于Token)
     */
    public AuthResponse changePassword(String token, String oldPassword, String newPassword) {
        if (token == null || token.isEmpty()) {
            throw new BusinessException(ErrorCode.TOKEN_MISSING);
        }
        
        User user = validateToken(token);
        if (user == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        
        return changeUserPassword(user, oldPassword, newPassword);
    }
    
    /**
     * 修改用户密码(基于User对象)
     */
    public AuthResponse changePassword(User user, String oldPassword, String newPassword) {
        return changeUserPassword(user, oldPassword, newPassword);
    }
    
    /**
     * 用户登出(基于User对象)
     */
    public AuthResponse logout(User user) {
        try {
            // 删除Redis中的refresh token
            String refreshTokenKey = "refresh_token:" + user.getId();
            redisTemplate.delete(refreshTokenKey);
            log.info("用户 {} 登出成功", user.getUsername());
            return AuthResponse.success(null, null, "登出成功");
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登出失败");
        }
    }
    
    /**
     * 修改用户密码
     *
     * @param user 当前用户
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return AuthResponse
     */
    private AuthResponse changeUserPassword(User user, String oldPassword, String newPassword) {
        try {
            // 验证原密码是否正确
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return AuthResponse.failure(ErrorCode.PASSWORD_INCORRECT);
            }
            
            // 检查新密码是否与原密码相同
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                return AuthResponse.failure(ErrorCode.PASSWORD_SAME);
            }
            
            // 更新用户密码
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setGmtModified(LocalDateTime.now());
            userRepository.save(user);
            
            return AuthResponse.success(null, null, "密码修改成功");
            
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改密码失败");
        }
    }
    
    /**
     * 刷新Token
     * 
     * @param refreshToken 刷新令牌
     * @return 新的认证信息
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // 验证刷新令牌有效性
            if (!jwtTokenService.validateToken(refreshToken)) {
                return AuthResponse.failure(ErrorCode.TOKEN_INVALID, "无效的刷新令牌");
            }
            
            // 检查Token类型是否为REFRESH
            String tokenType = jwtTokenService.getTokenType(refreshToken);
            if (!"REFRESH".equals(tokenType)) {
                return AuthResponse.failure(ErrorCode.TOKEN_INVALID, "令牌类型不正确");
            }
            
            // 从令牌中获取用户ID
            Long userId = jwtTokenService.getUserIdFromToken(refreshToken);
            if (userId == null) {
                return AuthResponse.failure(ErrorCode.TOKEN_INVALID, "无法获取用户信息");
            }
            
            // 从Redis中获取存储的刷新令牌进行比对
            String refreshTokenKey = "refresh_token:" + userId;
            String storedRefreshToken = (String) redisTemplate.opsForValue().get(refreshTokenKey);
            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                return AuthResponse.failure(ErrorCode.TOKEN_INVALID, "刷新令牌不匹配或已过期");
            }
            
            // 获取用户信息
            User user = getUserById(userId);
            if (user == null) {
                return AuthResponse.failure(ErrorCode.USER_NOT_FOUND, "用户不存在");
            }
            
            // 生成新的Access Token
            String newAccessToken = jwtTokenService.refreshAccessToken(refreshToken, user);
            
            // 返回新的认证信息
            UserDTO userDTO = UserDTO.fromUser(user);
            AuthResponse response = AuthResponse.success(newAccessToken, userDTO, "Token刷新成功");
            response.setRefreshToken(refreshToken); // 刷新令牌保持不变
            
            log.info("用户 {} Token刷新成功", user.getUsername());
            return response;
            
        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage(), e);
            return AuthResponse.failure(ErrorCode.SYSTEM_ERROR, "Token刷新失败");
        }
    }
}