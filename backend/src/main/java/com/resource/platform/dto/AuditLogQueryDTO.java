package com.resource.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志查询DTO
 */
@Data
public class AuditLogQueryDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 模块
     */
    private String module;
    
    /**
     * 操作
     */
    private String action;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
