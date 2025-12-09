package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IP下载记录实体类
 */
@Data
@TableName("ip_download_record")
public class IpDownloadRecord {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 下载日期
     */
    private LocalDate downloadDate;
    
    /**
     * 当日下载次数
     */
    private Integer downloadCount;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
