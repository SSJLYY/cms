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
 * 
 * 功能说明：
 * - 提供系统日志的查询和管理接口
 * - 提供审计日志的查询和管理接口
 * - 支持日志的统计分析功能
 * - 提供日志清理和导出功能
 * - 支持多维度的日志筛选和搜索
 * 
 * @author 系统
 * @since 1.0
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
     * 
     * 业务逻辑：
     * 1. 统计总日志数量
     * 2. 统计今日日志数量
     * 3. 统计成功和失败日志数量
     * 4. 计算平均响应时间
     * 5. 返回统计结果
     * 
     * @return 日志统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取日志统计信息")
    public Result<LogStatisticsVO> getStatistics() {
        // 记录请求开始
        log.info("开始获取日志统计信息");
        
        try {
            // 调用服务层获取统计信息
            LogStatisticsVO statistics = logService.getStatistics();
            
            // 记录获取成功
            log.info("获取日志统计信息成功: total={}, today={}, success={}, error={}, avgDuration={}ms", 
                statistics.getTotalLogs(), statistics.getTodayLogs(), 
                statistics.getSuccessLogs(), statistics.getErrorLogs(), statistics.getAvgDuration());
            
            return Result.success(statistics);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取日志统计信息失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 查询日志列表
     * 
     * 业务逻辑：
     * 1. 接收查询条件（模块、类型、状态、时间范围、关键词等）
     * 2. 执行分页查询
     * 3. 按创建时间倒序排列
     * 4. 返回分页结果
     * 
     * @param queryDTO 查询条件
     * @return 日志分页结果
     */
    @PostMapping("/query")
    @Operation(summary = "查询日志列表")
    public Result<Page<SystemLog>> queryLogs(@RequestBody LogQueryDTO queryDTO) {
        // 记录查询请求开始
        log.info("开始查询日志列表: page={}, pageSize={}, module={}, type={}, status={}, keyword={}", 
            queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getModule(), 
            queryDTO.getType(), queryDTO.getStatus(), queryDTO.getKeyword());
        
        try {
            // 调用服务层执行查询
            Page<SystemLog> page = logService.queryLogs(queryDTO);
            
            // 记录查询成功
            log.info("查询日志列表成功: total={}, records={}", page.getTotal(), page.getRecords().size());
            
            return Result.success(page);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("查询日志列表失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取日志详情
     * 
     * 业务逻辑：
     * 1. 根据ID查询日志信息
     * 2. 验证日志是否存在
     * 3. 返回日志详情
     * 
     * @param id 日志ID
     * @return 日志详情
     * @throws ResourceNotFoundException 当日志不存在时抛出
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取日志详情")
    public Result<SystemLog> getLogById(@PathVariable Long id) {
        // 记录查询请求开始
        log.info("开始获取日志详情: logId={}", id);
        
        try {
            // 调用服务层查询日志详情
            SystemLog systemLog = logService.getLogById(id);
            
            // 记录查询成功
            log.info("获取日志详情成功: logId={}, module={}, type={}, status={}", 
                id, systemLog.getModule(), systemLog.getType(), systemLog.getStatus());
            
            return Result.success(systemLog);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("获取日志详情失败: logId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 清理日志
     * 
     * 业务逻辑：
     * 1. 根据指定时间清理历史日志
     * 2. 删除指定时间之前的所有日志
     * 3. 统计清理的日志数量
     * 4. 返回清理结果
     * 
     * @param beforeTime 清理时间点，删除此时间之前的日志
     * @return 清理的日志数量
     */
    @DeleteMapping("/clean")
    @Operation(summary = "清理日志")
    @OperationLog(module = "日志管理", type = "清理", description = "清理系统日志", audit = true)
    public Result<Integer> cleanLogs(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        
        // 记录清理请求开始
        log.info("开始清理系统日志: beforeTime={}", beforeTime);
        
        try {
            // 调用服务层清理日志
            int count = logService.cleanLogs(beforeTime);
            
            // 记录清理成功
            log.info("清理系统日志成功: beforeTime={}, cleanedCount={}", beforeTime, count);
            
            return Result.success(count);
            
        } catch (Exception e) {
            // 记录清理失败
            log.error("清理系统日志失败: beforeTime={}, error={}", beforeTime, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 导出日志
     * 
     * 业务逻辑：
     * 1. 根据查询条件筛选日志
     * 2. 生成CSV格式文件
     * 3. 保存到临时目录
     * 4. 返回文件路径
     * 
     * @param queryDTO 查询条件
     * @return 导出文件路径
     * @throws RuntimeException 当导出失败时抛出
     */
    @PostMapping("/export")
    @Operation(summary = "导出日志")
    @OperationLog(module = "日志管理", type = "导出", description = "导出系统日志", audit = true)
    public Result<String> exportLogs(@RequestBody LogQueryDTO queryDTO) {
        // 记录导出请求开始
        log.info("开始导出系统日志: module={}, type={}, status={}", 
            queryDTO.getModule(), queryDTO.getType(), queryDTO.getStatus());
        
        try {
            // 调用服务层导出日志
            String filePath = logService.exportLogs(queryDTO);
            
            // 记录导出成功
            log.info("导出系统日志成功: filePath={}", filePath);
            
            return Result.success(filePath);
            
        } catch (Exception e) {
            // 记录导出失败
            log.error("导出系统日志失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            throw e;
        }
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
