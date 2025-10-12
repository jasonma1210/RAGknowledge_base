package com.aliyun.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据加密服务
 * <p>
 * 提供敏感数据加密存储和文件内容加密功能
 * 使用AES加密算法保护敏感数据
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-10-12
 */
@Service
public class EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

    // 默认加密算法
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    @Value("${encryption.key:}")
    private String encryptionKey;

    // 缓存密钥以提高性能
    private final Map<String, SecretKey> keyCache = new HashMap<>();

    /**
     * 敏感数据加密存储
     * 
     * @param data 待加密的数据
     * @return 加密后的数据
     */
    public String encryptSensitiveData(String data) {
        try {
            if (data == null || data.isEmpty()) {
                return data;
            }

            log.debug("加密敏感数据，长度: {}", data.length());

            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            String encryptedData = Base64.getEncoder().encodeToString(encryptedBytes);

            log.debug("敏感数据加密完成，加密后长度: {}", encryptedData.length());
            return encryptedData;

        } catch (Exception e) {
            log.error("敏感数据加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("敏感数据加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 敏感数据解密
     * 
     * @param encryptedData 加密的数据
     * @return 解密后的数据
     */
    public String decryptSensitiveData(String encryptedData) {
        try {
            if (encryptedData == null || encryptedData.isEmpty()) {
                return encryptedData;
            }

            log.debug("解密敏感数据，长度: {}", encryptedData.length());

            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            String decryptedData = new String(decryptedBytes);

            log.debug("敏感数据解密完成，解密后长度: {}", decryptedData.length());
            return decryptedData;

        } catch (Exception e) {
            log.error("敏感数据解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("敏感数据解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 文件内容加密
     * 
     * @param content 待加密的文件内容
     * @return 加密后的文件内容
     */
    public byte[] encryptFileContent(byte[] content) {
        try {
            if (content == null || content.length == 0) {
                return content;
            }

            log.debug("加密文件内容，长度: {}", content.length);

            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(content);

            log.debug("文件内容加密完成，加密后长度: {}", encryptedBytes.length);
            return encryptedBytes;

        } catch (Exception e) {
            log.error("文件内容加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件内容加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 文件内容解密
     * 
     * @param encryptedContent 加密的文件内容
     * @return 解密后的文件内容
     */
    public byte[] decryptFileContent(byte[] encryptedContent) {
        try {
            if (encryptedContent == null || encryptedContent.length == 0) {
                return encryptedContent;
            }

            log.debug("解密文件内容，长度: {}", encryptedContent.length);

            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedBytes = cipher.doFinal(encryptedContent);

            log.debug("文件内容解密完成，解密后长度: {}", decryptedBytes.length);
            return decryptedBytes;

        } catch (Exception e) {
            log.error("文件内容解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件内容解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成或获取密钥
     * 
     * @return AES密钥
     */
    private SecretKey getSecretKey() {
        // 检查缓存中是否有密钥
        if (keyCache.containsKey(encryptionKey)) {
            return keyCache.get(encryptionKey);
        }

        try {
            SecretKey secretKey;
            
            if (encryptionKey != null && !encryptionKey.isEmpty()) {
                // 使用配置的密钥
                byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
                secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            } else {
                // 生成新的密钥
                KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
                keyGenerator.init(256); // 使用256位密钥
                secretKey = keyGenerator.generateKey();
                
                // 记录生成的密钥（仅用于开发环境）
                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
                log.warn("生成新的加密密钥: {}", encodedKey);
                log.warn("请在生产环境中配置固定的加密密钥！");
            }

            // 缓存密钥
            keyCache.put(encryptionKey, secretKey);
            return secretKey;

        } catch (Exception e) {
            log.error("获取密钥失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取密钥失败: " + e.getMessage(), e);
        }
    }

    /**
     * 设置加密密钥（用于测试）
     * 
     * @param key 密钥
     */
    public void setEncryptionKey(String key) {
        this.encryptionKey = key;
        // 清除缓存
        keyCache.clear();
    }

    /**
     * 获取加密密钥（用于测试）
     * 
     * @return 加密密钥
     */
    public String getEncryptionKey() {
        return encryptionKey;
    }
}