package com.resource.platform.module.crawler.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.module.crawler.dto.CrawlerLogQueryDTO;
import com.resource.platform.module.crawler.dto.CrawlerTaskDTO;
import com.resource.platform.module.crawler.dto.CrawlerTaskQueryDTO;
import com.resource.platform.module.crawler.service.CrawlerExecutionService;
import com.resource.platform.module.crawler.service.CrawlerLogService;
import com.resource.platform.module.crawler.service.CrawlerTaskService;
import com.resource.platform.module.crawler.vo.CrawlerLogVO;
import com.resource.platform.module.crawler.vo.CrawlerTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫管理控制器
 *
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "爬虫管理", description = "爬虫任务和日志管理接口")
@RestController
@RequestMapping("/api/crawler")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class CrawlerController {

    @Autowired
    private CrawlerTaskService crawlerTaskService;

    @Autowired
    private CrawlerLogService crawlerLogService;

    @Autowired
    private CrawlerExecutionService crawlerExecutionService;

    @Operation(summary = "创建爬虫任务", description = "创建新的爬虫任务配置")
    @PostMapping("/tasks")
    @OperationLog(module = "爬虫管理", type = "创建", description = "创建爬虫任务")
    public Result<CrawlerTaskVO> createTask(@Valid @RequestBody CrawlerTaskDTO dto) {
        CrawlerTaskVO task = crawlerTaskService.createTask(dto);
        return Result.success(task);
    }

    @Operation(summary = "更新爬虫任务", description = "更新爬虫任务配置")
    @PutMapping("/tasks/{id}")
    @OperationLog(module = "爬虫管理", type = "更新", description = "更新爬虫任务")
    public Result<CrawlerTaskVO> updateTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Valid @RequestBody CrawlerTaskDTO dto) {
        CrawlerTaskVO task = crawlerTaskService.updateTask(id, dto);
        return Result.success(task);
    }

    @Operation(summary = "删除爬虫任务", description = "删除爬虫任务及相关数据")
    @DeleteMapping("/tasks/{id}")
    @OperationLog(module = "爬虫管理", type = "删除", description = "删除爬虫任务")
    public Result<Void> deleteTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(description = "是否删除关联资源") @RequestParam(defaultValue = "false") Boolean deleteResources) {
        crawlerTaskService.deleteTask(id, deleteResources);
        return Result.success();
    }

    @Operation(summary = "切换任务状态", description = "启用或禁用爬虫任务")
    @PutMapping("/tasks/{id}/toggle")
    @OperationLog(module = "爬虫管理", type = "更新", description = "切换任务状态")
    public Result<CrawlerTaskVO> toggleStatus(@Parameter(description = "任务ID") @PathVariable Long id) {
        CrawlerTaskVO task = crawlerTaskService.toggleTaskStatus(id);
        return Result.success(task);
    }

    @Operation(summary = "查询爬虫任务列表", description = "分页查询爬虫任务")
    @GetMapping("/tasks")
    public Result<PageResult<CrawlerTaskVO>> queryTasks(@Valid CrawlerTaskQueryDTO queryDTO) {
        PageResult<CrawlerTaskVO> result = crawlerTaskService.queryTasks(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取任务详情", description = "获取爬虫任务详细信息")
    @GetMapping("/tasks/{id}")
    public Result<CrawlerTaskVO> getTaskDetail(@Parameter(description = "任务ID") @PathVariable Long id) {
        CrawlerTaskVO task = crawlerTaskService.getTaskDetail(id);
        return Result.success(task);
    }

    @Operation(summary = "立即触发任务", description = "手动触发爬虫任务执行")
    @PostMapping("/tasks/{id}/trigger")
    @OperationLog(module = "爬虫管理", type = "执行", description = "触发爬虫任务")
    public Result<Void> triggerTask(@Parameter(description = "任务ID") @PathVariable Long id) {
        crawlerTaskService.triggerTask(id);
        return Result.success();
    }

    @Operation(summary = "停止任务执行", description = "停止正在执行的爬虫任务")
    @PostMapping("/tasks/{id}/stop")
    @OperationLog(module = "爬虫管理", type = "执行", description = "停止爬虫任务")
    public Result<Void> stopTask(@Parameter(description = "任务ID") @PathVariable Long id) {
        crawlerExecutionService.stopTask(id);
        return Result.success();
    }

    @Operation(summary = "验证URL", description = "验证目标URL是否有效和可访问")
    @PostMapping("/validate-url")
    public Result<Map<String, Object>> validateUrl(
            @Parameter(description = "目标URL") @NotBlank @RequestParam String url) {
        boolean valid = crawlerTaskService.validateTargetUrl(url);

        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);
        result.put("url", url);
        result.put("message", valid ? "URL有效且可访问" : "URL无效或无法访问");
        return Result.success(result);
    }

    @Operation(summary = "查询执行日志", description = "分页查询爬虫执行日志")
    @GetMapping("/logs")
    public Result<PageResult<CrawlerLogVO>> queryLogs(@Valid CrawlerLogQueryDTO queryDTO) {
        PageResult<CrawlerLogVO> result = crawlerLogService.queryLogs(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "获取日志详情", description = "获取爬虫执行日志详细信息")
    @GetMapping("/logs/{id}")
    public Result<CrawlerLogVO> getLogDetail(@Parameter(description = "日志ID") @PathVariable Long id) {
        CrawlerLogVO logDetail = crawlerLogService.getLogDetail(id);
        return Result.success(logDetail);
    }

    @Operation(summary = "检查任务执行状态", description = "检查任务是否正在执行")
    @GetMapping("/tasks/{id}/running")
    public Result<Map<String, Object>> isTaskRunning(@Parameter(description = "任务ID") @PathVariable Long id) {
        boolean running = crawlerExecutionService.isTaskRunning(id);
        log.debug("任务执行状态: taskId={}, running={}", id, running);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", id);
        result.put("running", running);
        return Result.success(result);
    }
}
