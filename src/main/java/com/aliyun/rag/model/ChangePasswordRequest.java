package com.aliyun.rag.model;

import lombok.Data;

/**
 * 修改密码请求
 * <p>
 * 用于用户修改密码接口的请求参数
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-15
 */
@Data
public class ChangePasswordRequest {

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

}