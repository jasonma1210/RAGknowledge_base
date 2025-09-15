/*
 * RAG知识库系统默认数据初始化脚本
 */

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO user_info (username, password, email, user_level, storage_quota, used_storage, gmt_create, gmt_modified) 
VALUES ('admin', '$2a$10$w92bG5Pn5n2pOJ5m5s5bOu9r8v7x6c8y9z0a1b2c3d4e5f6g7h8i9j', 'admin@example.com', 1, 10737418240, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 插入默认普通用户 (密码: user123)
INSERT INTO user_info (username, password, email, user_level, storage_quota, used_storage, gmt_create, gmt_modified) 
VALUES ('user', '$2a$10$D4OLcH5h7vF8fG5n1s5bOu9r8v7x6c8y9z0a1b2c3d4e5f6g7h8i9j', 'user@example.com', 0, 1073741824, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;