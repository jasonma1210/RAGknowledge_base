package com.aliyun.rag.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档信息模型
 * <p>
 * 用于存储和管理文档的元数据信息
 * 包含文档的基本属性、文件信息和处理状态
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
@Data
public class DocumentInfo {
    
    private String id;
    
    private String title;
    
    private String description;
    
    private String fileName;
    
    private String fileType;
    
    private Long fileSize;
    
    private String tags;
    
    private LocalDateTime uploadTime;
    
    private Integer chunkCount;
    

}