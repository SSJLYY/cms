package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("image_size")
public class ImageSize {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long imageId;
    
    private String sizeType;
    
    private Integer width;
    
    private Integer height;
    
    private String filePath;
    
    private String fileUrl;
    
    private Long fileSize;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
