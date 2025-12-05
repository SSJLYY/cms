package com.resource.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 友情链接查询DTO
 */
@Data
@Schema(description = "友情链接查询DTO")
public class FriendLinkQueryDTO {
    
    @Schema(description = "网站名称（模糊搜索）")
    private String name;
    
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
    
    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
