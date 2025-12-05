package com.resource.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevenueTypeVO {
    
    /**
     * 收益类型
     */
    private String revenueType;
    
    /**
     * 类型名称
     */
    private String typeName;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 累计收益
     */
    private BigDecimal accumulatedRevenue;
}
