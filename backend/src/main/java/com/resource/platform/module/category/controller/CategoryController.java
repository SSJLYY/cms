package com.resource.platform.module.category.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.BizErrorCode;
import com.resource.platform.common.Result;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.module.category.dto.CategoryQueryDTO;
import com.resource.platform.module.category.entity.Category;
import com.resource.platform.module.category.service.CategoryService;
import com.resource.platform.module.category.vo.CategoryStatisticsVO;
import com.resource.platform.module.category.vo.CategoryTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 *
 * @author 系统
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@Tag(name = "分类管理", description = "分类的增删改查、树形结构等接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/statistics")
    @Operation(summary = "获取分类统计信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CategoryStatisticsVO> getStatistics() {
        CategoryStatisticsVO statistics = categoryService.getStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        List<CategoryTreeVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @PostMapping("/query")
    @Operation(summary = "查询分类列表")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<Category>> queryCategories(@RequestBody CategoryQueryDTO queryDTO) {
        Page<Category> page = categoryService.queryCategories(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建分类")
    @OperationLog(module = "分类管理", type = "创建", description = "创建分类", audit = true)
    public Result<Category> createCategory(@RequestBody Category category) {
        Category created = categoryService.createCategory(category);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新分类")
    @OperationLog(module = "分类管理", type = "更新", description = "更新分类", audit = true)
    public Result<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        Category updated = categoryService.updateCategory(category);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除分类")
    @OperationLog(module = "分类管理", type = "删除", description = "删除分类", audit = true)
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量删除分类")
    @OperationLog(module = "分类管理", type = "批量删除", description = "批量删除分类", audit = true)
    public Result<Void> deleteCategories(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("批量删除分类ID列表为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "删除ID列表不能为空");
        }
        if (ids.size() > 100) {
            log.warn("批量删除分类数量超限: count={}", ids.size());
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "批量删除最多支持100条");
        }
        categoryService.deleteCategories(ids);
        return Result.success();
    }

    @PutMapping("/{id}/sort")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "调整分类排序")
    @OperationLog(module = "分类管理", type = "排序", description = "调整分类排序")
    public Result<Void> updateSortOrder(@PathVariable Long id, @RequestParam Integer sortOrder) {
        categoryService.updateSortOrder(id, sortOrder);
        return Result.success();
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出分类")
    @OperationLog(module = "分类管理", type = "导出", description = "导出分类", audit = true)
    public Result<String> exportCategories() {
        String filePath = categoryService.exportCategories();
        return Result.success(filePath);
    }
}
