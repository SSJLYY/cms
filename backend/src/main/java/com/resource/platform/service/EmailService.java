package com.resource.platform.service;

/**
 * 邮件服务接口
 */
public interface EmailService {
    
    /**
     * 发送简单文本邮件
     */
    void sendSimpleEmail(String to, String subject, String content);
    
    /**
     * 发送HTML邮件
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);
}
