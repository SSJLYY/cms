package com.resource.platform.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.Result;
import com.resource.platform.entity.LinkType;
import com.resource.platform.service.LinkTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 链接类型管理控制器
 * 
 * 功能说明：
 * - 提供链接类型的查询接口，支持公开和管理后台查询
 * - 提供链接类型的增删改查管理功能
 * - 支持链接类型的启用和禁用状态管理
 * - 提供批量删除链接类型功能
 * - 管理网盘类型的配置和排序
 * - 记录链接类型的管理操作日志
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/link-types")
@RequiredArgsConstructor
@Tag(name = "链接类型管理", description = "链接类型管理相关接口")
public class LinkTypeController {
    
    private final LinkTypeService linkTypeService;
    
    /**
     * 获取所有启用的链接类型（公开接口）
     * 
     * 业务逻辑：
     * 1. 查询状态为启用的链接类型
     * 2. 按排序字段升序排列
     * 3. 供前台页面展示使用
     * 4. 记录访问日志
     * 
     * @return 启用的链接类型列表
     */
    @GetMapping("/public/list")
    @Operation(summary = "获取所有启用的链接类型")
    public Result<List<LinkType>> listEnabled() {
        // 记录请求开始
        log.info("开始获取启用的链接类型列表");
        
        try {
            // 调用Service层查询启用的链接类型
            List<LinkType> list = linkTypeService.listEnabled();
            
            // 记录成功结果
            log.info("启用链接类型列表获取成功: count={}", list.size());
            
            return Result.success(list);
            
        } catch (Exception e) {
            // 记录异常
            log.error("启用链接类型列表获取失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取所有链接类型（管理后台）
     * 
     * 业务逻辑：
     * 1. 查询所有链接类型（包括启用和禁用）
     * 2. 按排序字段升序排列
     * 3. 供管理后台使用
     * 4. 记录管理操作日志
     * 
     * @return 所有链接类型列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有链接类型")
    public Result<List<LinkType>> listAll() {
        // 记录请求开始
        log.info("开始获取所有链接类型列表");
        
        try {
            // 调用Service层查询所有链接类型
            List<LinkType> list = linkTypeService.listAll();
            
            // 记录成功结果
            log.info("所有链接类型列表获取成功: count={}", list.size());
            
            return Result.success(list);
            
        } catch (Exception e) {
            // 记录异常
            log.error("所有链接类型列表获取失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 添加链接类型
     * 
     * 业务逻辑：
     * 1. 验证链接类型数据的完整性
     * 2. 检查类型名称是否重复
     * 3. 设置默认的排序和状态
     * 4. 保存链接类型到数据库
     * 5. 记录创建操作日志
     * 
     * @param linkType 链接类型实体对象
     * @return 操作结果
     * @throws ValidationException 当数据验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     */
    @PostMapping
    @Operation(summary = "添加链接类型")
    @OperationLog(module = "链接类型管理", type = "创建", description = "创建链接类型")
    public Result<Void> add(@Validated @RequestBody LinkType linkType) {
        // 记录请求开始
        log.info("开始创建链接类型: name={}, icon={}, description={}", 
            linkType.getName(), linkType.getIcon(), linkType.getDescription());
        
        try {
            // 步骤1：验证参数
            // 检查链接类型数据的完整性
            if (linkType.getName() == null || linkType.getName().trim().isEmpty()) {
                log.warn("链接类型名称为空");
                throw new IllegalArgumentException("链接类型名称不能为空");
            }
            
            // 步骤2：调用Service层创建链接类型
            linkTypeService.save(linkType);
            
            // 记录成功结果
            log.info("链接类型创建成功: id={}, name={}", 
                linkType.getId(), linkType.getName());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("链接类型创建失败: name={}, error={}", 
                linkType.getName(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 更新链接类型
     * 
     * 业务逻辑：
     * 1. 验证链接类型ID和数据的有效性
     * 2. 检查链接类型是否存在
     * 3. 验证类型名称是否重复（排除自己）
     * 4. 更新链接类型信息
     * 5. 记录更新操作日志
     * 
     * @param id 链接类型ID
     * @param linkType 链接类型实体对象
     * @return 操作结果
     * @throws ValidationException 当数据验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     * @throws ResourceNotFoundException 当链接类型不存在时抛出
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新链接类型")
    @OperationLog(module = "链接类型管理", type = "更新", description = "更新链接类型")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody LinkType linkType) {
        // 记录请求开始
        log.info("开始更新链接类型: id={}, name={}", id, linkType.getName());
        
        try {
            // 步骤1：验证参数
            // 检查链接类型ID是否有效
            if (id == null || id <= 0) {
                log.warn("链接类型ID无效: id={}", id);
                throw new IllegalArgumentException("链接类型ID无效");
            }
            
            if (linkType.getName() == null || linkType.getName().trim().isEmpty()) {
                log.warn("链接类型名称为空: id={}", id);
                throw new IllegalArgumentException("链接类型名称不能为空");
            }
            
            // 步骤2：设置ID并调用Service层更新
            linkType.setId(id);
            linkTypeService.updateById(linkType);
            
            // 记录成功结果
            log.info("链接类型更新成功: id={}, name={}", id, linkType.getName());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("链接类型更新失败: id={}, name={}, error={}", 
                id, linkType.getName(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 删除链接类型
     * 
     * 业务逻辑：
     * 1. 验证链接类型ID的有效性
     * 2. 检查链接类型是否存在
     * 3. 检查是否有关联的下载链接
     * 4. 执行删除操作
     * 5. 记录删除操作日志
     * 
     * @param id 链接类型ID
     * @return 操作结果
     * @throws ResourceNotFoundException 当链接类型不存在时抛出
     * @throws BusinessException 当存在关联数据时抛出
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除链接类型")
    @OperationLog(module = "链接类型管理", type = "删除", description = "删除链接类型")
    public Result<Void> delete(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始删除链接类型: id={}", id);
        
        try {
            // 步骤1：验证参数
            // 检查链接类型ID是否有效
            if (id == null || id <= 0) {
                log.warn("链接类型ID无效: id={}", id);
                throw new IllegalArgumentException("链接类型ID无效");
            }
            
            // 步骤2：调用Service层删除链接类型
            // Service层会检查类型是否存在以及是否有关联数据
            linkTypeService.removeById(id);
            
            // 记录成功结果
            log.info("链接类型删除成功: id={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("链接类型删除失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 批量删除链接类型
     * 
     * 业务逻辑：
     * 1. 验证ID列表的有效性
     * 2. 检查ID列表是否为空
     * 3. 检查每个链接类型是否存在关联数据
     * 4. 执行批量删除操作
     * 5. 记录批量删除结果
     * 
     * @param ids 链接类型ID列表
     * @return 操作结果
     * @throws BusinessException 当ID列表为空或存在关联数据时抛出
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除链接类型")
    @OperationLog(module = "链接类型管理", type = "批量删除", description = "批量删除链接类型")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        // 记录请求开始
        log.info("开始批量删除链接类型: ids={}, count={}", ids, ids != null ? ids.size() : 0);
        
        try {
            // 步骤1：验证参数
            // 检查ID列表是否为空
            if (ids == null || ids.isEmpty()) {
                log.warn("批量删除链接类型ID列表为空");
                throw new IllegalArgumentException("删除ID列表不能为空");
            }
            
            // 检查ID列表中是否有无效ID
            for (Long id : ids) {
                if (id == null || id <= 0) {
                    log.warn("批量删除包含无效ID: id={}", id);
                    throw new IllegalArgumentException("包含无效的链接类型ID: " + id);
                }
            }
            
            // 步骤2：调用Service层批量删除链接类型
            linkTypeService.removeByIds(ids);
            
            // 记录成功结果
            log.info("链接类型批量删除成功: count={}", ids.size());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("链接类型批量删除失败: ids={}, error={}", 
                ids, e.getMessage(), e);
            throw e;
        }
    }
}
