package com.resource.platform.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resource.platform.annotation.OperationLog;
import com.resource.platform.entity.AuditLog;
import com.resource.platform.entity.SystemLog;
import com.resource.platform.mapper.AuditLogMapper;
import com.resource.platform.mapper.SystemLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 自动记录带有@OperationLog注解的方法的操作日志
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private SystemLogMapper systemLogMapper;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("@annotation(com.resource.platform.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 执行方法
        Object result = null;
        Exception exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 记录日志
            try {
                recordLog(joinPoint, operationLog, request, result, exception, duration);
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        }
    }

    private void recordLog(ProceedingJoinPoint joinPoint, OperationLog operationLog,
                          HttpServletRequest request, Object result, Exception exception, long duration) {
        try {
            // 构建系统日志
            SystemLog systemLog = new SystemLog();
            systemLog.setModule(operationLog.module());
            systemLog.setType(operationLog.type());
            systemLog.setDescription(operationLog.description());
            systemLog.setMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
            
            if (request != null) {
                systemLog.setRequestUrl(request.getRequestURI());
                systemLog.setRequestMethod(request.getMethod());
                systemLog.setIpAddress(getIpAddress(request));
                systemLog.setUserAgent(request.getHeader("User-Agent"));
            }
            
            // 记录请求参数
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    systemLog.setRequestParams(objectMapper.writeValueAsString(args));
                } catch (Exception e) {
                    systemLog.setRequestParams("参数序列化失败");
                }
            }
            
            // 记录响应结果
            if (exception != null) {
                systemLog.setStatus("ERROR");
                systemLog.setErrorMessage(exception.getMessage());
            } else {
                systemLog.setStatus("SUCCESS");
                if (result != null) {
                    try {
                        String resultStr = objectMapper.writeValueAsString(result);
                        // 限制响应结果长度
                        if (resultStr.length() > 1000) {
                            resultStr = resultStr.substring(0, 1000) + "...";
                        }
                        systemLog.setResponseData(resultStr);
                    } catch (Exception e) {
                        systemLog.setResponseData("响应序列化失败");
                    }
                }
            }
            
            systemLog.setDuration(duration);
            systemLog.setCreateTime(LocalDateTime.now());
            
            // 保存系统日志
            systemLogMapper.insert(systemLog);
            
            // 如果需要审计日志
            if (operationLog.audit()) {
                recordAuditLog(operationLog, request, exception);
            }
            
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private void recordAuditLog(OperationLog operationLog, HttpServletRequest request, Exception exception) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setModule(operationLog.module());
            auditLog.setAction(operationLog.type());
            auditLog.setDescription(operationLog.description());
            
            if (request != null) {
                auditLog.setIpAddress(getIpAddress(request));
                // 从请求中获取用户ID（需要根据实际认证方式调整）
                Object userId = request.getAttribute("userId");
                if (userId != null) {
                    auditLog.setUserId(Long.parseLong(userId.toString()));
                }
            }
            
            auditLog.setStatus(exception == null ? "SUCCESS" : "FAILED");
            if (exception != null) {
                auditLog.setErrorMessage(exception.getMessage());
            }
            
            auditLog.setCreateTime(LocalDateTime.now());
            
            // 保存审计日志
            auditLogMapper.insert(auditLog);
            
        } catch (Exception e) {
            log.error("保存审计日志失败", e);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
