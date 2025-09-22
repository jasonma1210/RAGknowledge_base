package com.aliyun.rag.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 敏感数据脱敏工具类
 * <p>
 * 提供各种敏感信息的脱敏处理功能
 * 用于日志记录和API响应中的敏感信息保护
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Component
public class SensitiveDataMasker {

    /**
     * 脱敏密码，全部用星号替换
     *
     * @param password 原密码
     * @return 脱敏后的密码
     */
    public String maskPassword(String password) {
        if (!StringUtils.hasText(password)) {
            return "";
        }
        return "*".repeat(Math.min(password.length(), 8));
    }

    /**
     * 脱敏邮箱地址
     * 保留前缀首字符和@后面的域名，中间用****替换
     *
     * @param email 原邮箱地址
     * @return 脱敏后的邮箱地址
     */
    public String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        if (parts.length != 2) {
            return email;
        }

        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 1) {
            return email;
        }

        String maskedLocal = localPart.charAt(0) + "****" + localPart.charAt(localPart.length() - 1);
        return maskedLocal + "@" + domain;
    }

    /**
     * 脱敏手机号码
     * 保留前3位和后4位，中间用****替换
     *
     * @param phone 原手机号码
     * @return 脱敏后的手机号码
     */
    public String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }

        if (phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        } else {
            return phone.substring(0, 3) + "****";
        }
    }

    /**
     * 脱敏身份证号码
     * 保留前6位和后4位，中间用****替换
     *
     * @param idCard 原身份证号码
     * @return 脱敏后的身份证号码
     */
    public String maskIdCard(String idCard) {
        if (!StringUtils.hasText(idCard) || idCard.length() < 10) {
            return idCard;
        }

        if (idCard.length() == 18) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        } else if (idCard.length() == 15) {
            return idCard.substring(0, 6) + "*****" + idCard.substring(11);
        } else {
            return idCard.substring(0, 3) + "****";
        }
    }

    /**
     * 脱敏银行卡号
     * 保留前4位和后4位，中间用****替换
     *
     * @param bankCard 原银行卡号
     * @return 脱敏后的银行卡号
     */
    public String maskBankCard(String bankCard) {
        if (!StringUtils.hasText(bankCard) || bankCard.length() < 8) {
            return bankCard;
        }

        return bankCard.substring(0, 4) + "****" + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 脱敏API密钥
     * 只显示前8位和后4位，中间用****替换
     *
     * @param apiKey 原API密钥
     * @return 脱敏后的API密钥
     */
    public String maskApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey) || apiKey.length() < 12) {
            return apiKey;
        }

        return apiKey.substring(0, 8) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * 脱敏用户名
     * 如果长度大于2，保留首尾字符，中间用*替换
     *
     * @param username 原用户名
     * @return 脱敏后的用户名
     */
    public String maskUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return username;
        }

        if (username.length() <= 2) {
            return username;
        }

        if (username.length() == 3) {
            return username.charAt(0) + "*" + username.charAt(2);
        }

        return username.charAt(0) + "**" + username.charAt(username.length() - 1);
    }

    /**
     * 脱敏IP地址
     * 保留前两段，后两段用*替换
     *
     * @param ip 原IP地址
     * @return 脱敏后的IP地址
     */
    public String maskIpAddress(String ip) {
        if (!StringUtils.hasText(ip) || !ip.contains(".")) {
            return ip;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return ip;
        }

        return parts[0] + "." + parts[1] + ".*.*";
    }

    /**
     * 脱敏文件路径
     * 只保留文件名，路径用...替换
     *
     * @param filePath 原文件路径
     * @return 脱敏后的文件路径
     */
    public String maskFilePath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return filePath;
        }

        int lastSlashIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        if (lastSlashIndex > 0) {
            return ".../" + filePath.substring(lastSlashIndex + 1);
        }

        return filePath;
    }

    /**
     * 通用脱敏方法
     * 保留前后指定长度的字符，中间用*替换
     *
     * @param text 原文本
     * @param prefixLength 前缀保留长度
     * @param suffixLength 后缀保留长度
     * @return 脱敏后的文本
     */
    public String maskGeneric(String text, int prefixLength, int suffixLength) {
        if (!StringUtils.hasText(text) || text.length() <= prefixLength + suffixLength) {
            return text;
        }

        String prefix = text.substring(0, prefixLength);
        String suffix = text.substring(text.length() - suffixLength);
        int maskLength = text.length() - prefixLength - suffixLength;
        String mask = "*".repeat(Math.min(maskLength, 8)); // 最多显示8个星号

        return prefix + mask + suffix;
    }
}