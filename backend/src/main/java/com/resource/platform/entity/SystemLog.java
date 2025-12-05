package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("system_log")
public class SystemLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String module;
    
    private String type;
    
    private String description;
    
    private String method;
    
    private String requestUrl;
    
    private String requestMethod;
    
    private String requestParams;
    
    private String responseData;
    
    @TableField("ip")
    private String ipAddress;  // 对应数据库的ip字段
    
    private String userAgent;
    
    private String status;
    
    private String errorMessage;
    
    private Long duration;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
