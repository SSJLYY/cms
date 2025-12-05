package com.resource.platform.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 访问日志服务接口
 */
public interface AccessLogService {
    
    /**
     * 记录访问日志
     */
    void recordVisit(Long resourceId, HttpServletRequest request);
    
    /**
     * 记录下载日志
     */
    void recordDownload(Long resourceId, HttpServletRequest request);
    
    /**
     * 记录搜索日志
     */
    void recordSearch(String keyword, HttpServletRequest request);
}
