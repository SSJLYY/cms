package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("seo_submission")
public class SEOSubmission {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String engine;
    
    private String url;
    
    private String status;
    
    private String responseMessage;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submitTime;
}
