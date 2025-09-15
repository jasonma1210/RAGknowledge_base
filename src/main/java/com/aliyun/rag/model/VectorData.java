package com.aliyun.rag.model;

import lombok.Data;

/**
 * 向量数据模型
 * <p>
 * 用于表示存储在Milvus中的向量数据，包括向量ID、文本内容、元数据等信息
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-15
 */
@Data
public class VectorData {
    
    /**
     * 向量ID（Milvus中的主键）
     */
    private String id;
    
    /**
     * 文本内容
     */
    private String text;
    
    /**
     * 显示用的截断文本（不超过一行）
     */
    private String displayText;
    
    /**
     * 文件记录ID
     */
    private String fileRecordId;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 分块索引
     */
    private Integer chunkIndex;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 创建时间
     */
    private String createTime;
}