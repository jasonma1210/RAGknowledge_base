package com.aliyun.rag.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 文件类型验证器
 * <p>
 * 实现文件类型和大小验证逻辑
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
public class FileTypeValidator implements ConstraintValidator<ValidFileType, MultipartFile> {

    private String[] allowedTypes;
    private long maxSize;

    @Override
    public void initialize(ValidFileType constraintAnnotation) {
        this.allowedTypes = constraintAnnotation.allowedTypes();
        this.maxSize = constraintAnnotation.maxSize();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true; // 空文件由@NotNull处理
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("文件大小不能超过 %d MB", maxSize / 1024 / 1024)
            ).addConstraintViolation();
            return false;
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("文件名不能为空")
                    .addConstraintViolation();
            return false;
        }

        String fileExtension = getFileExtension(originalFilename);
        if (fileExtension == null || !Arrays.asList(allowedTypes).contains(fileExtension.toLowerCase())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("不支持的文件类型: %s，支持的类型: %s", 
                            fileExtension, String.join(", ", allowedTypes))
            ).addConstraintViolation();
            return false;
        }

        // 检查文件名安全性
        if (containsInvalidCharacters(originalFilename)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("文件名包含非法字符")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    /**
     * 检查文件名是否包含非法字符
     */
    private boolean containsInvalidCharacters(String filename) {
        // 检查是否包含路径遍历字符
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return true;
        }
        
        // 检查是否包含特殊字符
        String invalidChars = "[]{}|;:<>?*";
        for (char c : invalidChars.toCharArray()) {
            if (filename.indexOf(c) != -1) {
                return true;
            }
        }
        
        return false;
    }
}
