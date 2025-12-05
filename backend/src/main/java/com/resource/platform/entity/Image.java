package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("image")
public class Image {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String fileName;
    
    private String originalName;
    
    private String filePath;
    
    private String fileUrl;
    
    private Long fileSize;
    
    private String fileType;
    
    private Integer width;
    
    private Integer height;
    
    private String thumbnailUrl;
    
    private String storageType;
    
    private Integer isUsed;
    
    private Long uploaderId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
