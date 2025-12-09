package com.resource.platform.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.CrawlerLogQueryDTO;
import com.resource.platform.dto.CrawlerTaskDTO;
import com.resource.platform.dto.CrawlerTaskQueryDTO;
import com.resource.platform.service.CrawlerExecutionService;
import com.resource.platform.service.CrawlerLogService;
import com.resource.platform.service.CrawlerTaskService;
import com.resource.platform.vo.CrawlerLogVO;
import com.resource.platform.vo.CrawlerTaskVO;
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
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫管理控制器
 * 
 * 功能说明：
 * - 提供爬虫任务的增删改查接口
 * - 支持爬虫任务的手动触发和停止
 * - 提供爬虫执行日志的查询接口
 * - 支持URL验证和任务状态检查
 * - 管理爬虫任务的定时执行配置
 * - 处理爬虫任务的分类映射和自定义规则
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

    /**
     * 创建爬虫任务
     * 
     * 业务逻辑：
     * 1. 验证目标URL的有效性和可访问性
     * 2. 转换分类映射和自定义规则为JSON格式
     * 3. 设置任务的默认统计值
     * 4. 计算下次执行时间（如果任务启用）
     * 5. 保存任务到数据库
     * 6. 返回创建成功的任务信息
     * 
     * @param dto 爬虫任务数据传输对象
     * @return 创建成功的爬虫任务VO对象
     * @throws RuntimeException 当URL验证失败或数据格式错误时抛出
     */
    @Operation(summary = "创建爬虫任务", description = "创建新的爬虫任务配置")
    @PostMapping("/tasks")
    @OperationLog(module = "爬虫管理", type = "创建", description = "创建爬虫任务")
    public Result<CrawlerTaskVO> createTask(@Valid @RequestBody CrawlerTaskDTO dto) {
        // 记录创建请求开始
        log.info("开始创建爬虫任务: name={}, targetUrl={}, crawlInterval={}, maxDepth={}", 
            dto.getName(), dto.getTargetUrl(), dto.getCrawlInterval(), dto.getMaxDepth());
        
        try {
            // 调用服务层创建任务
            CrawlerTaskVO task = crawlerTaskService.createTask(dto);
            
            // 记录创建成功
            log.info("创建爬虫任务成功: taskId={}, name={}, status={}", 
                task.getId(), task.getName(), task.getStatus());
            
            return Result.success(task);
            
        } catch (Exception e) {
            // 记录创建失败
            log.error("创建爬虫任务失败: name={}, targetUrl={}, error={}", 
                dto.getName(), dto.getTargetUrl(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 更新爬虫任务
     * 
     * 业务逻辑：
     * 1. 验证任务是否存在
     * 2. 验证新的目标URL（如果有变化）
     * 3. 更新任务配置信息
     * 4. 重新计算下次执行时间
     * 5. 保存更新到数据库
     * 
     * @param id 任务ID
     * @param dto 更新的任务数据
     * @return 更新后的任务信息
     * @throws RuntimeException 当任务不存在或URL验证失败时抛出
     */
    @Operation(summary = "更新爬虫任务", description = "更新爬虫任务配置")
    @PutMapping("/tasks/{id}")
    @OperationLog(module = "爬虫管理", type = "更新", description = "更新爬虫任务")
    public Result<CrawlerTaskVO> updateTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Valid @RequestBody CrawlerTaskDTO dto) {
        
        // 记录更新请求开始
        log.info("开始更新爬虫任务: taskId={}, name={}, targetUrl={}", id, dto.getName(), dto.getTargetUrl());
        
        try {
            // 调用服务层更新任务
            CrawlerTaskVO task = crawlerTaskService.updateTask(id, dto);
            
            // 记录更新成功
            log.info("更新爬虫任务成功: taskId={}, name={}, status={}", id, task.getName(), task.getStatus());
            
            return Result.success(task);
            
        } catch (Exception e) {
            // 记录更新失败
            log.error("更新爬虫任务失败: taskId={}, name={}, error={}", id, dto.getName(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除爬虫任务
     * 
     * 业务逻辑：
     * 1. 验证任务是否存在
     * 2. 检查是否需要删除关联资源
     * 3. 逻辑删除关联资源（如果选择删除）
     * 4. 删除任务记录
     * 
     * @param id 任务ID
     * @param deleteResources 是否删除关联资源
     * @return 删除成功响应
     * @throws RuntimeException 当任务不存在时抛出
     */
    @Operation(summary = "删除爬虫任务", description = "删除爬虫任务及相关数据")
    @DeleteMapping("/tasks/{id}")
    @OperationLog(module = "爬虫管理", type = "删除", description = "删除爬虫任务")
    public Result<Void> deleteTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(description = "是否删除关联资源") @RequestParam(defaultValue = "false") Boolean deleteResources) {
        
        // 记录删除请求开始
        log.info("开始删除爬虫任务: taskId={}, deleteResources={}", id, deleteResources);
        
        try {
            // 调用服务层删除任务
            crawlerTaskService.deleteTask(id, deleteResources);
            
            // 记录删除成功
            log.info("删除爬虫任务成功: taskId={}, deleteResources={}", id, deleteResources);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录删除失败
            log.error("删除爬虫任务失败: taskId={}, deleteResources={}, error={}", 
                id, deleteResources, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 切换任务状态
     * 
     * 业务逻辑：
     * 1. 验证任务是否存在
     * 2. 切换任务状态（启用<->禁用）
     * 3. 更新下次执行时间
     * 4. 保存状态变更
     * 
     * @param id 任务ID
     * @return 更新后的任务信息
     * @throws RuntimeException 当任务不存在时抛出
     */
    @Operation(summary = "切换任务状态", description = "启用或禁用爬虫任务")
    @PutMapping("/tasks/{id}/toggle")
    @OperationLog(module = "爬虫管理", type = "更新", description = "切换任务状态")
    public Result<CrawlerTaskVO> toggleStatus(@Parameter(description = "任务ID") @PathVariable Long id) {
        // 记录切换请求开始
        log.info("开始切换任务状态: taskId={}", id);
        
        try {
            // 调用服务层切换状态
            CrawlerTaskVO task = crawlerTaskService.toggleTaskStatus(id);
            
            // 记录切换成功
            log.info("切换任务状态成功: taskId={}, newStatus={}", id, task.getStatus());
            
            return Result.success(task);
            
        } catch (Exception e) {
            // 记录切换失败
            log.error("切换任务状态失败: taskId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 查询爬虫任务列表
     * 
     * 业务逻辑：
     * 1. 接收查询条件（名称、状态等）
     * 2. 执行分页查询
     * 3. 转换为VO对象列表
     * 4. 返回分页结果
     * 
     * @param queryDTO 查询条件对象
     * @return 任务分页结果
     */
    @Operation(summary = "查询爬虫任务列表", description = "分页查询爬虫任务")
    @GetMapping("/tasks")
    public Result<PageResult<CrawlerTaskVO>> queryTasks(@Valid CrawlerTaskQueryDTO queryDTO) {
        // 记录查询请求开始
        log.info("开始查询爬虫任务列表: page={}, pageSize={}, name={}, status={}", 
            queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getName(), queryDTO.getStatus());
        
        try {
            // 调用服务层执行查询
            PageResult<CrawlerTaskVO> result = crawlerTaskService.queryTasks(queryDTO);
            
            // 记录查询成功
            log.info("查询爬虫任务列表成功: total={}, records={}", 
                result.getTotal(), result.getRecords().size());
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("查询爬虫任务列表失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取任务详情
     * 
     * 业务逻辑：
     * 1. 根据ID查询任务信息
     * 2. 验证任务是否存在
     * 3. 转换为VO对象
     * 4. 返回任务详情
     * 
     * @param id 任务ID
     * @return 任务详情对象
     * @throws RuntimeException 当任务不存在时抛出
     */
    @Operation(summary = "获取任务详情", description = "获取爬虫任务详细信息")
    @GetMapping("/tasks/{id}")
    public Result<CrawlerTaskVO> getTaskDetail(@Parameter(description = "任务ID") @PathVariable Long id) {
        // 记录查询请求开始
        log.info("开始获取任务详情: taskId={}", id);
        
        try {
            // 调用服务层查询任务详情
            CrawlerTaskVO task = crawlerTaskService.getTaskDetail(id);
            
            // 记录查询成功
            log.info("获取任务详情成功: taskId={}, name={}, status={}", 
                id, task.getName(), task.getStatus());
            
            return Result.success(task);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("获取任务详情失败: taskId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 立即触发任务
     * 
     * 业务逻辑：
     * 1. 检查任务是否存在
     * 2. 检查任务是否正在执行
     * 3. 提交任务到线程池执行
     * 4. 记录执行日志
     * 5. 返回触发成功响应
     * 
     * @param id 任务ID
     * @return 触发成功响应
     * @throws BusinessException 当任务不存在或正在执行时抛出
     */
    @Operation(summary = "立即触发任务", description = "手动触发爬虫任务执行")
    @PostMapping("/tasks/{id}/trigger")
    @OperationLog(module = "爬虫管理", type = "执行", description = "触发爬虫任务")
    public Result<Void> triggerTask(@Parameter(description = "任务ID") @PathVariable Long id) {
        // 记录触发请求开始
        log.info("开始手动触发爬虫任务: taskId={}", id);
        
        try {
            // 调用执行服务触发任务（1表示手动触发）
            crawlerExecutionService.executeCrawlerTask(id, 1);
            
            // 记录触发成功
            log.info("手动触发爬虫任务成功: taskId={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录触发失败
            log.error("手动触发爬虫任务失败: taskId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 停止任务执行
     * 
     * 业务逻辑：
     * 1. 检查任务是否正在执行
     * 2. 设置停止标记
     * 3. 等待任务自然停止
     * 4. 更新任务状态和日志
     * 
     * @param id 任务ID
     * @return 停止成功响应
     * @throws BusinessException 当任务未在执行时抛出
     */
    @Operation(summary = "停止任务执行", description = "停止正在执行的爬虫任务")
    @PostMapping("/tasks/{id}/stop")
    @OperationLog(module = "爬虫管理", type = "执行", description = "停止爬虫任务")
    public Result<Void> stopTask(@Parameter(description = "任务ID") @PathVariable Long id) {
        // 记录停止请求开始
        log.info("开始停止爬虫任务: taskId={}", id);
        
        try {
            // 调用执行服务停止任务
            crawlerExecutionService.stopTask(id);
            
            // 记录停止成功
            log.info("停止爬虫任务成功: taskId={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录停止失败
            log.error("停止爬虫任务失败: taskId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 验证URL
     * 
     * 业务逻辑：
     * 1. 验证URL格式是否正确
     * 2. 检查URL是否可访问
     * 3. 返回验证结果和相关信息
     * 
     * @param url 目标URL
     * @return 验证结果，包含有效性、URL和消息
     */
    @Operation(summary = "验证URL", description = "验证目标URL是否有效和可访问")
    @PostMapping("/validate-url")
    public Result<Map<String, Object>> validateUrl(
            @Parameter(description = "目标URL") @NotBlank @RequestParam String url) {
        
        // 记录验证请求开始
        log.info("开始验证URL: url={}", url);
        
        try {
            // 调用服务层验证URL
            boolean valid = crawlerTaskService.validateTargetUrl(url);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("valid", valid);
            result.put("url", url);
            result.put("message", valid ? "URL有效且可访问" : "URL无效或无法访问");
            
            // 记录验证结果
            log.info("URL验证完成: url={}, valid={}", url, valid);
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录验证失败
            log.error("URL验证失败: url={}, error={}", url, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 查询执行日志
     * 
     * 业务逻辑：
     * 1. 接收查询条件（任务ID、执行状态、时间范围等）
     * 2. 执行分页查询
     * 3. 转换为VO对象列表
     * 4. 返回分页结果
     * 
     * @param queryDTO 日志查询条件对象
     * @return 日志分页结果
     */
    @Operation(summary = "查询执行日志", description = "分页查询爬虫执行日志")
    @GetMapping("/logs")
    public Result<PageResult<CrawlerLogVO>> queryLogs(@Valid CrawlerLogQueryDTO queryDTO) {
        // 记录查询请求开始
        log.info("开始查询爬虫执行日志: page={}, pageSize={}, taskId={}, status={}", 
            queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getTaskId(), queryDTO.getStatus());
        
        try {
            // 调用服务层执行查询
            PageResult<CrawlerLogVO> result = crawlerLogService.queryLogs(queryDTO);
            
            // 记录查询成功
            log.info("查询爬虫执行日志成功: total={}, records={}", 
                result.getTotal(), result.getRecords().size());
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("查询爬虫执行日志失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取日志详情
     * 
     * 业务逻辑：
     * 1. 根据ID查询日志信息
     * 2. 验证日志是否存在
     * 3. 转换为VO对象
     * 4. 返回日志详情
     * 
     * @param id 日志ID
     * @return 日志详情对象
     * @throws RuntimeException 当日志不存在时抛出
     */
    @Operation(summary = "获取日志详情", description = "获取爬虫执行日志详细信息")
    @GetMapping("/logs/{id}")
    public Result<CrawlerLogVO> getLogDetail(@Parameter(description = "日志ID") @PathVariable Long id) {
        // 记录查询请求开始
        log.info("开始获取日志详情: logId={}", id);
        
        try {
            // 调用服务层查询日志详情
            CrawlerLogVO logDetail = crawlerLogService.getLogDetail(id);
            
            // 记录查询成功
            log.info("获取日志详情成功: logId={}, taskName={}, status={}", 
                id, logDetail.getTaskName(), logDetail.getStatus());
            
            return Result.success(logDetail);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("获取日志详情失败: logId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 检查任务是否正在执行
     * 
     * 业务逻辑：
     * 1. 查询任务的执行状态
     * 2. 检查任务是否在执行队列中
     * 3. 返回执行状态信息
     * 
     * @param id 任务ID
     * @return 任务执行状态信息
     */
    @Operation(summary = "检查任务执行状态", description = "检查任务是否正在执行")
    @GetMapping("/tasks/{id}/running")
    public Result<Map<String, Object>> isTaskRunning(@Parameter(description = "任务ID") @PathVariable Long id) {
        // 记录检查请求
        log.debug("检查任务执行状态: taskId={}", id);
        
        try {
            // 调用执行服务检查任务状态
            boolean running = crawlerExecutionService.isTaskRunning(id);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", id);
            result.put("running", running);
            
            // 记录检查结果
            log.debug("任务执行状态检查完成: taskId={}, running={}", id, running);
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录检查失败
            log.error("检查任务执行状态失败: taskId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
}
