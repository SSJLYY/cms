package com.resource.platform.controller;

import com.resource.platform.common.Result;
import com.resource.platform.entity.LinkType;
import com.resource.platform.service.LinkTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网盘类型配置Controller
 */
@RestController
@RequestMapping("/api/link-types")
@RequiredArgsConstructor
public class LinkTypeController {
    
    private final LinkTypeService linkTypeService;
    
    /**
     * 获取所有启用的网盘类型（公开接口）
     */
    @GetMapping("/public/list")
    public Result<List<LinkType>> listEnabled() {
        return Result.success(linkTypeService.listEnabled());
    }
    
    /**
     * 获取所有网盘类型（管理后台）
     */
    @GetMapping("/list")
    public Result<List<LinkType>> listAll() {
        return Result.success(linkTypeService.listAll());
    }
    
    /**
     * 添加网盘类型
     */
    @PostMapping
    public Result<Void> add(@RequestBody LinkType linkType) {
        linkTypeService.save(linkType);
        return Result.success();
    }
    
    /**
     * 更新网盘类型
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody LinkType linkType) {
        linkType.setId(id);
        linkTypeService.updateById(linkType);
        return Result.success();
    }
    
    /**
     * 删除网盘类型
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        linkTypeService.removeById(id);
        return Result.success();
    }
    
    /**
     * 批量删除网盘类型
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        linkTypeService.removeByIds(ids);
        return Result.success();
    }
}
