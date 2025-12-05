package com.resource.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片查询DTO
 */
@Data
public class ImageQueryDTO {
    
    /**
     * 文件名关键词
     */
    private String keyword;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 存储类型
     */
    private String storageType;
    
    /**
     * 是否已使用
     */
    private Integer isUsed;
    
    /**
     * 上传者ID
     */
    private Long uploaderId;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
