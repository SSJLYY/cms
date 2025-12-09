package com.resource.platform.controller;

import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.VisitQueryDTO;
import com.resource.platform.service.StatisticsService;
import com.resource.platform.vo.StatisticsOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计管理控制器
 * 
 * 功能说明：
 * - 提供系统统计概览接口
 * - 提供下载分布统计接口
 * - 提供访问统计详情接口
 * - 提供实时活动监控接口
 * - 支持多种时间周期的统计查询
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "统计管理")
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    /**
     * 获取统计概览
     * 
     * 业务逻辑：
     * 1. 根据时间周期计算统计范围
     * 2. 统计总下载量和总访问量
     * 3. 计算新增访问量
     * 4. 返回统计概览数据
     * 
     * @param period 统计周期（today/yesterday/week/month）
     * @return 统计概览数据
     */
    @Operation(summary = "获取统计概览")
    @GetMapping("/overview")
    public Result<StatisticsOverviewVO> getOverview(@RequestParam(defaultValue = "today") String period) {
        // 记录请求开始
        log.info("开始获取统计概览: period={}", period);
        
        try {
            // 调用服务层获取统计概览
            StatisticsOverviewVO overview = statisticsService.getOverview(period);
            
            // 记录获取成功
            log.info("获取统计概览成功: period={}, totalDownloads={}, totalVisits={}, newVisits={}", 
                period, overview.getTotalDownloads(), overview.getTotalVisits(), overview.getNewVisits());
            
            return Result.success(overview);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取统计概览失败: period={}, error={}", period, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取下载分布数据
     * 
     * 业务逻辑：
     * 1. 根据时间周期查询下载日志
     * 2. 按资源ID分组统计下载次数
     * 3. 获取资源标题信息
     * 4. 按下载次数排序返回前10名
     * 
     * @param period 统计周期（today/yesterday/week/month）
     * @return 下载分布数据列表
     */
    @Operation(summary = "获取下载分布数据")
    @GetMapping("/download-distribution")
    public Result<List<Map<String, Object>>> getDownloadDistribution(@RequestParam(defaultValue = "today") String period) {
        // 记录请求开始
        log.info("开始获取下载分布数据: period={}", period);
        
        try {
            // 调用服务层获取下载分布
            List<Map<String, Object>> distribution = statisticsService.getDownloadDistribution(period);
            
            // 记录获取成功
            log.info("获取下载分布数据成功: period={}, count={}", period, distribution.size());
            
            return Result.success(distribution);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取下载分布数据失败: period={}, error={}", period, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取访问统计详情
     * 
     * 业务逻辑：
     * 1. 根据查询条件执行分页查询
     * 2. 按资源ID分组统计访问次数
     * 3. 获取资源和分类信息
     * 4. 获取访问来源和浏览器信息
     * 5. 返回分页结果
     * 
     * @param query 访问查询条件
     * @return 访问统计详情分页结果
     */
    @Operation(summary = "获取访问统计详情")
    @GetMapping("/visit-details")
    public Result<PageResult<Map<String, Object>>> getVisitDetails(VisitQueryDTO query) {
        // 记录请求开始
        log.info("开始获取访问统计详情: page={}, pageSize={}, period={}", 
            query.getPageNum(), query.getPageSize(), query.getPeriod());
        
        try {
            // 调用服务层获取访问详情
            PageResult<Map<String, Object>> details = statisticsService.getVisitDetails(query);
            
            // 记录获取成功
            log.info("获取访问统计详情成功: total={}, records={}", 
                details.getTotal(), details.getRecords().size());
            
            return Result.success(details);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取访问统计详情失败: query={}, error={}", query, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取实时活动
     * 
     * 业务逻辑：
     * 1. 查询最新的访问日志记录
     * 2. 获取资源信息和活动类型
     * 3. 构建活动描述信息
     * 4. 按时间倒序返回活动列表
     * 
     * @param limit 返回记录数限制，默认10条
     * @return 实时活动列表
     */
    @Operation(summary = "获取实时活动")
    @GetMapping("/realtime-activities")
    public Result<List<Map<String, Object>>> getRealtimeActivities(@RequestParam(defaultValue = "10") Integer limit) {
        // 记录请求开始
        log.info("开始获取实时活动: limit={}", limit);
        
        try {
            // 调用服务层获取实时活动
            List<Map<String, Object>> activities = statisticsService.getRealtimeActivities(limit);
            
            // 记录获取成功
            log.info("获取实时活动成功: count={}", activities.size());
            
            return Result.success(activities);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取实时活动失败: limit={}, error={}", limit, e.getMessage(), e);
            throw e;
        }
    }
}
