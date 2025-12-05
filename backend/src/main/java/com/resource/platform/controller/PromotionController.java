package com.resource.platform.controller;

import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.AdvertisementDTO;
import com.resource.platform.entity.Advertisement;
import com.resource.platform.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        return Result.success(promotionService.getAdvertisementList(page, pageSize, position));
    }
    
    @Operation(summary = "获取广告详情")
    @GetMapping("/{id}")
    public Result<Advertisement> getAdvertisementById(@PathVariable Long id) {
        return Result.success(promotionService.getAdvertisementById(id));
    }
    
    @Operation(summary = "创建广告")
    @PostMapping
    public Result<Void> createAdvertisement(@RequestBody AdvertisementDTO dto) {
        promotionService.createAdvertisement(dto);
        return Result.success();
    }
    
    @Operation(summary = "更新广告")
    @PutMapping("/{id}")
    public Result<Void> updateAdvertisement(@PathVariable Long id, @RequestBody AdvertisementDTO dto) {
        promotionService.updateAdvertisement(id, dto);
        return Result.success();
    }
    
    @Operation(summary = "删除广告")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAdvertisement(@PathVariable Long id) {
        promotionService.deleteAdvertisement(id);
        return Result.success();
    }
    
    @Operation(summary = "更新广告状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        promotionService.updateStatus(id, status);
        return Result.success();
    }
    
    @Operation(summary = "更新广告排序")
    @PutMapping("/{id}/sort")
    public Result<Void> updateSortOrder(@PathVariable Long id, @RequestParam Integer sortOrder) {
        promotionService.updateSortOrder(id, sortOrder);
        return Result.success();
    }
    
    @Operation(summary = "记录点击")
    @PostMapping("/{id}/click")
    public Result<Void> recordClick(@PathVariable Long id) {
        promotionService.recordClick(id);
        return Result.success();
    }
    
    @Operation(summary = "获取广告位置选项")
    @GetMapping("/positions")
    public Result<List<Map<String, String>>> getPositionOptions() {
        return Result.success(promotionService.getPositionOptions());
    }
    
    @Operation(summary = "获取用户端广告")
    @GetMapping("/active")
    public Result<List<Advertisement>> getActiveAdvertisements(@RequestParam String position) {
        return Result.success(promotionService.getActiveAdvertisements(position));
    }
}
