package com.resource.platform.vo;

import lombok.Data;

/**
 * 日志统计VO
 */
@Data
public class LogStatisticsVO {
    
    /**
     * 总日志数
     */
    private Long totalLogs;
    
    /**
     * 今日日志数
     */
    private Long todayLogs;
    
    /**
     * 成功日志数
     */
    private Long successLogs;
    
    /**
     * 失败日志数
     */
    private Long errorLogs;
    
    /**
     * 平均响应时间（毫秒）
     */
    private Long avgDuration;
}
