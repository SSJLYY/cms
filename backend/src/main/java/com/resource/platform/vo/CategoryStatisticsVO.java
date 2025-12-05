package com.resource.platform.vo;

import lombok.Data;

/**
 * 分类统计VO
 */
@Data
public class CategoryStatisticsVO {
    
    /**
     * 总分类数
     */
    private Long totalCategories;
    
    /**
     * 一级分类数
     */
    private Long level1Categories;
    
    /**
     * 二级分类数
     */
    private Long level2Categories;
    
    /**
     * 启用分类数
     */
    private Long activeCategories;
    
    /**
     * 禁用分类数
     */
    private Long inactiveCategories;
}
