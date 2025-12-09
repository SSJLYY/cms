package com.resource.platform.service.impl;

import com.resource.platform.service.ConfigService;
import com.resource.platform.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Autowired
    private ConfigService configService;
    
    @Value("${spring.mail.username:}")
    private String defaultFrom;
    
    @Value("${spring.mail.enabled:true}")
    private boolean emailEnabled;
    
    /**
     * 从数据库获取邮件配置并创建 JavaMailSender
     */
    private JavaMailSender getConfiguredMailSender() {
        try {
            // 从数据库获取邮件配置
            List<String> mailConfigKeys = Arrays.asList(
                "email.smtp.host",
                "email.smtp.port",
                "email.username",
                "email.password",
                "email.from",
                "email.ssl.enable"
            );
            
            Map<String, String> mailConfigs = configService.getConfigsByKeys(mailConfigKeys);
            
            // 如果数据库中有配置，使用数据库配置
            if (mailConfigs.containsKey("email.smtp.host") && 
                !mailConfigs.get("email.smtp.host").isEmpty()) {
                
                JavaMailSenderImpl sender = new JavaMailSenderImpl();
                sender.setHost(mailConfigs.get("email.smtp.host"));
                sender.setPort(Integer.parseInt(mailConfigs.getOrDefault("email.smtp.port", "465")));
                sender.setUsername(mailConfigs.get("email.username"));
                sender.setPassword(mailConfigs.get("email.password"));
                
                Properties props = sender.getJavaMailProperties();
                props.put("mail.transport.protocol", "smtp");
                props.put("mail.smtp.auth", "true");
                
                boolean useSsl = "true".equalsIgnoreCase(mailConfigs.getOrDefault("email.ssl.enable", "true"));
                if (useSsl) {
                    props.put("mail.smtp.ssl.enable", "true");
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.socketFactory.port", mailConfigs.getOrDefault("email.smtp.port", "465"));
                    props.put("mail.smtp.starttls.enable", "false");
                    props.put("mail.smtp.starttls.required", "false");
                } else {
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.starttls.required", "true");
                }
                
                log.info("使用数据库邮件配置: host={}, port={}, username={}", 
                    sender.getHost(), sender.getPort(), sender.getUsername());
                return sender;
            }
        } catch (Exception e) {
            log.warn("获取数据库邮件配置失败，使用默认配置: {}", e.getMessage());
        }
        
        // 如果数据库没有配置或获取失败，使用默认的 mailSender
        return mailSender;
    }
    
    /**
     * 获取发件人地址
     */
    private String getFromAddress() {
        try {
            Map<String, String> mailConfigs = configService.getConfigsByKeys(Arrays.asList("email.from", "email.username"));
            String from = mailConfigs.get("email.from");
            if (from != null && !from.isEmpty()) {
                return from;
            }
            String username = mailConfigs.get("email.username");
            if (username != null && !username.isEmpty()) {
                return username;
            }
        } catch (Exception e) {
            log.warn("获取数据库发件人配置失败: {}", e.getMessage());
        }
        return defaultFrom;
    }
    
    @Override
    public void sendSimpleEmail(String to, String subject, String content) {
        log.info("准备发送简单邮件: to={}, subject={}", to, subject);
        
        if (!emailEnabled) {
            log.warn("邮件服务未启用，跳过发送邮件到: {}", to);
            throw new RuntimeException("邮件服务未启用");
        }
        
        try {
            JavaMailSender sender = getConfiguredMailSender();
            if (sender == null) {
                throw new RuntimeException("邮件发送器未配置");
            }
            
            String fromAddress = getFromAddress();
            log.info("使用发件人地址: {}", fromAddress);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            sender.send(message);
            log.info("邮件发送成功: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("邮件发送失败: to={}, subject={}", to, subject, e);
            throw new RuntimeException("邮件发送失败，请检查邮件配置", e);
        }
    }
    
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        log.info("准备发送HTML邮件: to={}, subject={}", to, subject);
        
        if (!emailEnabled) {
            log.warn("邮件服务未启用，跳过发送邮件到: {}", to);
            throw new RuntimeException("邮件服务未启用");
        }
        
        try {
            JavaMailSender sender = getConfiguredMailSender();
            if (sender == null) {
                throw new RuntimeException("邮件发送器未配置");
            }
            
            String fromAddress = getFromAddress();
            log.info("使用发件人地址: {}", fromAddress);
            
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            sender.send(message);
            log.info("HTML邮件发送成功: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("HTML邮件发送失败: to={}, subject={}", to, subject, e);
            throw new RuntimeException("邮件发送失败，请检查邮件配置", e);
        }
    }
}
