package com.resource.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevenueOverviewVO {
    
    /**
     * 总收益
     */
    private BigDecimal totalRevenue;
    
    /**
     * 总下载次数
     */
    private Integer totalDownloads;
    
    /**
     * 收益项数量
     */
    private Integer revenueItemCount;
    
    /**
     * 统计周期
     */
    private String period;
}
