package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.dto.CategoryQueryDTO;
import com.resource.platform.entity.Category;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.exception.ValidationException;
import com.resource.platform.mapper.CategoryMapper;
import com.resource.platform.service.CategoryService;
import com.resource.platform.vo.CategoryStatisticsVO;
import com.resource.platform.vo.CategoryTreeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private com.resource.platform.mapper.ResourceMapper resourceMapper;

    @Override
    public CategoryStatisticsVO getStatistics() {
        CategoryStatisticsVO statistics = new CategoryStatisticsVO();
        
        // 总分类数
        Long totalCategories = categoryMapper.selectCount(null);
        statistics.setTotalCategories(totalCategories);
        
        // 一级分类数
        Long level1Categories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getLevel, 1)
        );
        statistics.setLevel1Categories(level1Categories);
        
        // 二级分类数
        Long level2Categories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getLevel, 2)
        );
        statistics.setLevel2Categories(level2Categories);
        
        // 启用分类数
        Long activeCategories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1)
        );
        statistics.setActiveCategories(activeCategories);
        
        // 禁用分类数
        statistics.setInactiveCategories(totalCategories - activeCategories);
        
        return statistics;
    }

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        // 查询所有分类
        List<Category> allCategories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId)
        );
        
        // 构建树形结构
        return buildTree(allCategories, 0L);
    }

    private List<CategoryTreeVO> buildTree(List<Category> categories, Long parentId) {
        List<CategoryTreeVO> tree = new ArrayList<>();
        
        for (Category category : categories) {
            if (category.getParentId().equals(parentId)) {
                CategoryTreeVO node = new CategoryTreeVO();
                BeanUtils.copyProperties(category, node);
                
                // 递归查找子分类
                List<CategoryTreeVO> children = buildTree(categories, category.getId());
                node.setChildren(children);
                
                // 查询该分类下的资源数量
                Long resourceCount = resourceMapper.selectCount(
                    new LambdaQueryWrapper<com.resource.platform.entity.Resource>()
                        .eq(com.resource.platform.entity.Resource::getCategoryId, category.getId())
                );
                node.setResourceCount(resourceCount);
                
                tree.add(node);
            }
        }
        
        return tree;
    }

    @Override
    public Page<Category> queryCategories(CategoryQueryDTO queryDTO) {
        Page<Category> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.like(Category::getName, queryDTO.getKeyword());
        }
        
        // 父分类筛选
        if (queryDTO.getParentId() != null) {
            wrapper.eq(Category::getParentId, queryDTO.getParentId());
        }
        
        // 层级筛选
        if (queryDTO.getLevel() != null) {
            wrapper.eq(Category::getLevel, queryDTO.getLevel());
        }
        
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Category::getStatus, queryDTO.getStatus());
        }
        
        // 按排序字段和ID排序
        wrapper.orderByAsc(Category::getSortOrder)
               .orderByAsc(Category::getId);
        
        return categoryMapper.selectPage(page, wrapper);
    }

    @Override
    public Category getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ResourceNotFoundException("分类", id);
        }
        return category;
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        // 验证分类名称唯一性
        Long count = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getName, category.getName())
                .eq(Category::getParentId, category.getParentId())
        );
        if (count > 0) {
            throw new ValidationException("同级分类名称已存在");
        }
        
        // 设置层级
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
        } else {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new ValidationException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        }
        
        // 设置默认排序
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        
        // 设置默认状态
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        categoryMapper.insert(category);
        return category;
    }

    @Override
    @Transactional
    public Category updateCategory(Category category) {
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new ResourceNotFoundException("分类", category.getId());
        }
        
        // 设置层级
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
        } else {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new ValidationException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        }
        
        // 验证分类名称唯一性
        Long count = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getName, category.getName())
                .eq(Category::getParentId, category.getParentId())
                .ne(Category::getId, category.getId())
        );
        if (count > 0) {
            throw new ValidationException("同级分类名称已存在");
        }
        
        categoryMapper.updateById(category);
        return category;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ResourceNotFoundException("分类", id);
        }
        
        // 检查是否有子分类
        Long childCount = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getParentId, id)
        );
        if (childCount > 0) {
            throw new ValidationException("该分类下有子分类，无法删除");
        }
        
        // TODO: 检查是否有资源使用该分类
        
        categoryMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteCategories(List<Long> ids) {
        for (Long id : ids) {
            try {
                deleteCategory(id);
            } catch (Exception e) {
                log.error("批量删除分类失败: {}", id, e);
            }
        }
    }

    @Override
    @Transactional
    public void updateSortOrder(Long id, Integer sortOrder) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ResourceNotFoundException("分类", id);
        }
        
        category.setSortOrder(sortOrder);
        categoryMapper.updateById(category);
    }

    @Override
    public String exportCategories() {
        List<Category> categories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId)
        );
        
        // 生成CSV内容
        StringBuilder csv = new StringBuilder();
        csv.append("ID,名称,父分类ID,层级,图标,描述,排序,状态,创建时间\n");
        
        for (Category category : categories) {
            csv.append(category.getId()).append(",")
               .append(category.getName()).append(",")
               .append(category.getParentId()).append(",")
               .append(category.getLevel()).append(",")
               .append(category.getIcon() != null ? category.getIcon() : "").append(",")
               .append(category.getDescription() != null ? category.getDescription() : "").append(",")
               .append(category.getSortOrder()).append(",")
               .append(category.getStatus()).append(",")
               .append(category.getCreateTime()).append("\n");
        }
        
        // 保存到临时文件
        try {
            String fileName = "categories_export_" + System.currentTimeMillis() + ".csv";
            String filePath = "/tmp/" + fileName;
            File file = new File(filePath);
            FileUtils.writeStringToFile(file, csv.toString(), StandardCharsets.UTF_8);
            
            log.info("导出分类成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            log.error("导出分类失败", e);
            throw new RuntimeException("导出分类失败: " + e.getMessage());
        }
    }
}
