package com.resource.platform.module.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.exception.ValidationException;
import com.resource.platform.module.category.dto.CategoryQueryDTO;
import com.resource.platform.module.category.entity.Category;
import com.resource.platform.module.category.mapper.CategoryMapper;
import com.resource.platform.module.category.service.CategoryService;
import com.resource.platform.module.category.vo.CategoryStatisticsVO;
import com.resource.platform.module.category.vo.CategoryTreeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类服务实现类
 *
 * 功能说明：
 * - 实现分类的核心业务逻辑
 * - 处理分类的CRUD操作
 * - 管理分类的树形结构
 * - 处理分类的层级关系
 * - 提供分类统计和导出功能
 *
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private com.resource.platform.module.resource.mapper.ResourceMapper resourceMapper;

    @Autowired
    @Lazy
    private CategoryService categoryServiceProxy;

    @Override
    public CategoryStatisticsVO getStatistics() {
        log.info("开始获取分类统计信息");

        CategoryStatisticsVO statistics = new CategoryStatisticsVO();

        Long totalCategories = categoryMapper.selectCount(null);
        statistics.setTotalCategories(totalCategories);
        log.debug("总分类数: {}", totalCategories);

        Long level1Categories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getLevel, 1)
        );
        statistics.setLevel1Categories(level1Categories);
        log.debug("一级分类数: {}", level1Categories);

        Long level2Categories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getLevel, 2)
        );
        statistics.setLevel2Categories(level2Categories);
        log.debug("二级分类数: {}", level2Categories);

        Long activeCategories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1)
        );
        statistics.setActiveCategories(activeCategories);
        log.debug("启用分类数: {}", activeCategories);

        statistics.setInactiveCategories(totalCategories - activeCategories);
        log.debug("禁用分类数: {}", totalCategories - activeCategories);

        log.info("获取分类统计信息成功: total={}, level1={}, level2={}, active={}, inactive={}",
            totalCategories, level1Categories, level2Categories,
            activeCategories, totalCategories - activeCategories);

        return statistics;
    }

    @Override
    @Cacheable(value = "category:tree", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<CategoryTreeVO> getCategoryTree() {
        log.info("开始构建分类树");

        List<Category> allCategories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId)
        );

        log.debug("查询到分类数量: {}", allCategories.size());

        List<CategoryTreeVO> tree = buildTree(allCategories, 0L);

        log.info("构建分类树成功: rootCount={}, totalCount={}", tree.size(), allCategories.size());
        return tree;
    }

    private List<CategoryTreeVO> buildTree(List<Category> categories, Long parentId) {
        List<CategoryTreeVO> tree = new ArrayList<>();

        for (Category category : categories) {
            if (category.getParentId().equals(parentId)) {
                CategoryTreeVO node = new CategoryTreeVO();
                BeanUtils.copyProperties(category, node);

                List<CategoryTreeVO> children = buildTree(categories, category.getId());
                node.setChildren(children);

                Long resourceCount = resourceMapper.selectCount(
                    new LambdaQueryWrapper<com.resource.platform.module.resource.entity.Resource>()
                        .eq(com.resource.platform.module.resource.entity.Resource::getCategoryId, category.getId())
                );
                node.setResourceCount(resourceCount);

                tree.add(node);
                log.debug("构建分类节点: id={}, name={}, parentId={}, childrenCount={}, resourceCount={}",
                    category.getId(), category.getName(), parentId, children.size(), resourceCount);
            }
        }

        return tree;
    }

    @Override
    public Page<Category> queryCategories(CategoryQueryDTO queryDTO) {
        log.info("开始分页查询分类: page={}, pageSize={}, keyword={}, parentId={}, level={}, status={}",
            queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getKeyword(),
            queryDTO.getParentId(), queryDTO.getLevel(), queryDTO.getStatus());

        long safePageNum = queryDTO.getPage() == null || queryDTO.getPage() < 1 ? 1L : queryDTO.getPage();
        int safePageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1 ? 20
            : Math.min(queryDTO.getPageSize(), MAX_PAGE_SIZE);
        Page<Category> page = new Page<>(safePageNum, safePageSize);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            log.debug("添加关键词搜索条件: keyword={}", queryDTO.getKeyword());
            wrapper.like(Category::getName, queryDTO.getKeyword());
        }

        if (queryDTO.getParentId() != null) {
            log.debug("添加父分类筛选条件: parentId={}", queryDTO.getParentId());
            wrapper.eq(Category::getParentId, queryDTO.getParentId());
        }

        if (queryDTO.getLevel() != null) {
            log.debug("添加层级筛选条件: level={}", queryDTO.getLevel());
            wrapper.eq(Category::getLevel, queryDTO.getLevel());
        }

        if (queryDTO.getStatus() != null) {
            log.debug("添加状态筛选条件: status={}", queryDTO.getStatus());
            wrapper.eq(Category::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByAsc(Category::getSortOrder)
            .orderByAsc(Category::getId);

        Page<Category> result = categoryMapper.selectPage(page, wrapper);

        log.info("分页查询分类成功: total={}, records={}", result.getTotal(), result.getRecords().size());
        return result;
    }

    @Override
    public Category getCategoryById(Long id) {
        log.info("开始查询分类详情: categoryId={}", id);

        Category category = categoryMapper.selectById(id);
        if (category == null) {
            log.warn("分类不存在: categoryId={}", id);
            throw new ResourceNotFoundException("分类", id);
        }

        log.info("查询分类详情成功: categoryId={}, name={}, level={}",
            id, category.getName(), category.getLevel());
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category createCategory(Category category) {
        log.info("开始创建分类: name={}, parentId={}", category.getName(), category.getParentId());

        Long count = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getName, category.getName())
                .eq(Category::getParentId, category.getParentId())
        );
        if (count > 0) {
            log.warn("同级分类名称已存在: name={}, parentId={}, count={}",
                category.getName(), category.getParentId(), count);
            throw new ValidationException("同级分类名称已存在");
        }

        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
            log.debug("设置为一级分类: level=1");
        } else {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                log.warn("父分类不存在: parentId={}", category.getParentId());
                throw new ValidationException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
            log.debug("设置为子分类: parentId={}, parentLevel={}, level={}",
                category.getParentId(), parent.getLevel(), category.getLevel());
        }

        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }

        categoryMapper.insert(category);

        log.info("创建分类成功: categoryId={}, name={}, level={}",
            category.getId(), category.getName(), category.getLevel());
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category updateCategory(Category category) {
        log.info("开始更新分类: categoryId={}, name={}", category.getId(), category.getName());

        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            log.warn("分类不存在: categoryId={}", category.getId());
            throw new ResourceNotFoundException("分类", category.getId());
        }

        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
        } else {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                log.warn("父分类不存在: parentId={}", category.getParentId());
                throw new ValidationException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        }

        Long count = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getName, category.getName())
                .eq(Category::getParentId, category.getParentId())
                .ne(Category::getId, category.getId())
        );
        if (count > 0) {
            log.warn("同级分类名称已存在: name={}, parentId={}, count={}",
                category.getName(), category.getParentId(), count);
            throw new ValidationException("同级分类名称已存在");
        }

        int rows = categoryMapper.updateById(category);
        if (rows <= 0) {
            throw new BusinessException("更新分类失败");
        }

        log.info("更新分类成功: categoryId={}, name={}, level={}, affectedRows={}",
            category.getId(), category.getName(), category.getLevel(), rows);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void deleteCategory(Long id) {
        log.info("开始删除分类: categoryId={}", id);

        Category category = categoryMapper.selectById(id);
        if (category == null) {
            log.warn("分类不存在: categoryId={}", id);
            throw new ResourceNotFoundException("分类", id);
        }

        Long childCount = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getParentId, id)
        );
        if (childCount > 0) {
            log.warn("分类下有子分类，无法删除: categoryId={}, childCount={}", id, childCount);
            throw new ValidationException("该分类下有子分类，无法删除");
        }

        Long resourceCount = resourceMapper.selectCount(
            new LambdaQueryWrapper<com.resource.platform.module.resource.entity.Resource>()
                .eq(com.resource.platform.module.resource.entity.Resource::getCategoryId, id)
        );
        if (resourceCount > 0) {
            log.warn("分类下有关联资源，无法删除: categoryId={}, resourceCount={}", id, resourceCount);
            throw new ValidationException("该分类下有 " + resourceCount + " 个资源，请先移除资源后再删除分类");
        }

        int rows = categoryMapper.deleteById(id);
        if (rows <= 0) {
            throw new BusinessException("删除分类失败");
        }

        log.info("删除分类成功: categoryId={}, name={}, affectedRows={}",
            id, category.getName(), rows);
    }

    @Override
    @CacheEvict(value = "category:tree", allEntries = true)
    public void deleteCategories(List<Long> ids) {
        log.info("开始批量删除分类: count={}, ids={}", ids.size(), ids);

        int successCount = 0;
        int failCount = 0;
        List<Long> failedIds = new ArrayList<>();

        for (Long id : ids) {
            try {
                categoryServiceProxy.deleteCategory(id);
                successCount++;
            } catch (Exception e) {
                log.error("批量删除分类失败: categoryId={}, error={}", id, e.getMessage(), e);
                failCount++;
                failedIds.add(id);
            }
        }

        log.info("批量删除分类完成: total={}, success={}, fail={}",
            ids.size(), successCount, failCount);
        if (failCount > 0) {
            throw new BusinessException("批量删除分类存在失败记录, 失败ID: " + failedIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSortOrder(Long id, Integer sortOrder) {
        log.info("开始更新分类排序: categoryId={}, sortOrder={}", id, sortOrder);

        Category category = categoryMapper.selectById(id);
        if (category == null) {
            log.warn("分类不存在: categoryId={}", id);
            throw new ResourceNotFoundException("分类", id);
        }

        Integer oldSortOrder = category.getSortOrder();
        category.setSortOrder(sortOrder);

        int rows = categoryMapper.updateById(category);
        if (rows <= 0) {
            throw new BusinessException("更新分类排序失败");
        }

        log.info("更新分类排序成功: categoryId={}, oldSortOrder={}, newSortOrder={}, affectedRows={}",
            id, oldSortOrder, sortOrder, rows);
    }

    @Override
    public String exportCategories() {
        log.info("开始导出分类数据");

        List<Category> categories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId)
        );

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

        try {
            String fileName = "categories_export_" + System.currentTimeMillis() + ".csv";
            String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName;
            File file = new File(filePath);
            FileUtils.writeStringToFile(file, csv.toString(), StandardCharsets.UTF_8);

            log.info("导出分类成功: filePath={}, count={}, size={} bytes",
                filePath, categories.size(), csv.length());
            return filePath;
        } catch (IOException e) {
            log.error("导出分类失败: error={}", e.getMessage(), e);
            throw new BusinessException("导出分类失败: " + e.getMessage());
        }
    }
}
