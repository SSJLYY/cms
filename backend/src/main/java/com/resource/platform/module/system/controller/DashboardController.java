package com.resource.platform.module.system.controller;

import com.resource.platform.common.Result;
import com.resource.platform.module.system.service.DashboardService;
import com.resource.platform.module.system.vo.DashboardMetricsVO;
import com.resource.platform.module.system.vo.TrendDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 控制面板控制器
 * 
 * 功能说明：
 * - 提供仪表盘核心指标接口
 * - 提供趋势数据分析接口
 * - 提供热门资源和最新资源接口
 * - 提供待处理事项统计接口
 * - 提供系统状态监控接口
 * 
 * @author 系统
 * @since 1.0
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
     * 
     * 业务逻辑：
     * 1. 统计总资源数和今日新增资源数
     * 2. 统计总下载量和今日下载量
     * 3. 统计总用户数和今日新增用户数
     * 4. 统计总分类数和待审核资源数
     * 5. 返回核心指标数据
     * 
     * @return 仪表盘核心指标数据
     */
    @GetMapping("/metrics")
    @Operation(summary = "获取核心指标")
    public Result<DashboardMetricsVO> getMetrics() {
        DashboardMetricsVO metrics = dashboardService.getMetrics();
        return Result.success(metrics);
    }

    /**
     * 获取趋势数据
     * 
     * 业务逻辑：
     * 1. 根据指定天数生成日期列表
     * 2. 统计每日新增资源数量
     * 3. 统计每日下载量
     * 4. 统计每日新增用户数
     * 5. 返回趋势数据
     * 
     * @param days 统计天数，默认7天
     * @return 趋势数据
     */
    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据")
    public Result<TrendDataVO> getTrendData(@RequestParam(defaultValue = "7") Integer days) {
        if (days == null || days < 1 || days > 365) {
            log.warn("趋势数据统计天数参数无效: days={}", days);
            days = 7;
        }
        TrendDataVO trendData = dashboardService.getTrendData(days);
        return Result.success(trendData);
    }

    /**
     * 获取热门资源
     * 
     * 业务逻辑：
     * 1. 查询已发布且未删除的资源
     * 2. 按下载量倒序排列
     * 3. 限制返回数量
     * 4. 返回热门资源列表
     * 
     * @param limit 返回数量限制，默认10条
     * @return 热门资源列表
     */
    @GetMapping("/hot-resources")
    @Operation(summary = "获取热门资源")
    public Result<Object> getHotResources(@RequestParam(defaultValue = "10") Integer limit) {
        if (limit == null || limit < 1) {
            log.warn("热门资源数量参数无效: limit={}", limit);
            limit = 10;
        }
        Object hotResources = dashboardService.getHotResources(limit);
        return Result.success(hotResources);
    }

    /**
     * 获取最新资源
     * 
     * 业务逻辑：
     * 1. 查询已发布且未删除的资源
     * 2. 按创建时间倒序排列
     * 3. 限制返回数量
     * 4. 返回最新资源列表
     * 
     * @param limit 返回数量限制，默认10条
     * @return 最新资源列表
     */
    @GetMapping("/latest-resources")
    @Operation(summary = "获取最新资源")
    public Result<Object> getLatestResources(@RequestParam(defaultValue = "10") Integer limit) {
        if (limit == null || limit < 1) {
            log.warn("最新资源数量参数无效: limit={}", limit);
            limit = 10;
        }
        Object latestResources = dashboardService.getLatestResources(limit);
        return Result.success(latestResources);
    }

    /**
     * 获取待处理事项
     * 
     * 业务逻辑：
     * 1. 统计待审核资源数量
     * 2. 统计待处理反馈数量
     * 3. 统计待回复反馈数量
     * 4. 返回待处理事项统计
     * 
     * @return 待处理事项统计数据
     */
    @GetMapping("/pending-tasks")
    @Operation(summary = "获取待处理事项")
    public Result<Map<String, Long>> getPendingTasks() {
        Map<String, Long> tasks = dashboardService.getPendingTasks();
        return Result.success(tasks);
    }

    /**
     * 获取系统状态
     * 
     * 业务逻辑：
     * 1. 获取CPU使用率
     * 2. 获取内存使用率（JVM）
     * 3. 获取磁盘使用率
     * 4. 返回系统状态信息
     * 
     * @return 系统状态信息
     */
    @GetMapping("/system-status")
    @Operation(summary = "获取系统状态")
    public Result<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = dashboardService.getSystemStatus();
        return Result.success(status);
    }
}
