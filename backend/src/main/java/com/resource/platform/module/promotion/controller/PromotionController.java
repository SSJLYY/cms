package com.resource.platform.module.promotion.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.BizErrorCode;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.common.Result;
import com.resource.platform.module.promotion.dto.AdvertisementDTO;
import com.resource.platform.module.promotion.entity.Advertisement;
import com.resource.platform.module.promotion.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 推广管理控制器
 *
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "推广管理")
@RestController
@RequestMapping("/api/promotion")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @Operation(summary = "获取广告列表")
    @GetMapping("/list")
    public Result<PageResult<Advertisement>> getAdvertisementList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String position) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        PageResult<Advertisement> result = promotionService.getAdvertisementList(page, pageSize, position);
        return Result.success(result);
    }

    @Operation(summary = "获取广告详情")
    @GetMapping("/{id}")
    public Result<Advertisement> getAdvertisementById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告ID无效");
        }

        Advertisement advertisement = promotionService.getAdvertisementById(id);
        return Result.success(advertisement);
    }

    @Operation(summary = "创建广告")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "推广管理", type = "创建", description = "创建广告")
    public Result<Void> createAdvertisement(@Validated @RequestBody AdvertisementDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            log.warn("广告标题为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告标题不能为空");
        }

        if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
            log.warn("广告位置为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告位置不能为空");
        }

        promotionService.createAdvertisement(dto);
        return Result.success();
    }

    @Operation(summary = "更新广告")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "推广管理", type = "更新", description = "更新广告")
    public Result<Void> updateAdvertisement(@PathVariable Long id, @Validated @RequestBody AdvertisementDTO dto) {
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告ID无效");
        }

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            log.warn("广告标题为空: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告标题不能为空");
        }

        if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
            log.warn("广告位置为空: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告位置不能为空");
        }

        promotionService.updateAdvertisement(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除广告")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "推广管理", type = "删除", description = "删除广告")
    public Result<Void> deleteAdvertisement(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告ID无效");
        }

        promotionService.deleteAdvertisement(id);
        return Result.success();
    }

    @Operation(summary = "更新广告状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "推广管理", type = "更新状态", description = "更新广告状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告ID无效");
        }

        if (status == null || (status != 0 && status != 1)) {
            log.warn("广告状态值无效: id={}, status={}", id, status);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "状态值无效，只能是0（禁用）或1（启用）");
        }

        promotionService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "更新广告排序")
    @PutMapping("/{id}/sort")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "推广管理", type = "更新排序", description = "更新广告排序")
    public Result<Void> updateSortOrder(@PathVariable Long id, @RequestParam Integer sortOrder) {
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告ID无效");
        }

        if (sortOrder == null || sortOrder < 0) {
            log.warn("广告排序值无效: id={}, sortOrder={}", id, sortOrder);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "排序值无效，必须大于等于0");
        }

        promotionService.updateSortOrder(id, sortOrder);
        return Result.success();
    }

    @Operation(summary = "记录点击")
    @PostMapping("/{id}/click")
    public Result<Void> recordClick(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告ID无效");
        }

        promotionService.recordClick(id);
        return Result.success();
    }

    @Operation(summary = "获取广告位置选项")
    @GetMapping("/positions")
    public Result<List<Map<String, String>>> getPositionOptions() {
        List<Map<String, String>> options = promotionService.getPositionOptions();
        return Result.success(options);
    }

    @Operation(summary = "获取用户端广告")
    @GetMapping("/active")
    public Result<List<Advertisement>> getActiveAdvertisements(@RequestParam String position) {
        if (position == null || position.trim().isEmpty()) {
            log.warn("广告位置为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "广告位置不能为空");
        }

        List<Advertisement> advertisements = promotionService.getActiveAdvertisements(position);
        return Result.success(advertisements);
    }
}
