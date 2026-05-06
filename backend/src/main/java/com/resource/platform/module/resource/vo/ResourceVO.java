package com.resource.platform.module.resource.vo;

import com.resource.platform.module.image.vo.ImageVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResourceVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private List<ImageVO> images;
}
