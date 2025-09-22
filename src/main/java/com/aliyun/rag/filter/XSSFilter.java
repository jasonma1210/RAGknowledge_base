package com.aliyun.rag.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * XSS防护过滤器
 * <p>
 * 对HTTP请求参数进行XSS检测和过滤，防止恶意脚本注入
 * 支持检测和清理常见的XSS攻击向量
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-18
 */
public class XSSFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(XSSFilter.class);

    /**
     * XSS攻击模式检测正则表达式
     */
    private static final Pattern[] XSS_PATTERNS = {
        // Script标签
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        
        // JavaScript事件处理器
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        
        // Style表达式
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<style[^>]*>.*?</style>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        
        // 危险标签
        Pattern.compile("<(iframe|object|embed|applet|meta|link)[^>]*>", Pattern.CASE_INSENSITIVE),
        
        // 数据URI协议
        Pattern.compile("data:text/html", Pattern.CASE_INSENSITIVE),
        Pattern.compile("data:application/", Pattern.CASE_INSENSITIVE),
        
        // 其他潜在危险内容
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("livescript:", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("XSS防护过滤器初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 检查请求参数是否包含XSS攻击向量
        if (containsXSS(httpRequest)) {
            log.warn("检测到XSS攻击尝试，来源IP: {}, URI: {}", 
                getClientIP(httpRequest), httpRequest.getRequestURI());
            
            // 返回400错误
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"success\":false,\"message\":\"请求包含非法字符\"}");
            return;
        }
        
        // 继续处理请求
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.info("XSS防护过滤器销毁");
    }

    /**
     * 检查请求是否包含XSS攻击向量
     */
    private boolean containsXSS(HttpServletRequest request) {
        // 检查所有参数
        if (request.getParameterMap() != null) {
            for (String[] values : request.getParameterMap().values()) {
                for (String value : values) {
                    if (isXSSAttack(value)) {
                        return true;
                    }
                }
            }
        }
        
        // 检查请求头
        String userAgent = request.getHeader("User-Agent");
        if (StringUtils.hasText(userAgent) && isXSSAttack(userAgent)) {
            return true;
        }
        
        String referer = request.getHeader("Referer");
        if (StringUtils.hasText(referer) && isXSSAttack(referer)) {
            return true;
        }
        
        return false;
    }

    /**
     * 检查字符串是否包含XSS攻击模式
     */
    private boolean isXSSAttack(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        
        String decodedValue = decodeValue(value);
        
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(decodedValue).find()) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 解码字符串（处理URL编码和HTML编码）
     */
    private String decodeValue(String value) {
        if (value == null) {
            return null;
        }
        
        // URL解码
        try {
            value = java.net.URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            // 解码失败，使用原值
        }
        
        // HTML实体解码
        value = value.replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">")
                    .replaceAll("&quot;", "\"")
                    .replaceAll("&#x27;", "'")
                    .replaceAll("&#x2F;", "/")
                    .replaceAll("&amp;", "&");
        
        return value;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIP)) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}