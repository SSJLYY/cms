package com.resource.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.Result;
import com.resource.platform.dto.CategoryQueryDTO;
import com.resource.platform.entity.Category;
import com.resource.platform.service.CategoryService;
import com.resource.platform.vo.CategoryStatisticsVO;
import com.resource.platform.vo.CategoryTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 * 
 * 功能说明：
 * - 提供分类的增删改查接口
 * - 支持分类树形结构查询
 * - 提供分类统计信息接口
 * - 支持分类的批量操作
 * - 支持分类排序调整
 * - 提供分类数据导出功能
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

    /**
     * 获取分类统计信息
     * 
     * 业务逻辑：
     * 1. 统计总分类数
     * 2. 统计各层级分类数量
     * 3. 统计启用和禁用的分类数量
     * 4. 返回统计结果
     * 
     * @return 分类统计信息对象
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取分类统计信息")
    public Result<CategoryStatisticsVO> getStatistics() {
        // 记录请求开始
        log.info("开始获取分类统计信息");
        
        try {
            // 调用服务层获取统计信息
            CategoryStatisticsVO statistics = categoryService.getStatistics();
            
            // 记录获取成功
            log.info("获取分类统计信息成功: total={}, level1={}, level2={}, active={}", 
                statistics.getTotalCategories(), statistics.getLevel1Categories(),
                statistics.getLevel2Categories(), statistics.getActiveCategories());
            
            return Result.success(statistics);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取分类统计信息失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取分类树
     * 
     * 业务逻辑：
     * 1. 查询所有分类数据
     * 2. 构建树形结构（父子关系）
     * 3. 统计每个分类下的资源数量
     * 4. 返回树形结构数据
     * 
     * @return 分类树形结构列表
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        // 记录请求开始
        log.info("开始获取分类树");
        
        try {
            // 调用服务层构建分类树
            List<CategoryTreeVO> tree = categoryService.getCategoryTree();
            
            // 记录获取成功
            log.info("获取分类树成功: rootCount={}", tree.size());
            
            return Result.success(tree);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取分类树失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 查询分类列表
     * 
     * 业务逻辑：
     * 1. 接收查询条件（关键词、父分类、层级、状态）
     * 2. 执行分页查询
     * 3. 按排序字段排序
     * 4. 返回分页结果
     * 
     * @param queryDTO 查询条件对象
     * @return 分类分页结果
     */
    @PostMapping("/query")
    @Operation(summary = "查询分类列表")
    public Result<Page<Category>> queryCategories(@RequestBody CategoryQueryDTO queryDTO) {
        // 记录请求开始
        log.info("开始查询分类列表: page={}, pageSize={}, keyword={}, parentId={}, level={}, status={}", 
            queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getKeyword(),
            queryDTO.getParentId(), queryDTO.getLevel(), queryDTO.getStatus());
        
        try {
            // 调用服务层执行分页查询
            Page<Category> page = categoryService.queryCategories(queryDTO);
            
            // 记录查询成功
            log.info("查询分类列表成功: total={}, records={}", page.getTotal(), page.getRecords().size());
            
            return Result.success(page);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("查询分类列表失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取分类详情
     * 
     * 业务逻辑：
     * 1. 根据ID查询分类信息
     * 2. 验证分类是否存在
     * 3. 返回分类详情
     * 
     * @param id 分类ID
     * @return 分类详情对象
     * @throws ResourceNotFoundException 当分类不存在时抛出
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始获取分类详情: categoryId={}", id);
        
        try {
            // 调用服务层查询分类
            Category category = categoryService.getCategoryById(id);
            
            // 记录查询成功
            log.info("获取分类详情成功: categoryId={}, name={}", id, category.getName());
            
            return Result.success(category);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("获取分类详情失败: categoryId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建分类
     * 
     * 业务逻辑：
     * 1. 验证分类名称唯一性
     * 2. 验证父分类是否存在
     * 3. 设置分类层级
     * 4. 设置默认值（状态、排序）
     * 5. 保存分类到数据库
     * 6. 返回创建成功的分类
     * 
     * @param category 分类对象
     * @return 创建成功的分类对象
     * @throws ValidationException 当数据验证失败时抛出
     */
    @PostMapping
    @Operation(summary = "创建分类")
    @OperationLog(module = "分类管理", type = "创建", description = "创建分类", audit = true)
    public Result<Category> createCategory(@RequestBody Category category) {
        // 记录请求开始
        log.info("开始创建分类: name={}, parentId={}", category.getName(), category.getParentId());
        
        try {
            // 调用服务层创建分类
            Category created = categoryService.createCategory(category);
            
            // 记录创建成功
            log.info("创建分类成功: categoryId={}, name={}, level={}", 
                created.getId(), created.getName(), created.getLevel());
            
            return Result.success(created);
            
        } catch (Exception e) {
            // 记录创建失败
            log.error("创建分类失败: name={}, parentId={}, error={}", 
                category.getName(), category.getParentId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 更新分类
     * 
     * 业务逻辑：
     * 1. 验证分类是否存在
     * 2. 验证分类名称唯一性（排除自身）
     * 3. 验证父分类是否存在
     * 4. 更新分类层级
     * 5. 更新分类信息到数据库
     * 6. 返回更新后的分类
     * 
     * @param id 分类ID
     * @param category 分类对象
     * @return 更新后的分类对象
     * @throws ResourceNotFoundException 当分类不存在时抛出
     * @throws ValidationException 当数据验证失败时抛出
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    @OperationLog(module = "分类管理", type = "更新", description = "更新分类", audit = true)
    public Result<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        // 记录请求开始
        log.info("开始更新分类: categoryId={}, name={}", id, category.getName());
        
        try {
            // 设置分类ID
            category.setId(id);
            
            // 调用服务层更新分类
            Category updated = categoryService.updateCategory(category);
            
            // 记录更新成功
            log.info("更新分类成功: categoryId={}, name={}, level={}", 
                updated.getId(), updated.getName(), updated.getLevel());
            
            return Result.success(updated);
            
        } catch (Exception e) {
            // 记录更新失败
            log.error("更新分类失败: categoryId={}, name={}, error={}", 
                id, category.getName(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除分类
     * 
     * 业务逻辑：
     * 1. 验证分类是否存在
     * 2. 检查是否有子分类
     * 3. 检查是否有资源使用该分类
     * 4. 删除分类
     * 
     * @param id 分类ID
     * @return 删除成功响应
     * @throws ResourceNotFoundException 当分类不存在时抛出
     * @throws ValidationException 当分类下有子分类或资源时抛出
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    @OperationLog(module = "分类管理", type = "删除", description = "删除分类", audit = true)
    public Result<Void> deleteCategory(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始删除分类: categoryId={}", id);
        
        try {
            // 调用服务层删除分类
            categoryService.deleteCategory(id);
            
            // 记录删除成功
            log.info("删除分类成功: categoryId={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录删除失败
            log.error("删除分类失败: categoryId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 批量删除分类
     * 
     * 业务逻辑：
     * 1. 遍历分类ID列表
     * 2. 逐个删除分类
     * 3. 记录删除失败的分类
     * 4. 返回批量删除结果
     * 
     * @param ids 分类ID列表
     * @return 批量删除成功响应
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除分类")
    @OperationLog(module = "分类管理", type = "批量删除", description = "批量删除分类", audit = true)
    public Result<Void> deleteCategories(@RequestBody List<Long> ids) {
        // 记录请求开始
        log.info("开始批量删除分类: count={}, ids={}", ids.size(), ids);
        
        try {
            // 调用服务层批量删除分类
            categoryService.deleteCategories(ids);
            
            // 记录批量删除成功
            log.info("批量删除分类成功: count={}", ids.size());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录批量删除失败
            log.error("批量删除分类失败: ids={}, error={}", ids, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 调整分类排序
     * 
     * 业务逻辑：
     * 1. 验证分类是否存在
     * 2. 更新分类的排序值
     * 3. 保存到数据库
     * 
     * @param id 分类ID
     * @param sortOrder 新的排序值
     * @return 更新成功响应
     * @throws ResourceNotFoundException 当分类不存在时抛出
     */
    @PutMapping("/{id}/sort")
    @Operation(summary = "调整分类排序")
    @OperationLog(module = "分类管理", type = "排序", description = "调整分类排序")
    public Result<Void> updateSortOrder(@PathVariable Long id, @RequestParam Integer sortOrder) {
        // 记录请求开始
        log.info("开始调整分类排序: categoryId={}, sortOrder={}", id, sortOrder);
        
        try {
            // 调用服务层更新排序
            categoryService.updateSortOrder(id, sortOrder);
            
            // 记录更新成功
            log.info("调整分类排序成功: categoryId={}, sortOrder={}", id, sortOrder);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录更新失败
            log.error("调整分类排序失败: categoryId={}, sortOrder={}, error={}", 
                id, sortOrder, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 导出分类
     * 
     * 业务逻辑：
     * 1. 查询所有分类数据
     * 2. 生成CSV格式文件
     * 3. 保存到临时目录
     * 4. 返回文件路径
     * 
     * @return 导出文件的路径
     * @throws RuntimeException 当导出失败时抛出
     */
    @GetMapping("/export")
    @Operation(summary = "导出分类")
    @OperationLog(module = "分类管理", type = "导出", description = "导出分类", audit = true)
    public Result<String> exportCategories() {
        // 记录请求开始
        log.info("开始导出分类数据");
        
        try {
            // 调用服务层导出分类
            String filePath = categoryService.exportCategories();
            
            // 记录导出成功
            log.info("导出分类数据成功: filePath={}", filePath);
            
            return Result.success(filePath);
            
        } catch (Exception e) {
            // 记录导出失败
            log.error("导出分类数据失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
}
