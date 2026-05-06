package com.resource.platform.module.revenue.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.BizErrorCode;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.common.Result;
import com.resource.platform.module.revenue.entity.Revenue;
import com.resource.platform.module.revenue.service.RevenueService;
import com.resource.platform.module.revenue.vo.RevenueOverviewVO;
import com.resource.platform.module.revenue.vo.RevenueTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收益管理控制器
 *
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "收益管理")
@RestController
@RequestMapping("/api/revenue")
public class RevenueController {

    @Autowired
    private RevenueService revenueService;

    @Operation(summary = "获取收益概览")
    @GetMapping("/overview")
    public Result<RevenueOverviewVO> getOverview(@RequestParam(defaultValue = "today") String period) {
        if (period == null || period.trim().isEmpty()) {
            period = "today";
        }

        RevenueOverviewVO overview = revenueService.getOverview(period);
        return Result.success(overview);
    }

    @Operation(summary = "按类型获取收益统计")
    @GetMapping("/by-type")
    public Result<List<RevenueTypeVO>> getRevenueByType(@RequestParam(defaultValue = "today") String period) {
        if (period == null || period.trim().isEmpty()) {
            period = "today";
        }

        List<RevenueTypeVO> revenueTypes = revenueService.getRevenueByType(period);
        return Result.success(revenueTypes);
    }

    @Operation(summary = "获取收益明细列表")
    @GetMapping("/list")
    public Result<PageResult<Revenue>> getRevenueList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String revenueType,
            @RequestParam(required = false) String status) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        PageResult<Revenue> result = revenueService.getRevenueList(
            pageNum, pageSize, period, revenueType, status);
        return Result.success(result);
    }

    @Operation(summary = "删除收益记录")
    @DeleteMapping("/{id}")
    @OperationLog(module = "收益管理", type = "删除", description = "删除收益记录")
    public Result<Void> deleteRevenue(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.warn("收益记录ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "收益记录ID无效");
        }

        boolean success = revenueService.deleteRevenue(id);
        if (!success) {
            log.warn("收益记录删除失败: id={}", id);
            return Result.error("删除失败");
        }
        return Result.success();
    }

    @Operation(summary = "批量删除收益记录")
    @DeleteMapping("/batch")
    @OperationLog(module = "收益管理", type = "批量删除", description = "批量删除收益记录")
    public Result<Void> batchDeleteRevenue(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("批量删除收益记录ID列表为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "删除ID列表不能为空");
        }

        for (Long id : ids) {
            if (id == null || id <= 0) {
                log.warn("批量删除包含无效ID: id={}", id);
                throw new BusinessException(BizErrorCode.PARAM_ERROR, "包含无效的收益记录ID: " + id);
            }
        }

        boolean success = revenueService.batchDeleteRevenue(ids);
        if (!success) {
            log.warn("收益记录批量删除失败: count={}", ids.size());
            return Result.error("批量删除失败");
        }
        return Result.success();
    }
}
