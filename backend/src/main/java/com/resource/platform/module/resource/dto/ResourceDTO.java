package com.resource.platform.module.resource.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 资源数据传输对象
 *
 * <p>用于创建和更新资源时的请求参数校验。
 */
@Data
public class ResourceDTO {

    /** 资源标题 */
    @NotBlank(message = "资源标题不能为空")
    @Size(max = 200, message = "资源标题最长200个字符")
    private String title;

    /** 资源描述 */
    @Size(max = 2000, message = "资源描述最长2000个字符")
    private String description;

    /** 所属分类ID */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /** 资源状态 */
    private Integer status;

    /** 下载链接列表 */
    private List<DownloadLinkDTO> downloadLinks;

    /** 资源关联的图片ID列表（最多5张） */
    @Size(max = 5, message = "每个资源最多关联5张图片")
    private List<Long> imageIds;

    /** 封面图片ID */
    private Long coverImageId;
}
