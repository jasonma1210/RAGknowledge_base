package com.aliyun.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;

/**
 * 文件处理配置类
 * <p>
 * 集中管理文件上传、处理相关的配置参数
 * 支持通过配置文件动态调整文件处理行为
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
@Component
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {

    /**
     * 最大文件大小（字节），默认100MB
     */
    @Min(value = 1024, message = "文件大小限制不能小于1KB")
    private long maxFileSize = 100 * 1024 * 1024;

    /**
     * 大文件阈值（字节），默认50MB
     * 超过此大小的文件将使用流式处理
     */
    @Min(value = 1024, message = "大文件阈值不能小于1KB")
    private long largeFileThreshold = 50 * 1024 * 1024;

    /**
     * 支持的文件类型
     */
    @NotEmpty(message = "支持的文件类型不能为空")
    private List<String> allowedTypes = Arrays.asList("pdf", "docx", "txt", "md", "epub");

    /**
     * 文件处理缓冲区大小（字节），默认8KB
     */
    @Min(value = 1024, message = "缓冲区大小不能小于1KB")
    private int bufferSize = 8 * 1024;

    /**
     * 是否启用文件类型检查
     */
    private boolean enableTypeValidation = true;

    /**
     * 是否启用文件大小检查
     */
    private boolean enableSizeValidation = true;

    /**
     * 临时文件存储路径
     */
    private String tempDir = System.getProperty("java.io.tmpdir");

    /**
     * 文件处理超时时间（秒），默认5分钟
     */
    @Min(value = 1, message = "处理超时时间不能小于1秒")
    private int processingTimeoutSeconds = 300;

    // Getter和Setter方法
    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public long getLargeFileThreshold() {
        return largeFileThreshold;
    }

    public void setLargeFileThreshold(long largeFileThreshold) {
        this.largeFileThreshold = largeFileThreshold;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(List<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isEnableTypeValidation() {
        return enableTypeValidation;
    }

    public void setEnableTypeValidation(boolean enableTypeValidation) {
        this.enableTypeValidation = enableTypeValidation;
    }

    public boolean isEnableSizeValidation() {
        return enableSizeValidation;
    }

    public void setEnableSizeValidation(boolean enableSizeValidation) {
        this.enableSizeValidation = enableSizeValidation;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public int getProcessingTimeoutSeconds() {
        return processingTimeoutSeconds;
    }

    public void setProcessingTimeoutSeconds(int processingTimeoutSeconds) {
        this.processingTimeoutSeconds = processingTimeoutSeconds;
    }

    /**
     * 获取支持的文件类型数组
     */
    public String[] getAllowedTypesArray() {
        return allowedTypes.toArray(new String[0]);
    }

    /**
     * 检查文件类型是否支持
     */
    public boolean isFileTypeSupported(String fileExtension) {
        return allowedTypes.contains(fileExtension.toLowerCase());
    }
}