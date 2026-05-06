package com.resource.platform.module.resource.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.module.resource.dto.BatchOperationDTO;
import com.resource.platform.module.resource.dto.ResourceDTO;
import com.resource.platform.module.resource.dto.ResourceQueryDTO;
import com.resource.platform.module.resource.service.IpDownloadService;
import com.resource.platform.module.resource.service.ResourceService;
import com.resource.platform.module.resource.vo.ResourceVO;
import com.resource.platform.module.system.service.AccessLogService;
import com.resource.platform.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Tag(name = "资源管理")
@RestController
@RequestMapping("/api/resources")
@Validated
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private AccessLogService accessLogService;

    @Autowired
    private IpDownloadService ipDownloadService;

    @Operation(summary = "获取资源列表（前台）")
    @GetMapping("/public/list")
    public Result<List<ResourceVO>> getPublicResourceList() {
        return Result.success(resourceService.getPublishedResources());
    }

    @Operation(summary = "获取资源详情（前台）")
    @GetMapping("/public/{id}")
    public Result<ResourceVO> getPublicResourceDetail(@PathVariable Long id) {
        return Result.success(resourceService.getPublishedResourceDetail(id));
    }

    @Operation(summary = "获取资源列表（后台）")
    @GetMapping("/admin/list")
    public Result<PageResult<ResourceVO>> getAdminResourceList(ResourceQueryDTO query) {
        return Result.success(resourceService.queryResources(query));
    }

    @Operation(summary = "创建资源")
    @PostMapping("/admin/create")
    @OperationLog(module = "资源管理", type = "创建", description = "创建资源")
    public Result<ResourceVO> createResource(@Validated @RequestBody ResourceDTO dto) {
        return Result.success(resourceService.createResource(dto));
    }

    @Operation(summary = "更新资源")
    @PutMapping("/admin/update/{id}")
    @OperationLog(module = "资源管理", type = "更新", description = "更新资源")
    public Result<ResourceVO> updateResource(@PathVariable Long id, @Validated @RequestBody ResourceDTO dto) {
        return Result.success(resourceService.updateResource(id, dto));
    }

    @Operation(summary = "删除资源")
    @DeleteMapping("/admin/delete/{id}")
    @OperationLog(module = "资源管理", type = "删除", description = "删除资源")
    public Result<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return Result.success();
    }

    @Operation(summary = "切换资源状态")
    @PutMapping("/admin/toggle-status/{id}")
    @OperationLog(module = "资源管理", type = "状态切换", description = "切换资源状态")
    public Result<Void> toggleResourceStatus(@PathVariable Long id) {
        resourceService.toggleStatus(id);
        return Result.success();
    }

    @Operation(summary = "记录下载次数")
    @PostMapping("/public/download/{id}")
    public Result<Void> recordDownload(@PathVariable Long id, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        IpDownloadService.DownloadDecision decision = ipDownloadService.recordDownload(ipAddress, id);
        if (decision == IpDownloadService.DownloadDecision.ALREADY_DOWNLOADED) {
            log.warn("重复下载: resourceId={}, ip={}", id, ipAddress);
            return Result.error(208, "您今日已下载过此资源，重复下载不会计数");
        }
        if (decision == IpDownloadService.DownloadDecision.LIMIT_REACHED) {
            log.warn("下载次数超限: resourceId={}, ip={}", id, ipAddress);
            return Result.error(429, "今日下载次数已达上限，请明天再试");
        }
        resourceService.recordDownload(id);
        accessLogService.recordDownload(id, request);
        return Result.success();
    }

    @Operation(summary = "获取IP今日剩余下载次数")
    @GetMapping("/public/remaining-downloads")
    public Result<Integer> getRemainingDownloads(HttpServletRequest request) {
        return Result.success(ipDownloadService.getRemainingDownloads(getClientIp(request)));
    }

    @Operation(summary = "检查是否已下载")
    @GetMapping("/public/check-downloaded/{id}")
    public Result<Boolean> checkDownloaded(@PathVariable Long id, HttpServletRequest request) {
        return Result.success(ipDownloadService.hasDownloadedToday(getClientIp(request), id));
    }

    @Operation(summary = "记录访问")
    @PostMapping("/public/visit/{id}")
    public Result<Void> recordVisit(@PathVariable Long id, HttpServletRequest request) {
        accessLogService.recordVisit(id, request);
        return Result.success();
    }

    @Operation(summary = "批量发布资源")
    @PutMapping("/admin/batch-publish")
    @OperationLog(module = "资源管理", type = "批量发布", description = "批量发布资源")
    public Result<Integer> batchPublishResources(@Validated @RequestBody BatchOperationDTO request) {
        return Result.success(resourceService.batchUpdateStatus(request.getIds(), 1));
    }

    @Operation(summary = "批量下架资源")
    @PutMapping("/admin/batch-unpublish")
    @OperationLog(module = "资源管理", type = "批量下架", description = "批量下架资源")
    public Result<Integer> batchUnpublishResources(@Validated @RequestBody BatchOperationDTO request) {
        return Result.success(resourceService.batchUpdateStatus(request.getIds(), 0));
    }

    @Operation(summary = "批量删除资源")
    @DeleteMapping("/admin/batch-delete")
    @OperationLog(module = "资源管理", type = "批量删除", description = "批量删除资源")
    public Result<Integer> batchDeleteResources(@Validated @RequestBody BatchOperationDTO request) {
        return Result.success(resourceService.batchDelete(request.getIds()));
    }

    @Operation(summary = "批量移动资源分类")
    @PutMapping("/admin/batch-move-category")
    @OperationLog(module = "资源管理", type = "批量移动", description = "批量移动资源分类")
    public Result<Integer> batchMoveToCategory(@Validated @RequestBody BatchOperationDTO.BatchMoveCategoryRequest request) {
        return Result.success(resourceService.batchMoveToCategory(request.getIds(), request.getCategoryId()));
    }

    private String getClientIp(HttpServletRequest request) {
        return IpUtil.getClientIp(request);
    }
}
