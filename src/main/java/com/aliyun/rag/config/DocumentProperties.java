package com.aliyun.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * 文档处理配置类
 * <p>
 * 集中管理文档处理相关的配置参数，包括分块策略、支持格式等
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
@Component
@Configuration
@ConfigurationProperties(prefix = "document")
public class DocumentProperties {

    /**
     * 文档分块配置
     */
    private ChunkConfig chunk = new ChunkConfig();

    /**
     * 支持的文件格式
     */
    private List<String> supportedFormats = List.of("pdf", "docx", "txt", "md", "epub");

    public ChunkConfig getChunk() {
        return chunk;
    }

    public void setChunk(ChunkConfig chunk) {
        this.chunk = chunk;
    }

    public List<String> getSupportedFormats() {
        return supportedFormats;
    }

    public void setSupportedFormats(List<String> supportedFormats) {
        this.supportedFormats = supportedFormats;
    }

    /**
     * 检查文件格式是否支持
     */
    public boolean isFormatSupported(String format) {
        return supportedFormats.contains(format.toLowerCase());
    }

    /**
     * 文档分块配置
     */
    public static class ChunkConfig {
        
        /**
         * 分块大小（字符数）
         */
        @Min(value = 100, message = "分块大小不能小于100字符")
        @Max(value = 5000, message = "分块大小不能大于5000字符")
        private int size = 1000;
        
        /**
         * 分块重叠字符数
         */
        @Min(value = 0, message = "重叠字符数不能小于0")
        @Max(value = 1000, message = "重叠字符数不能大于1000")
        private int overlap = 200;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getOverlap() {
            return overlap;
        }

        public void setOverlap(int overlap) {
            this.overlap = overlap;
        }
    }
}