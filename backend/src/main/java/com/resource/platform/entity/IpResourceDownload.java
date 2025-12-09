package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IP资源下载详细记录实体类
 */
@Data
@TableName("ip_resource_download")
public class IpResourceDownload {
    
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
     * 资源ID
     */
    private Long resourceId;
    
    /**
     * 下载日期
     */
    private LocalDate downloadDate;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
