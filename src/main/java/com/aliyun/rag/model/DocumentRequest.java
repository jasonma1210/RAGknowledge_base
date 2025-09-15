package com.aliyun.rag.model;

import lombok.Data;
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
@Data
public class DocumentRequest {

    @NotNull(message = "文件不能为空")
    private MultipartFile file;

    private String title;

    private String description;

    private String tags;


}