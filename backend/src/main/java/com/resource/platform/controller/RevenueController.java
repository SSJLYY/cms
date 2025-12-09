package com.resource.platform.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.entity.Revenue;
import com.resource.platform.service.RevenueService;
import com.resource.platform.vo.RevenueOverviewVO;
import com.resource.platform.vo.RevenueTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收益管理控制器
 * 
 * 功能说明：
 * - 提供收益数据的概览统计接口
 * - 提供按类型分组的收益统计查询
 * - 提供收益明细的分页查询功能
 * - 支持收益记录的删除和批量删除
 * - 管理收益数据的查询和统计分析
 * - 记录收益管理的操作日志
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
    
    /**
     * 获取收益概览
     * 
     * 业务逻辑：
     * 1. 验证时间周期参数的有效性
     * 2. 查询指定周期的收益概览数据
     * 3. 统计总收益、下载次数等关键指标
     * 4. 返回收益概览统计结果
     * 
     * @param period 时间周期，默认为today（今天）
     * @return 收益概览统计数据
     */
    @Operation(summary = "获取收益概览")
    @GetMapping("/overview")
    public Result<RevenueOverviewVO> getOverview(@RequestParam(defaultValue = "today") String period) {
        // 记录请求开始
        log.info("开始获取收益概览: period={}", period);
        
        try {
            // 步骤1：验证参数
            // 检查时间周期参数是否有效
            if (period == null || period.trim().isEmpty()) {
                period = "today";
                log.debug("修正时间周期参数: period=today");
            }
            
            // 步骤2：调用Service层获取收益概览
            RevenueOverviewVO overview = revenueService.getOverview(period);
            
            // 记录成功结果
            log.info("收益概览获取成功: period={}, totalRevenue={}, totalDownloads={}, itemCount={}", 
                period, overview.getTotalRevenue(), overview.getTotalDownloads(), 
                overview.getRevenueItemCount());
            
            return Result.success(overview);
            
        } catch (Exception e) {
            // 记录异常
            log.error("收益概览获取失败: period={}, error={}", period, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 按类型获取收益统计
     * 
     * 业务逻辑：
     * 1. 验证时间周期参数的有效性
     * 2. 查询指定周期内各类型的收益统计
     * 3. 按收益类型分组统计金额和下载次数
     * 4. 返回分类收益统计结果
     * 
     * @param period 时间周期，默认为today（今天）
     * @return 按类型分组的收益统计列表
     */
    @Operation(summary = "按类型获取收益统计")
    @GetMapping("/by-type")
    public Result<List<RevenueTypeVO>> getRevenueByType(@RequestParam(defaultValue = "today") String period) {
        // 记录请求开始
        log.info("开始获取按类型收益统计: period={}", period);
        
        try {
            // 步骤1：验证参数
            // 检查时间周期参数是否有效
            if (period == null || period.trim().isEmpty()) {
                period = "today";
                log.debug("修正时间周期参数: period=today");
            }
            
            // 步骤2：调用Service层获取按类型收益统计
            List<RevenueTypeVO> revenueTypes = revenueService.getRevenueByType(period);
            
            // 记录成功结果
            log.info("按类型收益统计获取成功: period={}, typeCount={}", period, revenueTypes.size());
            
            // 记录各类型的收益统计
            for (RevenueTypeVO typeVO : revenueTypes) {
                log.debug("收益类型统计: type={}, typeName={}, totalAmount={}, downloadCount={}", 
                    typeVO.getRevenueType(), typeVO.getTypeName(), 
                    typeVO.getTotalAmount(), typeVO.getDownloadCount());
            }
            
            return Result.success(revenueTypes);
            
        } catch (Exception e) {
            // 记录异常
            log.error("按类型收益统计获取失败: period={}, error={}", period, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取收益明细列表
     * 
     * 业务逻辑：
     * 1. 验证分页查询参数的有效性
     * 2. 构建查询条件（时间周期、收益类型、状态）
     * 3. 执行分页查询并排序
     * 4. 返回收益明细分页结果
     * 
     * @param pageNum 页码，默认为1
     * @param pageSize 页大小，默认为10
     * @param period 时间周期，可选
     * @param revenueType 收益类型，可选
     * @param status 状态，可选
     * @return 收益明细分页结果
     */
    @Operation(summary = "获取收益明细列表")
    @GetMapping("/list")
    public Result<PageResult<Revenue>> getRevenueList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String revenueType,
            @RequestParam(required = false) String status) {
        // 记录请求开始
        log.info("开始获取收益明细列表: pageNum={}, pageSize={}, period={}, revenueType={}, status={}", 
            pageNum, pageSize, period, revenueType, status);
        
        try {
            // 步骤1：验证分页参数
            // 检查分页参数的合理性
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
                log.debug("修正页码参数: pageNum=1");
            }
            
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
                log.debug("修正页大小参数: pageSize=10");
            }
            
            // 步骤2：调用Service层执行分页查询
            PageResult<Revenue> result = revenueService.getRevenueList(
                pageNum, pageSize, period, revenueType, status);
            
            // 记录成功结果
            log.info("收益明细列表获取成功: total={}, records={}, pageNum={}, pageSize={}", 
                result.getTotal(), result.getRecords().size(), pageNum, pageSize);
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常
            log.error("收益明细列表获取失败: pageNum={}, pageSize={}, period={}, error={}", 
                pageNum, pageSize, period, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 删除收益记录
     * 
     * 业务逻辑：
     * 1. 验证收益记录ID的有效性
     * 2. 检查收益记录是否存在
     * 3. 执行删除操作
     * 4. 记录删除操作日志
     * 
     * @param id 收益记录ID
     * @return 操作结果
     * @throws ResourceNotFoundException 当收益记录不存在时抛出
     */
    @Operation(summary = "删除收益记录")
    @DeleteMapping("/{id}")
    @OperationLog(module = "收益管理", type = "删除", description = "删除收益记录")
    public Result<Void> deleteRevenue(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始删除收益记录: id={}", id);
        
        try {
            // 步骤1：验证参数
            // 检查收益记录ID是否有效
            if (id == null || id <= 0) {
                log.warn("收益记录ID无效: id={}", id);
                throw new IllegalArgumentException("收益记录ID无效");
            }
            
            // 步骤2：调用Service层删除收益记录
            boolean success = revenueService.deleteRevenue(id);
            
            // 步骤3：处理删除结果
            if (success) {
                // 记录成功结果
                log.info("收益记录删除成功: id={}", id);
                return Result.success();
            } else {
                // 记录删除失败
                log.warn("收益记录删除失败: id={}", id);
                return Result.error("删除失败");
            }
            
        } catch (Exception e) {
            // 记录异常
            log.error("收益记录删除异常: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 批量删除收益记录
     * 
     * 业务逻辑：
     * 1. 验证ID列表的有效性
     * 2. 检查ID列表是否为空
     * 3. 执行批量删除操作
     * 4. 记录批量删除结果
     * 
     * @param ids 收益记录ID列表
     * @return 操作结果
     * @throws BusinessException 当ID列表为空时抛出
     */
    @Operation(summary = "批量删除收益记录")
    @DeleteMapping("/batch")
    @OperationLog(module = "收益管理", type = "批量删除", description = "批量删除收益记录")
    public Result<Void> batchDeleteRevenue(@RequestBody List<Long> ids) {
        // 记录请求开始
        log.info("开始批量删除收益记录: ids={}, count={}", ids, ids != null ? ids.size() : 0);
        
        try {
            // 步骤1：验证参数
            // 检查ID列表是否为空
            if (ids == null || ids.isEmpty()) {
                log.warn("批量删除收益记录ID列表为空");
                throw new IllegalArgumentException("删除ID列表不能为空");
            }
            
            // 检查ID列表中是否有无效ID
            for (Long id : ids) {
                if (id == null || id <= 0) {
                    log.warn("批量删除包含无效ID: id={}", id);
                    throw new IllegalArgumentException("包含无效的收益记录ID: " + id);
                }
            }
            
            // 步骤2：调用Service层批量删除收益记录
            boolean success = revenueService.batchDeleteRevenue(ids);
            
            // 步骤3：处理删除结果
            if (success) {
                // 记录成功结果
                log.info("收益记录批量删除成功: count={}", ids.size());
                return Result.success();
            } else {
                // 记录删除失败
                log.warn("收益记录批量删除失败: count={}", ids.size());
                return Result.error("批量删除失败");
            }
            
        } catch (Exception e) {
            // 记录异常
            log.error("收益记录批量删除异常: ids={}, error={}", ids, e.getMessage(), e);
            throw e;
        }
    }
}
