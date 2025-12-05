package com.resource.platform.dto;

import lombok.Data;

/**
 * 分类查询DTO
 */
@Data
public class CategoryQueryDTO {
    
    /**
     * 分类名称关键词
     */
    private String keyword;
    
    /**
     * 父分类ID
     */
    private Long parentId;
    
    /**
     * 层级
     */
    private Integer level;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
