package com.resource.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片VO
 */
@Data
public class ImageVO {
    
    private Long id;
    private String fileName;
    private String originalName;
    private String fileUrl;
    private String thumbnailUrl;
    private Long fileSize;
    private String fileType;
    private Integer width;
    private Integer height;
    private String storageType;
    private Integer isUsed;
    private Long uploaderId;
    private LocalDateTime createTime;
}
