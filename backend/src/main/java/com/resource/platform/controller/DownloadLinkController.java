package com.resource.platform.controller;

import com.resource.platform.common.Result;
import com.resource.platform.dto.DownloadLinkDTO;
import com.resource.platform.service.DownloadLinkService;
import com.resource.platform.vo.DownloadLinkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 下载链接管理控制器
 * 
 * 功能说明：
 * - 提供资源下载链接的查询接口
 * - 提供下载链接的创建和删除管理功能
 * - 支持按资源ID查询相关的下载链接
 * - 管理下载链接的排序和状态
 * - 记录下载链接的访问和管理日志
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "下载链接管理")
@RestController
@RequestMapping("/api/download-links")
public class DownloadLinkController {

    @Autowired
    private DownloadLinkService downloadLinkService;

    /**
     * 获取资源的下载链接
     * 
     * 业务逻辑：
     * 1. 验证资源ID的有效性
     * 2. 查询指定资源的所有下载链接
     * 3. 按排序字段排序返回结果
     * 4. 记录链接访问日志
     * 
     * @param resourceId 资源ID
     * @return 下载链接列表
     */
    @Operation(summary = "获取资源的下载链接")
    @GetMapping("/resource/{resourceId}")
    public Result<List<DownloadLinkVO>> getDownloadLinks(@PathVariable Long resourceId) {
        // 记录请求开始
        log.info("开始获取资源下载链接: resourceId={}", resourceId);
        
        try {
            // 步骤1：验证参数
            // 检查资源ID是否有效
            if (resourceId == null || resourceId <= 0) {
                log.warn("资源ID无效: resourceId={}", resourceId);
                throw new IllegalArgumentException("资源ID无效");
            }
            
            // 步骤2：调用Service层查询下载链接
            List<DownloadLinkVO> links = downloadLinkService.getDownloadLinks(resourceId);
            
            // 记录成功结果
            log.info("资源下载链接获取成功: resourceId={}, linkCount={}", 
                resourceId, links.size());
            
            return Result.success(links);
            
        } catch (Exception e) {
            // 记录异常
            log.error("资源下载链接获取失败: resourceId={}, error={}", 
                resourceId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 添加下载链接
     * 
     * 业务逻辑：
     * 1. 验证下载链接数据的完整性
     * 2. 检查链接URL的有效性
     * 3. 设置默认的排序和状态
     * 4. 保存下载链接到数据库
     * 5. 记录创建操作日志
     * 
     * @param dto 下载链接数据传输对象
     * @return 创建成功的下载链接信息
     * @throws ValidationException 当数据验证失败时抛出
     */
    @Operation(summary = "添加下载链接")
    @PostMapping("/admin/create")
    public Result<DownloadLinkVO> createDownloadLink(@Validated @RequestBody DownloadLinkDTO dto) {
        // 记录请求开始
        log.info("开始创建下载链接: resourceId={}, linkType={}, title={}", 
            dto.getResourceId(), dto.getLinkType(), dto.getTitle());
        
        try {
            // 步骤1：验证参数
            // @Validated注解已经进行了基本验证，这里进行业务验证
            if (dto.getResourceId() == null || dto.getResourceId() <= 0) {
                log.warn("资源ID无效: resourceId={}", dto.getResourceId());
                throw new IllegalArgumentException("资源ID无效");
            }
            
            if (dto.getDownloadUrl() == null || dto.getDownloadUrl().trim().isEmpty()) {
                log.warn("下载链接URL为空: resourceId={}", dto.getResourceId());
                throw new IllegalArgumentException("下载链接URL不能为空");
            }
            
            // 步骤2：调用Service层创建下载链接
            DownloadLinkVO linkVO = downloadLinkService.createDownloadLink(dto);
            
            // 记录成功结果
            log.info("下载链接创建成功: id={}, resourceId={}, title={}, url={}", 
                linkVO.getId(), linkVO.getResourceId(), linkVO.getTitle(), 
                linkVO.getDownloadUrl());
            
            return Result.success(linkVO);
            
        } catch (Exception e) {
            // 记录异常
            log.error("下载链接创建失败: resourceId={}, title={}, error={}", 
                dto.getResourceId(), dto.getTitle(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除下载链接
     * 
     * 业务逻辑：
     * 1. 验证下载链接ID的有效性
     * 2. 检查下载链接是否存在
     * 3. 执行删除操作
     * 4. 记录删除操作日志
     * 
     * @param id 下载链接ID
     * @return 操作结果
     * @throws ResourceNotFoundException 当下载链接不存在时抛出
     */
    @Operation(summary = "删除下载链接")
    @DeleteMapping("/admin/delete/{id}")
    public Result<Void> deleteDownloadLink(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始删除下载链接: id={}", id);
        
        try {
            // 步骤1：验证参数
            // 检查下载链接ID是否有效
            if (id == null || id <= 0) {
                log.warn("下载链接ID无效: id={}", id);
                throw new IllegalArgumentException("下载链接ID无效");
            }
            
            // 步骤2：调用Service层删除下载链接
            // Service层会检查链接是否存在
            downloadLinkService.deleteDownloadLink(id);
            
            // 记录成功结果
            log.info("下载链接删除成功: id={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("下载链接删除失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
}
