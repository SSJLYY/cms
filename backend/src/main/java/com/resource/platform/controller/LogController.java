package com.resource.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.Result;
import com.resource.platform.dto.AuditLogQueryDTO;
import com.resource.platform.dto.LogQueryDTO;
import com.resource.platform.entity.AuditLog;
import com.resource.platform.entity.SystemLog;
import com.resource.platform.service.AuditLogService;
import com.resource.platform.service.LogService;
import com.resource.platform.vo.AuditLogStatisticsVO;
import com.resource.platform.vo.LogStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 日志管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
@Tag(name = "日志管理", description = "系统日志和审计日志管理接口")
public class LogController {

    @Autowired
    private LogService logService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 获取日志统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取日志统计信息")
    public Result<LogStatisticsVO> getStatistics() {
        LogStatisticsVO statistics = logService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 查询日志列表
     */
    @PostMapping("/query")
    @Operation(summary = "查询日志列表")
    public Result<Page<SystemLog>> queryLogs(@RequestBody LogQueryDTO queryDTO) {
        Page<SystemLog> page = logService.queryLogs(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取日志详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取日志详情")
    public Result<SystemLog> getLogById(@PathVariable Long id) {
        SystemLog log = logService.getLogById(id);
        return Result.success(log);
    }

    /**
     * 清理日志
     */
    @DeleteMapping("/clean")
    @Operation(summary = "清理日志")
    @OperationLog(module = "日志管理", type = "清理", description = "清理系统日志", audit = true)
    public Result<Integer> cleanLogs(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        int count = logService.cleanLogs(beforeTime);
        return Result.success(count);
    }

    /**
     * 导出日志
     */
    @PostMapping("/export")
    @Operation(summary = "导出日志")
    @OperationLog(module = "日志管理", type = "导出", description = "导出系统日志", audit = true)
    public Result<String> exportLogs(@RequestBody LogQueryDTO queryDTO) {
        String filePath = logService.exportLogs(queryDTO);
        return Result.success(filePath);
    }

    /**
     * 获取审计日志统计信息
     */
    @GetMapping("/audit/statistics")
    @Operation(summary = "获取审计日志统计信息")
    public Result<AuditLogStatisticsVO> getAuditStatistics() {
        AuditLogStatisticsVO statistics = auditLogService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 查询审计日志列表
     */
    @PostMapping("/audit/query")
    @Operation(summary = "查询审计日志列表")
    public Result<Page<AuditLog>> queryAuditLogs(@RequestBody AuditLogQueryDTO queryDTO) {
        Page<AuditLog> page = auditLogService.queryAuditLogs(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取审计日志详情
     */
    @GetMapping("/audit/{id}")
    @Operation(summary = "获取审计日志详情")
    public Result<AuditLog> getAuditLogById(@PathVariable Long id) {
        AuditLog auditLog = auditLogService.getAuditLogById(id);
        return Result.success(auditLog);
    }
}
