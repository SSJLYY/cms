package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("crawler_task")
public class CrawlerTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String targetUrl;
    
    private Integer status;
    
    private Integer crawlInterval;
    
    private Integer maxDepth;
    
    private String categoryMapping;
    
    private Integer intelligentMode;
    
    private String customRules;
    
    private Integer totalCrawled;
    
    private Integer totalSuccess;
    
    private Integer totalFailed;
    
    private LocalDateTime lastExecuteTime;
    
    private LocalDateTime nextExecuteTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
