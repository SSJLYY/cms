package com.resource.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志查询DTO
 */
@Data
public class LogQueryDTO {
    
    /**
     * 模块
     */
    private String module;
    
    /**
     * 类型
     */
    private String type;
    
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
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
