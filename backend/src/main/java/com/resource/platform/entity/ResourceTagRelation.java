package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resource_tag_relation")
public class ResourceTagRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long resourceId;
    
    private Long tagId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
