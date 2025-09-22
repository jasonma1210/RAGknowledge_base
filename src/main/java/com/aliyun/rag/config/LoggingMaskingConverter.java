package com.aliyun.rag.config;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志脱敏转换器
 * <p>
 * 自动检测日志消息中的敏感信息并进行脱敏处理
 * 用于保护生产环境日志中的敏感数据
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
public class LoggingMaskingConverter extends MessageConverter {

    /**
     * 敏感信息模式定义
     */
    private static final Pattern[] SENSITIVE_PATTERNS = {
        // 密码模式
        Pattern.compile("(password[\"']?\\s*[:=]\\s*[\"']?)([^\"'\\s,}]+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(pwd[\"']?\\s*[:=]\\s*[\"']?)([^\"'\\s,}]+)", Pattern.CASE_INSENSITIVE),
        
        // 邮箱模式
        Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"),
        
        // 手机号模式（中国大陆）
        Pattern.compile("(1[3-9]\\d)(\\d{4})(\\d{4})"),
        
        // 身份证号模式
        Pattern.compile("(\\d{6})(\\d{8})(\\d{4})"),
        
        // API密钥模式
        Pattern.compile("(api[_-]?key[\"']?\\s*[:=]\\s*[\"']?)([a-zA-Z0-9+/=]{16,})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(token[\"']?\\s*[:=]\\s*[\"']?)([a-zA-Z0-9._-]{20,})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(secret[\"']?\\s*[:=]\\s*[\"']?)([a-zA-Z0-9+/=]{16,})", Pattern.CASE_INSENSITIVE),
        
        // JWT Token模式
        Pattern.compile("(Bearer\\s+)([a-zA-Z0-9._-]+)", Pattern.CASE_INSENSITIVE),
        
        // 银行卡号模式
        Pattern.compile("(\\d{4})(\\d{8,12})(\\d{4})"),
        
        // IP地址模式（保留前两段）
        Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.)\\d{1,3}\\.\\d{1,3}")
    };

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        return maskSensitiveData(message);
    }

    /**
     * 脱敏敏感数据
     */
    private String maskSensitiveData(String message) {
        String maskedMessage = message;
        
        // 密码脱敏
        maskedMessage = maskPasswords(maskedMessage);
        
        // 邮箱脱敏
        maskedMessage = maskEmails(maskedMessage);
        
        // 手机号脱敏
        maskedMessage = maskPhones(maskedMessage);
        
        // 身份证脱敏
        maskedMessage = maskIdCards(maskedMessage);
        
        // API密钥和Token脱敏
        maskedMessage = maskApiKeys(maskedMessage);
        
        // 银行卡脱敏
        maskedMessage = maskBankCards(maskedMessage);
        
        // IP地址脱敏
        maskedMessage = maskIpAddresses(maskedMessage);
        
        return maskedMessage;
    }

    private String maskPasswords(String message) {
        Pattern pattern = Pattern.compile("(password[\"']?\\s*[:=]\\s*[\"']?)([^\"'\\s,}]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        return matcher.replaceAll("$1****");
    }

    private String maskEmails(String message) {
        Pattern pattern = Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
        Matcher matcher = pattern.matcher(message);
        return matcher.replaceAll(match -> {
            String localPart = match.group(1);
            String domain = match.group(2);
            
            if (localPart.length() <= 1) {
                return match.group(0);
            }
            
            String maskedLocal = localPart.charAt(0) + "****" + localPart.charAt(localPart.length() - 1);
            return maskedLocal + "@" + domain;
        });
    }

    private String maskPhones(String message) {
        Pattern pattern = Pattern.compile("(1[3-9]\\d)(\\d{4})(\\d{4})");
        Matcher matcher = pattern.matcher(message);
        return matcher.replaceAll("$1****$3");
    }

    private String maskIdCards(String message) {
        Pattern pattern = Pattern.compile("(\\d{6})(\\d{8})(\\d{4})");
        Matcher matcher = pattern.matcher(message);
        return matcher.replaceAll("$1********$3");
    }

    private String maskApiKeys(String message) {
        // API Key
        Pattern apiKeyPattern = Pattern.compile("(api[_-]?key[\"']?\\s*[:=]\\s*[\"']?)([a-zA-Z0-9+/=]{16,})", Pattern.CASE_INSENSITIVE);
        message = apiKeyPattern.matcher(message).replaceAll("$1****");
        
        // Token
        Pattern tokenPattern = Pattern.compile("(token[\"']?\\s*[:=]\\s*[\"']?)([a-zA-Z0-9._-]{20,})", Pattern.CASE_INSENSITIVE);
        message = tokenPattern.matcher(message).replaceAll("$1****");
        
        // Secret
        Pattern secretPattern = Pattern.compile("(secret[\"']?\\s*[:=]\\s*[\"']?)([a-zA-Z0-9+/=]{16,})", Pattern.CASE_INSENSITIVE);
        message = secretPattern.matcher(message).replaceAll("$1****");
        
        // Bearer Token
        Pattern bearerPattern = Pattern.compile("(Bearer\\s+)([a-zA-Z0-9._-]+)", Pattern.CASE_INSENSITIVE);
        message = bearerPattern.matcher(message).replaceAll("$1****");
        
        return message;
    }

    private String maskBankCards(String message) {
        Pattern pattern = Pattern.compile("(\\d{4})(\\d{8,12})(\\d{4})");
        Matcher matcher = pattern.matcher(message);
        return matcher.replaceAll("$1****$3");
    }

    private String maskIpAddresses(String message) {
        Pattern pattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.)\\d{1,3}\\.\\d{1,3}");
        Matcher matcher = pattern.matcher(message);
        return matcher.replaceAll("$1*.*");
    }
}