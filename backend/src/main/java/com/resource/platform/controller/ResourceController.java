package com.resource.platform.controller;

import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.ResourceDTO;
import com.resource.platform.dto.ResourceQueryDTO;
import com.resource.platform.service.ResourceService;
import com.resource.platform.vo.ResourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "资源管理")
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private com.resource.platform.service.AccessLogService accessLogService;

    @Operation(summary = "获取资源列表（客户前台）")
    @GetMapping("/public/list")
    public Result<List<ResourceVO>> getPublicResourceList() {
        List<ResourceVO> resources = resourceService.getPublishedResources();
        return Result.success(resources);
    }

    @Operation(summary = "获取资源列表（管理后台，支持搜索和筛选）")
    @GetMapping("/admin/list")
    public Result<PageResult<ResourceVO>> getAdminResourceList(ResourceQueryDTO query) {
        PageResult<ResourceVO> result = resourceService.queryResources(query);
        return Result.success(result);
    }

    @Operation(summary = "创建资源")
    @PostMapping("/admin/create")
    public Result<ResourceVO> createResource(@Validated @RequestBody ResourceDTO dto) {
        ResourceVO resourceVO = resourceService.createResource(dto);
        return Result.success(resourceVO);
    }

    @Operation(summary = "更新资源")
    @PutMapping("/admin/update/{id}")
    public Result<ResourceVO> updateResource(@PathVariable Long id, @Validated @RequestBody ResourceDTO dto) {
        ResourceVO resourceVO = resourceService.updateResource(id, dto);
        return Result.success(resourceVO);
    }

    @Operation(summary = "删除资源（软删除）")
    @DeleteMapping("/admin/delete/{id}")
    public Result<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return Result.success();
    }

    @Operation(summary = "切换资源状态")
    @PutMapping("/admin/toggle-status/{id}")
    public Result<Void> toggleResourceStatus(@PathVariable Long id) {
        resourceService.toggleStatus(id);
        return Result.success();
    }

    @Operation(summary = "记录下载次数")
    @PostMapping("/public/download/{id}")
    public Result<Void> recordDownload(@PathVariable Long id, HttpServletRequest request) {
        resourceService.recordDownload(id);
        accessLogService.recordDownload(id, request);
        return Result.success();
    }
    
    @Operation(summary = "记录访问")
    @PostMapping("/public/visit/{id}")
    public Result<Void> recordVisit(@PathVariable Long id, HttpServletRequest request) {
        accessLogService.recordVisit(id, request);
        return Result.success();
    }
}
