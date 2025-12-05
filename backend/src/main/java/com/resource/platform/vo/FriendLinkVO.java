package com.resource.platform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友情链接VO
 */
@Data
@Schema(description = "友情链接VO")
public class FriendLinkVO {
    
    @Schema(description = "友情链接ID")
    private Long id;
    
    @Schema(description = "网站名称")
    private String name;
    
    @Schema(description = "网站URL")
    private String url;
    
    @Schema(description = "网站Logo URL")
    private String logo;
    
    @Schema(description = "网站描述")
    private String description;
    
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
    
    @Schema(description = "排序序号")
    private Integer sortOrder;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
