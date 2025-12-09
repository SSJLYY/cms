package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("crawler_log")
public class CrawlerLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long taskId;
    
    private String taskName;
    
    private Integer executeType;
    
    private Integer status;
    
    private Integer crawledCount;
    
    private Integer successCount;
    
    private Integer failedCount;
    
    private Integer duration;
    
    private String errorMessage;
    
    private String errorType;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
