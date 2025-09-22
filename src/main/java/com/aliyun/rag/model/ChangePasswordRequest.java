package com.aliyun.rag.model;

/**
 * 修改密码请求
 * <p>
 * 用于用户修改密码接口的请求参数
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-10
 */
public class ChangePasswordRequest {

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    // Getters and Setters
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}