package com.aliyun.rag.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 自定义验证注解：文件类型验证
 * <p>
 * 用于验证上传文件的类型是否符合要求
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Documented
@Constraint(validatedBy = FileTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileType {
    
    String message() default "不支持的文件类型";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 允许的文件类型
     */
    String[] allowedTypes() default {"pdf", "docx", "txt", "md", "epub"};
    
    /**
     * 最大文件大小（字节）
     */
    long maxSize() default 104857600L; // 100MB
}
