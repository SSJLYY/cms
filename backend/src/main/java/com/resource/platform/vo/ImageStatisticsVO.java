package com.resource.platform.vo;

import lombok.Data;

/**
 * 图片统计VO
 */
@Data
public class ImageStatisticsVO {
    
    /**
     * 总图片数
     */
    private Long totalImages;
    
    /**
     * 已使用图片数
     */
    private Long usedImages;
    
    /**
     * 未使用图片数
     */
    private Long unusedImages;
    
    /**
     * 总存储大小（字节）
     */
    private Long totalSize;
    
    /**
     * 今日上传数
     */
    private Long todayUploads;
}
