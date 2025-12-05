package com.resource.platform.service;

import com.resource.platform.common.PageResult;
import com.resource.platform.dto.VisitQueryDTO;
import com.resource.platform.vo.StatisticsOverviewVO;

import java.util.List;
import java.util.Map;

public interface StatisticsService {
    /**
     * 获取统计概览
     */
    StatisticsOverviewVO getOverview(String period);
    
    /**
     * 获取下载分布数据
     */
    List<Map<String, Object>> getDownloadDistribution(String period);
    
    /**
     * 获取访问统计详情
     */
    PageResult<Map<String, Object>> getVisitDetails(VisitQueryDTO query);
    
    /**
     * 获取实时活动
     */
    List<Map<String, Object>> getRealtimeActivities(Integer limit);
}
