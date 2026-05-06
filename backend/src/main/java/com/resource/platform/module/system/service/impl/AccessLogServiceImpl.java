package com.resource.platform.module.system.service.impl;

import com.resource.platform.module.system.entity.AccessLog;
import com.resource.platform.module.system.mapper.AccessLogMapper;
import com.resource.platform.module.system.service.AccessLogService;
import com.resource.platform.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AccessLogServiceImpl implements AccessLogService {
    
    @Autowired
    private AccessLogMapper accessLogMapper;
    
    @Override
    public void recordVisit(Long resourceId, HttpServletRequest request) {
        try {
            AccessLog accessLog = buildAccessLog(resourceId, "visit", request);
            accessLogMapper.insert(accessLog);
        } catch (Exception e) {
            log.error("记录访问日志失败", e);
        }
    }
    
    @Override
    public void recordDownload(Long resourceId, HttpServletRequest request) {
        try {
            AccessLog accessLog = buildAccessLog(resourceId, "download", request);
            accessLogMapper.insert(accessLog);
        } catch (Exception e) {
            log.error("记录下载日志失败", e);
        }
    }
    
    @Override
    public void recordSearch(String keyword, HttpServletRequest request) {
        try {
            AccessLog accessLog = buildAccessLog(null, "search", request);
            accessLogMapper.insert(accessLog);
        } catch (Exception e) {
            log.error("记录搜索日志失败", e);
        }
    }
    
    private AccessLog buildAccessLog(Long resourceId, String actionType, HttpServletRequest request) {
        AccessLog accessLog = new AccessLog();
        accessLog.setResourceId(resourceId);
        accessLog.setActionType(actionType);
        accessLog.setReferer(request.getHeader("Referer"));
        accessLog.setUserAgent(request.getHeader("User-Agent"));
        accessLog.setBrowser(parseBrowser(request.getHeader("User-Agent")));
        accessLog.setIpAddress(IpUtil.getClientIp(request));
        accessLog.setCreateTime(LocalDateTime.now());
        return accessLog;
    }

    private String parseBrowser(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("Edg/") || userAgent.contains("Edge")) {
            return "Edge";
        } else if (userAgent.contains("Chrome")) {
            return "Chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Safari")) {
            return "Safari";
        } else if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
            return "Opera";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "IE";
        } else {
            return "Other";
        }
    }
}
