package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("advertisement")
public class Advertisement {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String position;
    
    private String type;
    
    private String imageUrl;
    
    private String linkUrl;
    
    private String content;
    
    private Integer status;
    
    private Integer sortOrder;
    
    private Integer clickCount;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
