package com.resource.platform.module.resource.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源视图对象
 *
 * <p>对外暴露的资源信息，不包含内部字段（如 deleted、auditorId 等）。
 * 关联数据（下载链接、图片）通过独立字段携带。
 */
@Data
public class ResourceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 资源ID */
    private Long id;

    /** 资源标题 */
    private String title;

    /** 资源描述 */
    private String description;

    /** 封面图片ID */
    private Long coverImageId;

    /** 封面图片URL */
    private String coverImageUrl;

    /** 所属分类ID */
    private Long categoryId;

    /** 所属分类名称 */
    private String categoryName;

    /** 资源状态：0-已下架，1-已发布 */
    private Integer status;

    /** 下载次数 */
    private Integer downloadCount;

    /** 浏览次数 */
    private Integer viewCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 下载链接列表 */
    private List<DownloadLinkVO> downloadLinks;

    /** 资源关联的图片列表 */
    private List<ImageVO> images;
}
