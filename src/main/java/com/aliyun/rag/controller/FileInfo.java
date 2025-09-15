package com.aliyun.rag.controller;

import lombok.Data;

/**
 * 文件信息包装类
 * <p>
 * 用于在异步处理中传递文件信息
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-15
 */
@Data
public class FileInfo {
    private byte[] bytes;
    private String originalFilename;
    private String contentType;

    public FileInfo(byte[] bytes, String originalFilename, String contentType) {
        this.bytes = bytes;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }
}