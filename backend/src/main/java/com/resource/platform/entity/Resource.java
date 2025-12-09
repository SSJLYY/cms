package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resource")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String description;
    
    private Long coverImageId;
    
    private String tags;
    
    private Long categoryId;
    
    private Integer status;
    
    private Integer downloadCount;
    
    private Integer viewCount;
    
    private Integer sortOrder;
    
    private Integer isPinned;
    
    private String auditStatus;
    
    private LocalDateTime auditTime;
    
    private Long auditorId;
    
    private Long crawlerTaskId;
    
    private String sourceUrl;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
