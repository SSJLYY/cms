package com.resource.platform.controller;

import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.VisitQueryDTO;
import com.resource.platform.service.StatisticsService;
import com.resource.platform.vo.StatisticsOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "统计管理")
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Operation(summary = "获取统计概览")
    @GetMapping("/overview")
    public Result<StatisticsOverviewVO> getOverview(@RequestParam(defaultValue = "today") String period) {
        return Result.success(statisticsService.getOverview(period));
    }
    
    @Operation(summary = "获取下载分布数据")
    @GetMapping("/download-distribution")
    public Result<List<Map<String, Object>>> getDownloadDistribution(@RequestParam(defaultValue = "today") String period) {
        return Result.success(statisticsService.getDownloadDistribution(period));
    }
    
    @Operation(summary = "获取访问统计详情")
    @GetMapping("/visit-details")
    public Result<PageResult<Map<String, Object>>> getVisitDetails(VisitQueryDTO query) {
        return Result.success(statisticsService.getVisitDetails(query));
    }
    
    @Operation(summary = "获取实时活动")
    @GetMapping("/realtime-activities")
    public Result<List<Map<String, Object>>> getRealtimeActivities(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statisticsService.getRealtimeActivities(limit));
    }
}
