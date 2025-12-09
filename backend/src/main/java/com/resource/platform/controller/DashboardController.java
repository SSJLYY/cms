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
        // 记录请求开始
        log.info("开始获取仪表盘核心指标");
        
        try {
            // 调用服务层获取指标
            DashboardMetricsVO metrics = dashboardService.getMetrics();
            
            // 记录获取成功
            log.info("获取仪表盘核心指标成功: totalResources={}, todayResources={}, totalDownloads={}, todayDownloads={}", 
                metrics.getTotalResources(), metrics.getTodayResources(), 
                metrics.getTotalDownloads(), metrics.getTodayDownloads());
            
            return Result.success(metrics);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取仪表盘核心指标失败: error={}", e.getMessage(), e);
            throw e;
        }
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
        // 记录请求开始
        log.info("开始获取趋势数据: days={}", days);
        
        try {
            // 调用服务层获取趋势数据
            TrendDataVO trendData = dashboardService.getTrendData(days);
            
            // 记录获取成功
            log.info("获取趋势数据成功: days={}, dateCount={}", days, trendData.getDates().size());
            
            return Result.success(trendData);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取趋势数据失败: days={}, error={}", days, e.getMessage(), e);
            throw e;
        }
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
        // 记录请求开始
        log.info("开始获取热门资源: limit={}", limit);
        
        try {
            // 调用服务层获取热门资源
            Object hotResources = dashboardService.getHotResources(limit);
            
            // 记录获取成功
            log.info("获取热门资源成功: limit={}", limit);
            
            return Result.success(hotResources);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取热门资源失败: limit={}, error={}", limit, e.getMessage(), e);
            throw e;
        }
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
        // 记录请求开始
        log.info("开始获取最新资源: limit={}", limit);
        
        try {
            // 调用服务层获取最新资源
            Object latestResources = dashboardService.getLatestResources(limit);
            
            // 记录获取成功
            log.info("获取最新资源成功: limit={}", limit);
            
            return Result.success(latestResources);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取最新资源失败: limit={}, error={}", limit, e.getMessage(), e);
            throw e;
        }
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
        // 记录请求开始
        log.info("开始获取待处理事项");
        
        try {
            // 调用服务层获取待处理事项
            Map<String, Long> tasks = dashboardService.getPendingTasks();
            
            // 记录获取成功
            log.info("获取待处理事项成功: pendingResources={}, pendingFeedback={}, unrepliedFeedback={}", 
                tasks.get("pendingResources"), tasks.get("pendingFeedback"), tasks.get("unrepliedFeedback"));
            
            return Result.success(tasks);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取待处理事项失败: error={}", e.getMessage(), e);
            throw e;
        }
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
        // 记录请求开始
        log.info("开始获取系统状态");
        
        try {
            // 调用服务层获取系统状态
            Map<String, Object> status = dashboardService.getSystemStatus();
            
            // 记录获取成功
            log.info("获取系统状态成功: cpuUsage={}%, memoryUsage={}%, diskUsage={}%", 
                status.get("cpuUsage"), status.get("memoryUsage"), status.get("diskUsage"));
            
            return Result.success(status);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取系统状态失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
}
