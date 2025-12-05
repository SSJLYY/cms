package com.resource.platform.vo;

import lombok.Data;

/**
 * 控制面板核心指标VO
 */
@Data
public class DashboardMetricsVO {
    
    /**
     * 总资源数
     */
    private Long totalResources;
    
    /**
     * 今日新增资源
     */
    private Long todayResources;
    
    /**
     * 总下载量
     */
    private Long totalDownloads;
    
    /**
     * 今日下载量
     */
    private Long todayDownloads;
    
    /**
     * 总用户数
     */
    private Long totalUsers;
    
    /**
     * 今日新增用户
     */
    private Long todayUsers;
    
    /**
     * 总分类数
     */
    private Long totalCategories;
    
    /**
     * 待审核资源数
     */
    private Long pendingResources;
}
