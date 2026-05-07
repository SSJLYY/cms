package com.resource.platform.module.promotion.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.BizErrorCode;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.common.Result;
import com.resource.platform.module.promotion.entity.LinkType;
import com.resource.platform.module.promotion.service.LinkTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 链接类型管理控制器
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

    @GetMapping("/public/list")
    @Operation(summary = "获取所有启用的链接类型")
    public Result<List<LinkType>> listEnabled() {
        List<LinkType> list = linkTypeService.listEnabled();
        return Result.success(list);
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有链接类型")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<LinkType>> listAll() {
        List<LinkType> list = linkTypeService.listAll();
        return Result.success(list);
    }

    @PostMapping
    @Operation(summary = "添加链接类型")
    @OperationLog(module = "链接类型管理", type = "创建", description = "创建链接类型")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> add(@Validated @RequestBody LinkType linkType) {
        if (linkType.getName() == null || linkType.getName().trim().isEmpty()) {
            log.warn("链接类型名称为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "链接类型名称不能为空");
        }

        linkTypeService.save(linkType);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新链接类型")
    @OperationLog(module = "链接类型管理", type = "更新", description = "更新链接类型")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody LinkType linkType) {
        if (id == null || id <= 0) {
            log.warn("链接类型ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "链接类型ID无效");
        }

        if (linkType.getName() == null || linkType.getName().trim().isEmpty()) {
            log.warn("链接类型名称为空: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "链接类型名称不能为空");
        }

        linkType.setId(id);
        linkTypeService.updateById(linkType);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除链接类型")
    @OperationLog(module = "链接类型管理", type = "删除", description = "删除链接类型")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.warn("链接类型ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "链接类型ID无效");
        }

        linkTypeService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量删除链接类型")
    @OperationLog(module = "链接类型管理", type = "批量删除", description = "批量删除链接类型")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("批量删除链接类型ID列表为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "删除ID列表不能为空");
        }

        if (ids.size() > 100) {
            log.warn("批量删除链接类型数量超限: count={}", ids.size());
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "批量删除最多支持100条");
        }

        for (Long id : ids) {
            if (id == null || id <= 0) {
                log.warn("批量删除包含无效ID: id={}", id);
                throw new BusinessException(BizErrorCode.PARAM_ERROR, "包含无效的链接类型ID: " + id);
            }
        }

        linkTypeService.batchRemoveByIds(ids);
        return Result.success();
    }
}
