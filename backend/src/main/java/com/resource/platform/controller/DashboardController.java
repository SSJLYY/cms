package com.resource.platform.controller;

import com.resource.platform.common.Result;
import com.resource.platform.service.DashboardService;
import com.resource.platform.vo.DashboardMetricsVO;
import com.resource.platform.vo.TrendDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 控制面板控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "控制面板", description = "控制面板数据统计接口")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取核心指标
     */
    @GetMapping("/metrics")
    @Operation(summary = "获取核心指标")
    public Result<DashboardMetricsVO> getMetrics() {
        DashboardMetricsVO metrics = dashboardService.getMetrics();
        return Result.success(metrics);
    }

    /**
     * 获取趋势数据
     */
    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据")
    public Result<TrendDataVO> getTrendData(@RequestParam(defaultValue = "7") Integer days) {
        TrendDataVO trendData = dashboardService.getTrendData(days);
        return Result.success(trendData);
    }

    /**
     * 获取热门资源
     */
    @GetMapping("/hot-resources")
    @Operation(summary = "获取热门资源")
    public Result<Object> getHotResources(@RequestParam(defaultValue = "10") Integer limit) {
        Object hotResources = dashboardService.getHotResources(limit);
        return Result.success(hotResources);
    }

    /**
     * 获取最新资源
     */
    @GetMapping("/latest-resources")
    @Operation(summary = "获取最新资源")
    public Result<Object> getLatestResources(@RequestParam(defaultValue = "10") Integer limit) {
        Object latestResources = dashboardService.getLatestResources(limit);
        return Result.success(latestResources);
    }

    /**
     * 获取待处理事项
     */
    @GetMapping("/pending-tasks")
    @Operation(summary = "获取待处理事项")
    public Result<Map<String, Long>> getPendingTasks() {
        Map<String, Long> tasks = dashboardService.getPendingTasks();
        return Result.success(tasks);
    }

    /**
     * 获取系统状态
     */
    @GetMapping("/system-status")
    @Operation(summary = "获取系统状态")
    public Result<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = dashboardService.getSystemStatus();
        return Result.success(status);
    }
}
