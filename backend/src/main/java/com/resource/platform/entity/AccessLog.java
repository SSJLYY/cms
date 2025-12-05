package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("access_log")
public class AccessLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long resourceId;
    
    private String actionType;
    
    private String referer;
    
    private String userAgent;
    
    private String browser;
    
    private String ipAddress;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
