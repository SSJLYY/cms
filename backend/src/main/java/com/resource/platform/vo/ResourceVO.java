package com.resource.platform.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResourceVO {
    private Long id;
    private String title;
    private String description;
    private Long coverImageId;
    private String coverImageUrl;
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private Integer downloadCount;
    private Integer viewCount;
    private LocalDateTime createTime;
    private List<DownloadLinkVO> downloadLinks;
    
    // 图片相关字段
    private List<ImageVO> images;  // 资源关联的所有图片
}
