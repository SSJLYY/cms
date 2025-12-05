package com.resource.platform.service;

import com.resource.platform.common.PageResult;
import com.resource.platform.entity.Revenue;
import com.resource.platform.vo.RevenueOverviewVO;
import com.resource.platform.vo.RevenueTypeVO;

import java.util.List;

public interface RevenueService {
    
    /**
     * 获取收益概览
     */
    RevenueOverviewVO getOverview(String period);
    
    /**
     * 按类型获取收益统计
     */
    List<RevenueTypeVO> getRevenueByType(String period);
    
    /**
     * 获取收益明细列表
     */
    PageResult<Revenue> getRevenueList(Integer pageNum, Integer pageSize, String period, String revenueType, String status);
    
    /**
     * 删除收益记录
     */
    boolean deleteRevenue(Long id);
    
    /**
     * 批量删除收益记录
     */
    boolean batchDeleteRevenue(List<Long> ids);
}
