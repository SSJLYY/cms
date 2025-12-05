package com.resource.platform.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ResourceDTO {
    @NotBlank(message = "资源标题不能为空")
    private String title;
    
    private String description;
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    private Integer status;
    
    private List<DownloadLinkDTO> downloadLinks;
    
    // 图片相关字段
    private List<Long> imageIds;  // 资源关联的图片ID列表（最多5张）
    
    private Long coverImageId;  // 封面图片ID
}
