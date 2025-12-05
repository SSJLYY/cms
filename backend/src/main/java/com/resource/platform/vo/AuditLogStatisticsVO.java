package com.resource.platform.vo;

import lombok.Data;

/**
 * 审计日志统计VO
 */
@Data
public class AuditLogStatisticsVO {
    
    /**
     * 总审计日志数
     */
    private Long totalAuditLogs;
    
    /**
     * 今日审计日志数
     */
    private Long todayAuditLogs;
    
    /**
     * 成功操作数
     */
    private Long successOperations;
    
    /**
     * 失败操作数
     */
    private Long failedOperations;
}
