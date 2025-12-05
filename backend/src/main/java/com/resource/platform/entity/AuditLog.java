package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long operatorId;
    
    private String operatorName;
    
    private String operationType;
    
    private String operationModule;
    
    private String operationObject;
    
    private String operationDetail;
    
    private String ipAddress;  // 对应数据库的ip字段
    
    private String userAgent;
    
    // 新增字段
    private Long userId;
    
    private String module;
    
    private String action;
    
    private String description;
    
    private String status;
    
    private String errorMessage;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
