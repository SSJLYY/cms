package com.resource.platform.service;

/**
 * IP下载限制服务接口
 */
public interface IpDownloadService {
    
    /**
     * 检查IP今日是否还能下载
     * @param ipAddress IP地址
     * @return true-可以下载，false-已达上限
     */
    boolean canDownload(String ipAddress);
    
    /**
     * 记录IP下载
     * @param ipAddress IP地址
     * @param resourceId 资源ID
     */
    void recordDownload(String ipAddress, Long resourceId);
    
    /**
     * 获取IP今日剩余下载次数
     * @param ipAddress IP地址
     * @return 剩余次数
     */
    int getRemainingDownloads(String ipAddress);
    
    /**
     * 获取IP今日已下载次数
     * @param ipAddress IP地址
     * @return 已下载次数
     */
    int getTodayDownloadCount(String ipAddress);
    
    /**
     * 检查IP今日是否已下载过此资源
     * @param ipAddress IP地址
     * @param resourceId 资源ID
     * @return true-已下载过，false-未下载过
     */
    boolean hasDownloadedToday(String ipAddress, Long resourceId);
}
