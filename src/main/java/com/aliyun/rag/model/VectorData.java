package com.aliyun.rag.model;

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

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getFileRecordId() {
        return fileRecordId;
    }

    public void setFileRecordId(String fileRecordId) {
        this.fileRecordId = fileRecordId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}