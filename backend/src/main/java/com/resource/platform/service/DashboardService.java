package com.resource.platform.service;

import com.resource.platform.vo.DashboardMetricsVO;
import com.resource.platform.vo.TrendDataVO;

import java.util.Map;

/**
 * 控制面板服务接口
 */
public interface DashboardService {
    
    /**
     * 获取核心指标
     */
    DashboardMetricsVO getMetrics();
    
    /**
     * 获取趋势数据
     * 
     * @param days 天数
     */
    TrendDataVO getTrendData(Integer days);
    
    /**
     * 获取热门资源
     * 
     * @param limit 数量限制
     */
    Object getHotResources(Integer limit);
    
    /**
     * 获取最新资源
     * 
     * @param limit 数量限制
     */
    Object getLatestResources(Integer limit);
    
    /**
     * 获取待处理事项
     */
    Map<String, Long> getPendingTasks();
    
    /**
     * 获取系统状态
     */
    Map<String, Object> getSystemStatus();
}
