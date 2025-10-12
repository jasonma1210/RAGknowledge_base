-- RAG知识库系统数据库索引优化脚本
-- 用于提升查询性能和系统响应速度

-- 用户表索引优化
CREATE INDEX IF NOT EXISTS idx_user_username ON user(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);
CREATE INDEX IF NOT EXISTS idx_user_level ON user(level);
CREATE INDEX IF NOT EXISTS idx_user_gmt_create ON user(gmt_create);

-- 用户文件记录表索引优化
CREATE INDEX IF NOT EXISTS idx_user_file_record_user_id ON user_file_record(user_id);
CREATE INDEX IF NOT EXISTS idx_user_file_record_file_type ON user_file_record(file_type);
CREATE INDEX IF NOT EXISTS idx_user_file_record_upload_time ON user_file_record(upload_time);
CREATE INDEX IF NOT EXISTS idx_user_file_record_is_deleted ON user_file_record(is_deleted);
CREATE INDEX IF NOT EXISTS idx_user_file_record_user_type ON user_file_record(user_id, file_type);

-- 文档Milvus映射表索引优化
CREATE INDEX IF NOT EXISTS idx_document_milvus_mapping_file_id ON document_milvus_mapping(file_record_id);
CREATE INDEX IF NOT EXISTS idx_document_milvus_mapping_user_id ON document_milvus_mapping(user_id);
CREATE INDEX IF NOT EXISTS idx_document_milvus_mapping_is_deleted ON document_milvus_mapping(is_deleted);
CREATE INDEX IF NOT EXISTS idx_document_milvus_mapping_vector_index ON document_milvus_mapping(vector_index);
CREATE INDEX IF NOT EXISTS idx_document_milvus_mapping_user_file ON document_milvus_mapping(user_id, file_record_id);

-- 搜索历史表索引优化（如果存在）
-- CREATE INDEX IF NOT EXISTS idx_search_history_user_id ON search_history(user_id);
-- CREATE INDEX IF NOT EXISTS idx_search_history_search_time ON search_history(search_time);
-- CREATE INDEX IF NOT EXISTS idx_search_history_query ON search_history(query);

-- 系统日志表索引优化（如果存在）
-- CREATE INDEX IF NOT EXISTS idx_system_log_level ON system_log(level);
-- CREATE INDEX IF NOT EXISTS idx_system_log_create_time ON system_log(create_time);
-- CREATE INDEX IF NOT EXISTS idx_system_log_user_id ON system_log(user_id);

-- 复合索引优化（提升复杂查询性能）
CREATE INDEX IF NOT EXISTS idx_user_file_record_user_deleted ON user_file_record(user_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_document_milvus_user_deleted ON document_milvus_mapping(user_id, is_deleted);

-- 全文索引优化（用于搜索功能）
-- 注意：MySQL 8.0+支持全文索引，可以根据需要启用
-- ALTER TABLE user_file_record ADD FULLTEXT(file_name);
-- ALTER TABLE document_milvus_mapping ADD FULLTEXT(milvus_id);

-- 分区表优化建议（适用于大数据量场景）
-- 可以考虑按时间分区用户文件记录表
-- ALTER TABLE user_file_record PARTITION BY RANGE (YEAR(upload_time)) (
--     PARTITION p2024 VALUES LESS THAN (2025),
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p2026 VALUES LESS THAN (2027),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 统计信息更新（提升查询优化器性能）
ANALYZE TABLE user;
ANALYZE TABLE user_file_record;
ANALYZE TABLE document_milvus_mapping;

-- 查看索引使用情况的查询语句
-- SHOW INDEX FROM user_file_record;
-- SHOW INDEX FROM document_milvus_mapping;

-- 性能监控查询
-- 查看慢查询日志
-- SHOW VARIABLES LIKE 'slow_query_log';
-- SHOW VARIABLES LIKE 'long_query_time';

-- 查看表大小和索引大小
-- SELECT 
--     table_name,
--     ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size in MB'
-- FROM information_schema.tables 
-- WHERE table_schema = 'rag_knowledge_base'
-- ORDER BY (data_length + index_length) DESC;
