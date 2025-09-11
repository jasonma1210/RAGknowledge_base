/*
 * RAG知识库系统数据库初始化脚本
 * 
 * 遵循阿里巴巴开发规范：
 * 1. 表名使用小写字母，单词间用下划线分隔
 * 2. 字段名使用小写字母，单词间用下划线分隔
 * 3. 主键统一命名为id，类型为BIGINT且自增
 * 4. 所有表都包含gmt_create、gmt_modified、is_deleted字段
 * 5. 时间字段使用datetime类型
 * 6. 状态字段使用tinyint类型
 * 7. 字符串字段根据长度选择varchar或text类型
 */

-- 删除已存在的表
DROP TABLE IF EXISTS document_milvus_mapping;
DROP TABLE IF EXISTS user_file_record;
DROP TABLE IF EXISTS user_info;

-- 用户信息表
CREATE TABLE user_info (
    -- 主键ID
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    
    -- 用户名（唯一）
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    
    -- 密码（BCrypt加密存储）
    password VARCHAR(100) NOT NULL COMMENT '密码',
    
    -- 邮箱
    email VARCHAR(100) COMMENT '邮箱',
    
    -- 用户等级（0:普通用户 1:进阶用户）
    user_level TINYINT NOT NULL DEFAULT 0 COMMENT '用户等级 0:普通用户 1:进阶用户',
    
    -- 存储配额（字节）
    storage_quota BIGINT NOT NULL DEFAULT 0 COMMENT '存储配额（字节）',
    
    -- 已使用存储空间（字节）
    used_storage BIGINT NOT NULL DEFAULT 0 COMMENT '已使用存储空间（字节）',
    
    -- 最后登录时间
    last_login_time DATETIME COMMENT '最后登录时间',
    
    -- 创建时间
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 修改时间
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    
    -- 是否删除（0:未删除 1:已删除）
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0:未删除 1:已删除',
    
    -- 唯一索引
    UNIQUE KEY uk_username (username),
    
    -- 主键约束
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 用户文件上传记录表
CREATE TABLE user_file_record (
    -- 主键ID
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    
    -- 用户ID（外键）
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
#     -- 文档ID（与Milvus中的ID对应）
#     document_id VARCHAR(50) NOT NULL COMMENT '文档ID（与Milvus中的ID对应）',
    
    -- 文件名
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    
    -- 文件路径
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    
    -- 文件大小（字节）
    file_size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    
    -- 文件类型
    file_type VARCHAR(50) COMMENT '文件类型',

    -- 上传时间
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    
    -- 创建时间
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 修改时间
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    
    -- 是否删除（0:未删除 1:已删除）
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0:未删除 1:已删除',
    
    -- 索引
    KEY idx_user_id (user_id),
    KEY idx_upload_time (upload_time),
    
    -- 主键约束
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文件上传记录表';

-- 文档与Milvus向量ID映射表
CREATE TABLE document_milvus_mapping (
    -- 主键ID
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    
    -- 文件记录ID（对应user_file_record表的id）
    file_record_id BIGINT NOT NULL COMMENT '文件记录ID',
    
    -- Milvus向量ID
    milvus_id VARCHAR(100) NOT NULL COMMENT 'Milvus向量ID',
    
    -- 向量索引（在文档中的位置）
    vector_index INT NOT NULL DEFAULT 0 COMMENT '向量索引（在文档中的位置）',
    
    -- 创建时间
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 修改时间
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    
    -- 是否删除（0:未删除 1:已删除）
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0:未删除 1:已删除',
    
    -- 索引
    KEY idx_file_record_id (file_record_id),
    KEY idx_milvus_id (milvus_id),
    
    -- 主键约束
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档与Milvus向量ID映射表';