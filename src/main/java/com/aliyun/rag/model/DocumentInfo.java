package com.aliyun.rag.model;

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
    
    private Integer vectorCount;
    
    private String processingStatus;
    
    private String downloadUrl;
    
    private Boolean previewAvailable;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public Integer getVectorCount() {
        return vectorCount;
    }

    public void setVectorCount(Integer vectorCount) {
        this.vectorCount = vectorCount;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Boolean getPreviewAvailable() {
        return previewAvailable;
    }

    public void setPreviewAvailable(Boolean previewAvailable) {
        this.previewAvailable = previewAvailable;
    }
}