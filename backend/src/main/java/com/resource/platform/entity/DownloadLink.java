package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("download_link")
public class DownloadLink {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long resourceId;
    
    private String linkName;
    
    private String linkType;
    
    private String linkUrl;
    
    private String password;
    
    private Integer isValid;
    
    private LocalDateTime checkTime;
    
    private Integer sortOrder;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
