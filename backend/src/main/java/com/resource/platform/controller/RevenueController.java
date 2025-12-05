package com.resource.platform.controller;

import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.entity.Revenue;
import com.resource.platform.service.RevenueService;
import com.resource.platform.vo.RevenueOverviewVO;
import com.resource.platform.vo.RevenueTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "收益管理")
@RestController
@RequestMapping("/api/revenue")
public class RevenueController {
    
    @Autowired
    private RevenueService revenueService;
    
    @Operation(summary = "获取收益概览")
    @GetMapping("/overview")
    public Result<RevenueOverviewVO> getOverview(@RequestParam(defaultValue = "today") String period) {
        return Result.success(revenueService.getOverview(period));
    }
    
    @Operation(summary = "按类型获取收益统计")
    @GetMapping("/by-type")
    public Result<List<RevenueTypeVO>> getRevenueByType(@RequestParam(defaultValue = "today") String period) {
        return Result.success(revenueService.getRevenueByType(period));
    }
    
    @Operation(summary = "获取收益明细列表")
    @GetMapping("/list")
    public Result<PageResult<Revenue>> getRevenueList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String revenueType,
            @RequestParam(required = false) String status) {
        return Result.success(revenueService.getRevenueList(pageNum, pageSize, period, revenueType, status));
    }
    
    @Operation(summary = "删除收益记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRevenue(@PathVariable Long id) {
        boolean success = revenueService.deleteRevenue(id);
        return success ? Result.success() : Result.error("删除失败");
    }
    
    @Operation(summary = "批量删除收益记录")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteRevenue(@RequestBody List<Long> ids) {
        boolean success = revenueService.batchDeleteRevenue(ids);
        return success ? Result.success() : Result.error("批量删除失败");
    }
}
