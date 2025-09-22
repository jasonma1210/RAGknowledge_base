package com.aliyun.rag.model;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

/**
 * 文档请求模型
 * <p>
 * 用于接收文档上传请求的参数模型
 * 支持文件上传和相关元数据的提交
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
public class DocumentRequest {

    @NotNull(message = "文件不能为空")
    private MultipartFile file;

    private String title;

    private String description;

    private String tags;

    // Getters and Setters
    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}