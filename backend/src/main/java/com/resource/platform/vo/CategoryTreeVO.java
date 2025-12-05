package com.resource.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类树VO
 */
@Data
public class CategoryTreeVO {
    
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private String icon;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 子分类列表
     */
    private List<CategoryTreeVO> children;
    
    /**
     * 资源数量
     */
    private Long resourceCount;
}
