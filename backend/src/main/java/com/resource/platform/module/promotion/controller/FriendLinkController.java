package com.resource.platform.module.promotion.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.BizErrorCode;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.common.Result;
import com.resource.platform.module.promotion.dto.FriendLinkDTO;
import com.resource.platform.module.promotion.dto.FriendLinkQueryDTO;
import com.resource.platform.module.promotion.service.FriendLinkService;
import com.resource.platform.module.promotion.vo.FriendLinkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 友情链接管理控制器
 *
 * @author 系统
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/friendlinks")
@RequiredArgsConstructor
@Tag(name = "友情链接管理", description = "友情链接管理相关接口")
public class FriendLinkController {

    private final FriendLinkService friendLinkService;

    @GetMapping("/page")
    @Operation(summary = "分页查询友情链接")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<FriendLinkVO>> queryPage(FriendLinkQueryDTO queryDTO) {
        if (queryDTO.getPage() == null || queryDTO.getPage() < 1) {
            queryDTO.setPage(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }

        PageResult<FriendLinkVO> result = friendLinkService.queryPage(queryDTO);
        return Result.success(result);
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的友情链接")
    public Result<List<FriendLinkVO>> listEnabled() {
        List<FriendLinkVO> list = friendLinkService.listEnabled();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取友情链接")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<FriendLinkVO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.warn("友情链接ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "友情链接ID无效");
        }

        FriendLinkVO vo = friendLinkService.getById(id);
        return Result.success(vo);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "友情链接管理", type = "创建")
    @Operation(summary = "创建友情链接")
    public Result<Long> create(@Validated @RequestBody FriendLinkDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            log.warn("友情链接名称为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "友情链接名称不能为空");
        }

        if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
            log.warn("友情链接URL为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "友情链接URL不能为空");
        }

        Long id = friendLinkService.create(dto);
        return Result.success(id);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "友情链接管理", type = "更新")
    @Operation(summary = "更新友情链接")
    public Result<Void> update(@Validated @RequestBody FriendLinkDTO dto) {
        if (dto.getId() == null || dto.getId() <= 0) {
            log.warn("友情链接ID无效: id={}", dto.getId());
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "友情链接ID无效");
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            log.warn("友情链接名称为空: id={}", dto.getId());
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "友情链接名称不能为空");
        }

        if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
            log.warn("友情链接URL为空: id={}", dto.getId());
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "友情链接URL不能为空");
        }

        friendLinkService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "友情链接管理", type = "删除")
    @Operation(summary = "删除友情链接")
    public Result<Void> delete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.warn("友情链接ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "友情链接ID无效");
        }

        friendLinkService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "友情链接管理", type = "批量删除")
    @Operation(summary = "批量删除友情链接")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("批量删除友情链接ID列表为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "删除ID列表不能为空");
        }

        if (ids.size() > 100) {
            log.warn("批量删除友情链接数量超限: count={}", ids.size());
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "批量删除最多支持100条");
        }

        for (Long id : ids) {
            if (id == null || id <= 0) {
                log.warn("批量删除包含无效ID: id={}", id);
                throw new BusinessException(BizErrorCode.PARAM_ERROR, "包含无效的友情链接ID: " + id);
            }
        }

        friendLinkService.batchDelete(ids);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "友情链接管理", type = "更新状态")
    @Operation(summary = "更新友情链接状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (id == null || id <= 0) {
            log.warn("友情链接ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "友情链接ID无效");
        }

        if (status == null || (status != 0 && status != 1)) {
            log.warn("友情链接状态值无效: id={}, status={}", id, status);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "状态值无效，只能是0（禁用）或1（启用）");
        }

        friendLinkService.updateStatus(id, status);
        return Result.success();
    }
}
