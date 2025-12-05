package com.resource.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.dto.CategoryQueryDTO;
import com.resource.platform.entity.Category;
import com.resource.platform.vo.CategoryStatisticsVO;
import com.resource.platform.vo.CategoryTreeVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {
    
    /**
     * 获取分类统计信息
     */
    CategoryStatisticsVO getStatistics();
    
    /**
     * 获取分类树
     */
    List<CategoryTreeVO> getCategoryTree();
    
    /**
     * 查询分类列表
     */
    Page<Category> queryCategories(CategoryQueryDTO queryDTO);
    
    /**
     * 获取分类详情
     */
    Category getCategoryById(Long id);
    
    /**
     * 创建分类
     */
    Category createCategory(Category category);
    
    /**
     * 更新分类
     */
    Category updateCategory(Category category);
    
    /**
     * 删除分类
     */
    void deleteCategory(Long id);
    
    /**
     * 批量删除分类
     */
    void deleteCategories(List<Long> ids);
    
    /**
     * 调整分类排序
     */
    void updateSortOrder(Long id, Integer sortOrder);
    
    /**
     * 导出分类
     */
    String exportCategories();
}
