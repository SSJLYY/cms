package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resource_tag")
public class ResourceTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String tagName;
    
    private Integer useCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
