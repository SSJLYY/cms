package com.resource.platform.module.resource.controller;

import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.annotation.OperationLog;
import com.resource.platform.module.resource.dto.BatchOperationDTO;
import com.resource.platform.module.resource.dto.ResourceDTO;
import com.resource.platform.module.resource.dto.ResourceQueryDTO;
import com.resource.platform.module.resource.service.ResourceService;
import com.resource.platform.module.resource.vo.ResourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 资源管理控制器
 * 
 * 功能说明：
 * - 提供资源的增删改查接口（管理后台）
 * - 提供资源列表查询接口（客户前台）
 * - 处理资源下载记录和访问统计
 * - 管理IP下载限制和重复下载检测
 * - 支持资源状态切换和软删除
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "资源管理")
@RestController
@RequestMapping("/api/resources")
@Validated
public class ResourceController {

    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private com.resource.platform.service.AccessLogService accessLogService;
    
    @Autowired
    private com.resource.platform.service.IpDownloadService ipDownloadService;

    /**
     * 获取资源列表（客户前台）
     * 
     * 业务逻辑：
     * 1. 查询所有已发布状态的资源
     * 2. 按创建时间倒序排列
     * 3. 返回资源列表
     * 
     * @return 已发布的资源列表
     */
    @Operation(summary = "获取资源列表（客户前台）")
    @GetMapping("/public/list")
    public Result<List<ResourceVO>> getPublicResourceList() {
        List<ResourceVO> resources = resourceService.getPublishedResources();
        return Result.success(resources);
    }

    /**
     * 获取资源列表（管理后台，支持搜索和筛选）
     * 
     * 业务逻辑：
     * 1. 接收查询条件（关键词、分类、状态、排序等）
     * 2. 执行分页查询
     * 3. 返回分页结果
     * 
     * @param query 查询条件对象，包含分页、搜索、筛选参数
     * @return 资源分页结果
     */
    @Operation(summary = "获取资源列表（管理后台，支持搜索和筛选）")
    @GetMapping("/admin/list")
    public Result<PageResult<ResourceVO>> getAdminResourceList(ResourceQueryDTO query) {
        return Result.success(resourceService.queryResources(query));
    }

    /**
     * 创建资源
     * 
     * 业务逻辑：
     * 1. 验证资源数据的完整性
     * 2. 保存资源基本信息
     * 3. 保存资源关联的下载链接
     * 4. 保存资源关联的图片
     * 5. 返回创建成功的资源信息
     * 
     * @param dto 资源数据传输对象，包含资源的所有信息
     * @return 创建成功的资源对象
     * @throws RuntimeException 当数据验证失败或保存失败时抛出
     */
    @Operation(summary = "创建资源")
    @PostMapping("/admin/create")
    @OperationLog(module = "资源管理", type = "创建", description = "创建资源")
    public Result<ResourceVO> createResource(@Validated @RequestBody ResourceDTO dto) {
        return Result.success(resourceService.createResource(dto));
    }

    /**
     * 更新资源
     * 
     * 业务逻辑：
     * 1. 验证资源是否存在
     * 2. 更新资源基本信息
     * 3. 更新资源关联的下载链接
     * 4. 更新资源关联的图片
     * 5. 处理被移除图片的状态
     * 6. 返回更新后的资源信息
     * 
     * @param id 资源ID
     * @param dto 资源数据传输对象，包含更新后的资源信息
     * @return 更新后的资源对象
     * @throws RuntimeException 当资源不存在或更新失败时抛出
     */
    @Operation(summary = "更新资源")
    @PutMapping("/admin/update/{id}")
    @OperationLog(module = "资源管理", type = "更新", description = "更新资源")
    public Result<ResourceVO> updateResource(@PathVariable Long id, @Validated @RequestBody ResourceDTO dto) {
        return Result.success(resourceService.updateResource(id, dto));
    }

    /**
     * 删除资源（软删除）
     * 
     * 业务逻辑：
     * 1. 验证资源是否存在
     * 2. 删除资源基本信息
     * 3. 删除资源关联的下载链接
     * 4. 删除资源关联的图片关系
     * 5. 更新相关图片的使用状态
     * 
     * @param id 资源ID
     * @return 删除成功响应
     * @throws RuntimeException 当资源不存在或删除失败时抛出
     */
    @Operation(summary = "删除资源（软删除）")
    @DeleteMapping("/admin/delete/{id}")
    @OperationLog(module = "资源管理", type = "删除", description = "删除资源")
    public Result<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return Result.success();
    }

    /**
     * 切换资源状态
     * 
     * 业务逻辑：
     * 1. 查询资源当前状态
     * 2. 切换状态（启用<->禁用）
     * 3. 更新资源状态
     * 
     * @param id 资源ID
     * @return 切换成功响应
     * @throws RuntimeException 当资源不存在时抛出
     */
    @Operation(summary = "切换资源状态")
    @PutMapping("/admin/toggle-status/{id}")
    @OperationLog(module = "资源管理", type = "状态切换", description = "切换资源状态")
    public Result<Void> toggleResourceStatus(@PathVariable Long id) {
        resourceService.toggleStatus(id);
        return Result.success();
    }

    /**
     * 记录下载次数
     * 
     * 业务逻辑：
     * 1. 获取客户端IP地址
     * 2. 检查今日是否已下载过此资源（防止重复计数）
     * 3. 检查IP今日下载次数是否达到上限
     * 4. 记录资源下载次数
     * 5. 记录访问日志
     * 6. 记录IP下载记录
     * 
     * @param id 资源ID
     * @param request HTTP请求对象，用于获取IP地址
     * @return 下载记录成功响应，或错误提示
     */
    @Operation(summary = "记录下载次数")
    @PostMapping("/public/download/{id}")
    public Result<Void> recordDownload(@PathVariable Long id, HttpServletRequest request) {

        // 步骤1：获取客户端真实IP地址
        // 考虑代理和负载均衡的情况
        String ipAddress = getClientIp(request);
        
            // 步骤2：检查是否已下载过此资源
            // 防止同一IP重复下载同一资源导致计数不准确
            if (ipDownloadService.hasDownloadedToday(ipAddress, id)) {
                log.warn("重复下载检测: resourceId={}, ip={}, 今日已下载过", id, ipAddress);
                return Result.error(208, "您今日已下载过此资源，重复下载不会计数");
            }
            
            // 步骤3：检查IP下载限制
            // 防止恶意刷下载次数
            if (!ipDownloadService.canDownload(ipAddress)) {
                log.warn("下载次数限制: ip={}, 今日下载次数已达上限", ipAddress);
                return Result.error(429, "今日下载次数已达上限，请明天再试");
            }
            
            // 步骤4：记录资源下载次数
            // 更新资源表的download_count字段
            log.debug("更新资源下载计数: resourceId={}", id);
            resourceService.recordDownload(id);
            
            // 步骤5：记录访问日志
            // 保存详细的下载日志，包含IP、时间等信息
            log.debug("记录访问日志: resourceId={}, ip={}", id, ipAddress);
            accessLogService.recordDownload(id, request);
            
            // 步骤6：记录IP下载记录
            // 用于后续的重复下载检测和限制检查
            log.debug("记录IP下载记录: resourceId={}, ip={}", id, ipAddress);
            ipDownloadService.recordDownload(ipAddress, id);
            
            
            return Result.success();
    }
    
    /**
     * 获取IP今日剩余下载次数
     * 
     * 业务逻辑：
     * 1. 获取客户端IP地址
     * 2. 查询该IP今日已下载次数
     * 3. 计算剩余可下载次数
     * 4. 返回剩余次数
     * 
     * @param request HTTP请求对象，用于获取IP地址
     * @return 剩余下载次数
     */
    @Operation(summary = "获取IP今日剩余下载次数")
    @GetMapping("/public/remaining-downloads")
    public Result<Integer> getRemainingDownloads(HttpServletRequest request) {

        // 获取客户端IP地址
        String ipAddress = getClientIp(request);
        
        // 记录查询请求
        log.debug("查询IP剩余下载次数: ip={}", ipAddress);
        
            // 调用服务层查询剩余次数
            int remaining = ipDownloadService.getRemainingDownloads(ipAddress);
            
            // 记录查询结果
            log.debug("查询IP剩余下载次数成功: ip={}, remaining={}", ipAddress, remaining);
            
            return Result.success(remaining);
    }
    
    /**
     * 检查是否已下载过此资源
     * 
     * 业务逻辑：
     * 1. 获取客户端IP地址
     * 2. 查询该IP今日是否已下载过指定资源
     * 3. 返回检查结果
     * 
     * @param id 资源ID
     * @param request HTTP请求对象，用于获取IP地址
     * @return true表示已下载过，false表示未下载过
     */
    @Operation(summary = "检查是否已下载过此资源")
    @GetMapping("/public/check-downloaded/{id}")
    public Result<Boolean> checkDownloaded(@PathVariable Long id, HttpServletRequest request) {

        // 获取客户端IP地址
        String ipAddress = getClientIp(request);
        
        // 记录检查请求
        log.debug("检查资源是否已下载: resourceId={}, ip={}", id, ipAddress);
        
            boolean downloaded = ipDownloadService.hasDownloadedToday(ipAddress, id);
            
            // 记录检查结果
            log.debug("检查资源下载状态完成: resourceId={}, ip={}, downloaded={}", id, ipAddress, downloaded);
            
            return Result.success(downloaded);
    }
    
    /**
     * 获取客户端真实IP地址（委托给IpUtil统一处理）
     */
    private String getClientIp(HttpServletRequest request) {
        return com.resource.platform.util.IpUtil.getClientIp(request);
    }
    
    /**
     * 记录访问
     * 
     * 业务逻辑：
     * 1. 获取客户端IP和访问信息
     * 2. 记录资源访问日志
     * 3. 用于统计资源的浏览量
     * 
     * @param id 资源ID
     * @param request HTTP请求对象，用于获取访问信息
     * @return 记录成功响应
     */
    @Operation(summary = "记录访问")
    @PostMapping("/public/visit/{id}")
    public Result<Void> recordVisit(@PathVariable Long id, HttpServletRequest request) {
        accessLogService.recordVisit(id, request);
        return Result.success();
    }
    
    /**
     * 批量发布资源
     * 
     * 业务逻辑：
     * 1. 接收资源ID列表
     * 2. 批量将资源状态设置为已发布（1）
     * 3. 返回更新成功的数量
     * 
     * @param request 批量操作请求对象
     * @return 更新成功的数量
     */
    @Operation(summary = "批量发布资源")
    @PutMapping("/admin/batch-publish")
    @OperationLog(module = "资源管理", type = "批量发布", description = "批量发布资源")
    public Result<Integer> batchPublishResources(@Validated @RequestBody BatchOperationDTO request) {
        return Result.success(resourceService.batchUpdateStatus(request.getIds(), 1));
    }
    
    /**
     * 批量下架资源
     * 
     * 业务逻辑：
     * 1. 接收资源ID列表
     * 2. 批量将资源状态设置为已下架（0）
     * 3. 返回更新成功的数量
     * 
     * @param request 批量操作请求对象
     * @return 更新成功的数量
     */
    @Operation(summary = "批量下架资源")
    @PutMapping("/admin/batch-unpublish")
    @OperationLog(module = "资源管理", type = "批量下架", description = "批量下架资源")
    public Result<Integer> batchUnpublishResources(@Validated @RequestBody BatchOperationDTO request) {
        return Result.success(resourceService.batchUpdateStatus(request.getIds(), 0));
    }
    
    /**
     * 批量删除资源
     * 
     * 业务逻辑：
     * 1. 接收资源ID列表
     * 2. 批量删除资源（软删除）
     * 3. 同时删除关联的下载链接和图片关系
     * 4. 返回删除成功的数量
     * 
     * @param request 批量操作请求对象
     * @return 删除成功的数量
     */
    @Operation(summary = "批量删除资源")
    @DeleteMapping("/admin/batch-delete")
    @OperationLog(module = "资源管理", type = "批量删除", description = "批量删除资源")
    public Result<Integer> batchDeleteResources(@Validated @RequestBody BatchOperationDTO request) {
        return Result.success(resourceService.batchDelete(request.getIds()));
    }
    
    /**
     * 批量移动资源到指定分类
     * 
     * 业务逻辑：
     * 1. 接收资源ID列表和目标分类ID
     * 2. 验证目标分类是否存在
     * 3. 批量更新资源的分类
     * 4. 返回移动成功的数量
     * 
     * @param request 批量移动请求对象
     * @return 移动成功的数量
     */
    @Operation(summary = "批量移动资源到指定分类")
    @PutMapping("/admin/batch-move-category")
    @OperationLog(module = "资源管理", type = "批量移动", description = "批量移动资源到指定分类")
    public Result<Integer> batchMoveToCategory(@Validated @RequestBody BatchOperationDTO.BatchMoveCategoryRequest request) {
        return Result.success(resourceService.batchMoveToCategory(request.getIds(), request.getCategoryId()));
    }
}
