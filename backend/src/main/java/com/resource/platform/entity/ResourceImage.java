package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resource_image")
public class ResourceImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long resourceId;
    
    private Long imageId;
    
    private Integer isCover;
    
    private Integer sortOrder;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
