package com.aliyun.rag.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 自定义验证注解：搜索查询验证
 * <p>
 * 用于验证搜索查询的合法性和安全性
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Documented
@Constraint(validatedBy = SearchQueryValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSearchQuery {
    
    String message() default "搜索查询格式不正确";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 最小查询长度
     */
    int minLength() default 1;
    
    /**
     * 最大查询长度
     */
    int maxLength() default 500;
    
    /**
     * 是否允许特殊字符
     */
    boolean allowSpecialChars() default true;
}
