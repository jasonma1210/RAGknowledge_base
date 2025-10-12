package com.aliyun.rag.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 搜索查询验证器
 * <p>
 * 实现搜索查询的格式和安全性验证
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
public class SearchQueryValidator implements ConstraintValidator<ValidSearchQuery, String> {

    private int minLength;
    private int maxLength;
    private boolean allowSpecialChars;
    
    // SQL注入检测模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror|onclick)"
    );
    
    // XSS检测模式
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)(<script|</script|javascript:|vbscript:|onload|onerror|onclick|onmouseover|onfocus|onblur)"
    );

    @Override
    public void initialize(ValidSearchQuery constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.allowSpecialChars = constraintAnnotation.allowSpecialChars();
    }

    @Override
    public boolean isValid(String query, ConstraintValidatorContext context) {
        if (query == null) {
            return true; // 空值由@NotNull处理
        }

        String trimmedQuery = query.trim();
        
        // 检查长度
        if (trimmedQuery.length() < minLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("搜索查询长度不能少于 %d 个字符", minLength)
            ).addConstraintViolation();
            return false;
        }
        
        if (trimmedQuery.length() > maxLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("搜索查询长度不能超过 %d 个字符", maxLength)
            ).addConstraintViolation();
            return false;
        }

        // 检查SQL注入
        if (SQL_INJECTION_PATTERN.matcher(trimmedQuery).find()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("搜索查询包含非法SQL关键字")
                    .addConstraintViolation();
            return false;
        }

        // 检查XSS攻击
        if (XSS_PATTERN.matcher(trimmedQuery).find()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("搜索查询包含非法脚本内容")
                    .addConstraintViolation();
            return false;
        }

        // 检查特殊字符（如果配置不允许）
        if (!allowSpecialChars && containsSpecialChars(trimmedQuery)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("搜索查询包含不允许的特殊字符")
                    .addConstraintViolation();
            return false;
        }

        // 检查是否只包含空白字符
        if (trimmedQuery.matches("\\s*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("搜索查询不能只包含空白字符")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * 检查是否包含特殊字符
     */
    private boolean containsSpecialChars(String query) {
        // 定义不允许的特殊字符
        String specialChars = "!@#$%^&*()+=[]{}|;:'\"<>?/~`";
        
        for (char c : specialChars.toCharArray()) {
            if (query.indexOf(c) != -1) {
                return true;
            }
        }
        
        return false;
    }
}
