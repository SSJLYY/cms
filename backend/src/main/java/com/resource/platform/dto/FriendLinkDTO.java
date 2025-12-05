package com.resource.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 友情链接DTO
 */
@Data
@Schema(description = "友情链接DTO")
public class FriendLinkDTO {
    
    @Schema(description = "友情链接ID（更新时必填）")
    private Long id;
    
    @NotBlank(message = "网站名称不能为空")
    @Schema(description = "网站名称", required = true)
    private String name;
    
    @NotBlank(message = "网站URL不能为空")
    @Schema(description = "网站URL", required = true)
    private String url;
    
    @Schema(description = "网站Logo URL")
    private String logo;
    
    @Schema(description = "网站描述")
    private String description;
    
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-禁用，1-启用", required = true)
    private Integer status;
    
    @Schema(description = "排序序号")
    private Integer sortOrder;
}
