package com.aliyun.rag.service;

import com.aliyun.rag.model.User;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
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
    private BucketManager bucketManager;

    @PostConstruct
    public void init() {
        if (isConfigValid()) {
            Configuration cfg = new Configuration(Region.region0());
            uploadManager = new UploadManager(cfg);
            auth = Auth.create(accessKey, secretKey);
            bucketManager = new BucketManager(auth, cfg);
            log.info("七牛云服务初始化成功");
        } else {
            log.warn("七牛云配置无效，服务将不可用");
        }
    }
    
    /**
     * 验证配置是否有效
     */
    private boolean isConfigValid() {
        return accessKey != null && !accessKey.trim().isEmpty() &&
               secretKey != null && !secretKey.trim().isEmpty() &&
               bucket != null && !bucket.trim().isEmpty() &&
               domain != null && !domain.trim().isEmpty() &&
               !accessKey.equals("123") &&
               !secretKey.equals("321") &&
               !bucket.equals("3333");
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
        // 验证配置是否有效
        if (!isConfigValid()) {
            log.error("七牛云配置无效，无法上传文件");
            throw new RuntimeException("七牛云服务配置无效，请联系管理员");
        }
        
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
     * 删除七牛云文件
     *
     * @param fileKey 文件key
     * @throws RuntimeException 删除失败时抛出异常
     */
    public void deleteFile(String fileKey) {
        if (!isConfigValid()) {
            log.warn("七牛云配置无效，跳过文件删除: {}", fileKey);
            return;
        }
        
        try {
            // 从URL中提取文件key（去掉域名部分）
            if (fileKey.startsWith(domain)) {
                fileKey = fileKey.substring(domain.length() + 1);
            }

            // 删除文件
            Response response = bucketManager.delete(bucket, fileKey);

            if (response.isOK()) {
                log.info("文件删除成功: {}", fileKey);
            } else {
                log.error("文件删除失败: {}", response.error);
                // 不抛出异常，避免影响主流程
            }
        } catch (QiniuException e) {
            log.error("七牛云删除异常: {}", e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 从七牛云下载文件
     *
     * @param fileKey 文件key
     * @return 文件数据
     * @throws RuntimeException 下载失败时抛出异常
     */
    public byte[] downloadFile(String fileKey) {
        if (!isConfigValid()) {
            log.warn("七牛云配置无效，无法下载文件: {}", fileKey);
            return null;
        }
        
        try {
            // 从URL中提取文件key（去掉域名部分）
            if (fileKey.startsWith(domain)) {
                fileKey = fileKey.substring(domain.length() + 1);
            }

            // 检查文件是否存在
            com.qiniu.storage.model.FileInfo fileInfo = bucketManager.stat(bucket, fileKey);
            
            if (fileInfo == null) {
                log.warn("文件不存在: {}", fileKey);
                return null;
            }
            
            // 获取文件下载URL（带过期时间）
            String downloadUrl = auth.privateDownloadUrl(domain + "/" + fileKey, 3600); // 1小时过期
            
            // 下载文件内容
            java.net.URL url = new java.net.URL(downloadUrl);
            java.io.InputStream inputStream = url.openStream();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            inputStream.close();
            return baos.toByteArray();
        } catch (QiniuException e) {
            log.error("七牛云查询文件信息异常: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("文件下载异常: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
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

    /**
     * 检查七牛云配置是否有效（公共方法）
     *
     * @return 配置是否有效
     */
    public boolean isConfigValidPublic() {
        return isConfigValid();
    }
}