package com.aliyun.rag.repository;

import com.aliyun.rag.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户信息Repository
 * <p>
 * 提供用户信息的数据库操作接口
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-11
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据用户名查找未删除的用户
     * 
     * @param username 用户名
     * @param isDeleted 是否删除
     * @return 用户信息
     */
    Optional<User> findByUsernameAndIsDeleted(String username, Integer isDeleted);
}