package com.resource.platform.controller;

import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.BatchOperationDTO;
import com.resource.platform.dto.ResourceDTO;
import com.resource.platform.dto.ResourceQueryDTO;
import com.resource.platform.service.ResourceService;
import com.resource.platform.vo.ResourceVO;
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
        // 记录请求开始
        log.info("开始获取公开资源列表");
        
        try {
            // 调用服务层查询已发布的资源
            List<ResourceVO> resources = resourceService.getPublishedResources();
            
            // 记录查询成功
            log.info("获取公开资源列表成功: count={}", resources.size());
            
            return Result.success(resources);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("获取公开资源列表失败: error={}", e.getMessage(), e);
            throw e;
        }
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
        // 记录请求开始
        log.info("开始查询管理后台资源列表: page={}, pageSize={}, keyword={}, categoryId={}, status={}", 
            query.getPageNum(), query.getPageSize(), query.getKeyword(), 
            query.getCategoryId(), query.getStatus());
        
        try {
            // 调用服务层执行分页查询
            PageResult<ResourceVO> result = resourceService.queryResources(query);
            
            // 记录查询成功
            log.info("查询管理后台资源列表成功: total={}, records={}", 
                result.getTotal(), result.getRecords().size());
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("查询管理后台资源列表失败: query={}, error={}", query, e.getMessage(), e);
            throw e;
        }
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
    public Result<ResourceVO> createResource(@Validated @RequestBody ResourceDTO dto) {
        // 记录请求开始
        log.info("开始创建资源: title={}, categoryId={}", dto.getTitle(), dto.getCategoryId());
        
        try {
            // 调用服务层创建资源
            ResourceVO resourceVO = resourceService.createResource(dto);
            
            // 记录创建成功
            log.info("创建资源成功: resourceId={}, title={}", resourceVO.getId(), resourceVO.getTitle());
            
            return Result.success(resourceVO);
            
        } catch (Exception e) {
            // 记录创建失败
            log.error("创建资源失败: title={}, error={}", dto.getTitle(), e.getMessage(), e);
            throw e;
        }
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
    public Result<ResourceVO> updateResource(@PathVariable Long id, @Validated @RequestBody ResourceDTO dto) {
        // 记录请求开始
        log.info("开始更新资源: resourceId={}, title={}", id, dto.getTitle());
        
        try {
            // 调用服务层更新资源
            ResourceVO resourceVO = resourceService.updateResource(id, dto);
            
            // 记录更新成功
            log.info("更新资源成功: resourceId={}, title={}", id, resourceVO.getTitle());
            
            return Result.success(resourceVO);
            
        } catch (Exception e) {
            // 记录更新失败
            log.error("更新资源失败: resourceId={}, title={}, error={}", id, dto.getTitle(), e.getMessage(), e);
            throw e;
        }
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
    public Result<Void> deleteResource(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始删除资源: resourceId={}", id);
        
        try {
            // 调用服务层删除资源
            resourceService.deleteResource(id);
            
            // 记录删除成功
            log.info("删除资源成功: resourceId={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录删除失败
            log.error("删除资源失败: resourceId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
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
    public Result<Void> toggleResourceStatus(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始切换资源状态: resourceId={}", id);
        
        try {
            // 调用服务层切换状态
            resourceService.toggleStatus(id);
            
            // 记录切换成功
            log.info("切换资源状态成功: resourceId={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录切换失败
            log.error("切换资源状态失败: resourceId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
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
        
        // 记录下载请求开始
        log.info("开始记录资源下载: resourceId={}, ip={}", id, ipAddress);
        
        try {
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
            
            // 记录下载成功
            log.info("记录资源下载成功: resourceId={}, ip={}", id, ipAddress);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录下载失败
            log.error("记录资源下载失败: resourceId={}, ip={}, error={}", id, ipAddress, e.getMessage(), e);
            throw e;
        }
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
        
        try {
            // 调用服务层查询剩余次数
            int remaining = ipDownloadService.getRemainingDownloads(ipAddress);
            
            // 记录查询结果
            log.debug("查询IP剩余下载次数成功: ip={}, remaining={}", ipAddress, remaining);
            
            return Result.success(remaining);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("查询IP剩余下载次数失败: ip={}, error={}", ipAddress, e.getMessage(), e);
            throw e;
        }
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
        
        try {
            // 调用服务层检查下载状态
            boolean downloaded = ipDownloadService.hasDownloadedToday(ipAddress, id);
            
            // 记录检查结果
            log.debug("检查资源下载状态完成: resourceId={}, ip={}, downloaded={}", id, ipAddress, downloaded);
            
            return Result.success(downloaded);
            
        } catch (Exception e) {
            // 记录检查失败
            log.error("检查资源下载状态失败: resourceId={}, ip={}, error={}", id, ipAddress, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取客户端真实IP地址
     * 
     * 业务逻辑：
     * 1. 优先从X-Forwarded-For头获取IP（适用于经过代理的情况）
     * 2. 其次从X-Real-IP头获取IP（适用于Nginx等反向代理）
     * 3. 最后从RemoteAddr获取IP（直连情况）
     * 4. 处理多级代理的情况，取第一个IP
     * 
     * @param request HTTP请求对象
     * @return 客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        // 步骤1：尝试从X-Forwarded-For头获取IP
        // 这个头通常由代理服务器添加，包含客户端的真实IP
        String ip = request.getHeader("X-Forwarded-For");
        
        // 步骤2：如果X-Forwarded-For为空，尝试从X-Real-IP获取
        // Nginx等反向代理通常使用这个头传递真实IP
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        
        // 步骤3：如果以上都为空，直接获取RemoteAddr
        // 这是直连情况下的客户端IP
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 步骤4：处理多级代理的情况
        // X-Forwarded-For可能包含多个IP，格式为：client, proxy1, proxy2
        // 取第一个IP作为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
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
        // 获取客户端IP地址
        String ipAddress = getClientIp(request);
        
        // 记录访问请求
        log.debug("记录资源访问: resourceId={}, ip={}", id, ipAddress);
        
        try {
            // 调用服务层记录访问日志
            accessLogService.recordVisit(id, request);
            
            // 记录访问成功（使用DEBUG级别，避免日志过多）
            log.debug("记录资源访问成功: resourceId={}, ip={}", id, ipAddress);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录访问失败
            log.error("记录资源访问失败: resourceId={}, ip={}, error={}", id, ipAddress, e.getMessage(), e);
            throw e;
        }
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
    public Result<Integer> batchPublishResources(@Validated @RequestBody BatchOperationDTO request) {
        log.info("开始批量发布资源: ids={}", request.getIds());
        
        try {
            int updatedCount = resourceService.batchUpdateStatus(request.getIds(), 1);
            
            log.info("批量发布资源成功: 请求数量={}, 实际更新数量={}", request.getIds().size(), updatedCount);
            
            return Result.success(updatedCount);
            
        } catch (Exception e) {
            log.error("批量发布资源失败: ids={}, error={}", request.getIds(), e.getMessage(), e);
            throw e;
        }
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
    public Result<Integer> batchUnpublishResources(@Validated @RequestBody BatchOperationDTO request) {
        log.info("开始批量下架资源: ids={}", request.getIds());
        
        try {
            int updatedCount = resourceService.batchUpdateStatus(request.getIds(), 0);
            
            log.info("批量下架资源成功: 请求数量={}, 实际更新数量={}", request.getIds().size(), updatedCount);
            
            return Result.success(updatedCount);
            
        } catch (Exception e) {
            log.error("批量下架资源失败: ids={}, error={}", request.getIds(), e.getMessage(), e);
            throw e;
        }
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
    public Result<Integer> batchDeleteResources(@Validated @RequestBody BatchOperationDTO request) {
        log.info("开始批量删除资源: ids={}", request.getIds());
        
        try {
            int deletedCount = resourceService.batchDelete(request.getIds());
            
            log.info("批量删除资源成功: 请求数量={}, 实际删除数量={}", request.getIds().size(), deletedCount);
            
            return Result.success(deletedCount);
            
        } catch (Exception e) {
            log.error("批量删除资源失败: ids={}, error={}", request.getIds(), e.getMessage(), e);
            throw e;
        }
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
    public Result<Integer> batchMoveToCategory(@Validated @RequestBody BatchOperationDTO.BatchMoveCategoryRequest request) {
        log.info("开始批量移动资源到分类: ids={}, categoryId={}", request.getIds(), request.getCategoryId());
        
        try {
            int movedCount = resourceService.batchMoveToCategory(request.getIds(), request.getCategoryId());
            
            log.info("批量移动资源到分类成功: 请求数量={}, 实际移动数量={}, categoryId={}", 
                    request.getIds().size(), movedCount, request.getCategoryId());
            
            return Result.success(movedCount);
            
        } catch (Exception e) {
            log.error("批量移动资源到分类失败: ids={}, categoryId={}, error={}", 
                    request.getIds(), request.getCategoryId(), e.getMessage(), e);
            throw e;
        }
    }
}
