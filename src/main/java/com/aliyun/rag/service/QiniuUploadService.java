package com.aliyun.rag.service;

import com.aliyun.rag.model.User;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 七牛云文件上传服务
 * <p>
 * 提供文件上传到七牛云存储的功能
 * 支持多种文件类型和自动生成文件名
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
@Service
public class QiniuUploadService {
    
    private static final Logger log = LoggerFactory.getLogger(QiniuUploadService.class);
    
    @Value("${qiniu.access-key}")
    private String accessKey;
    
    @Value("${qiniu.secret-key}")
    private String secretKey;
    
    @Value("${qiniu.bucket}")
    private String bucket;
    
    @Value("${qiniu.domain}")
    private String domain;
    
    private UploadManager uploadManager;
    private Auth auth;
    
    @PostConstruct
    public void init() {
        // 初始化七牛云配置
        Configuration cfg = new Configuration(Region.autoRegion());
        uploadManager = new UploadManager(cfg);
        auth = Auth.create(accessKey, secretKey);
    }
    
    /**
     * 上传文件到七牛云
     * 
     * @param file 要上传的文件
     * @param user 用户信息
     * @param fileName 文件名（可选，如果为空则自动生成）
     * @return 文件访问URL
     * @throws RuntimeException 上传失败时抛出异常
     */
    public String uploadFile(MultipartFile file, User user, String fileName) {
        try {
            // 如果文件名为空，则生成唯一文件名
            if (fileName == null || fileName.trim().isEmpty()) {
                String originalName = file.getOriginalFilename();
                String extension = getFileExtension(originalName);
                fileName = UUID.randomUUID() + (extension != null ? "." + extension : "");
            }
            
            // 生成用户目录结构：用户名/年月/文件名
            String userDir = user.getUsername();
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String key = userDir + "/" + dateDir + "/" + fileName;
            
            // 生成上传凭证
            String upToken = auth.uploadToken(bucket);
            
            // 上传文件
            Response response = uploadManager.put(file.getBytes(), key, upToken);
            
            if (response.isOK()) {
                log.info("文件上传成功: {}", key);
                return domain + "/" + key;
            } else {
                log.error("文件上传失败: {}", response.error);
                throw new RuntimeException("文件上传失败: " + response.error);
            }
        } catch (QiniuException e) {
            log.error("七牛云上传异常: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("文件读取异常: {}", e.getMessage(), e);
            throw new RuntimeException("文件读取失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 上传文件到七牛云（自动生成文件名）
     * 
     * @param file 要上传的文件
     * @param user 用户信息
     * @return 文件访问URL
     * @throws RuntimeException 上传失败时抛出异常
     */
    public String uploadFile(MultipartFile file, User user) {
        return uploadFile(file, user, null);
    }
    
    /**
     * 上传文件到七牛云（兼容旧接口）
     * 
     * @param file 要上传的文件
     * @param fileName 文件名
     * @return 文件访问URL
     * @throws RuntimeException 上传失败时抛出异常
     */
    public String uploadFile(MultipartFile file, String fileName) {
        // 创建一个默认用户用于兼容旧接口
        User defaultUser = new User();
        defaultUser.setUsername("default");
        return uploadFile(file, defaultUser, fileName);
    }
    
    /**
     * 上传文件到七牛云（兼容旧接口，自动生成文件名）
     * 
     * @param file 要上传的文件
     * @return 文件访问URL
     * @throws RuntimeException 上传失败时抛出异常
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, (String) null);
    }
    
    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}