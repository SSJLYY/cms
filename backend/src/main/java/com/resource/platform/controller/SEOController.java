package com.resource.platform.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.SubmissionDTO;
import com.resource.platform.entity.SEOSubmission;
import com.resource.platform.service.SEOService;
import com.resource.platform.vo.SEOStatsVO;
import com.resource.platform.vo.SubmissionResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * SEO管理控制器
 * 
 * 功能说明：
 * - 提供SEO统计数据的查询接口
 * - 支持网站地图的生成和管理
 * - 提供搜索引擎提交功能（百度、必应）
 * - 支持批量提交到多个搜索引擎
 * - 管理SEO提交历史记录
 * - 提供重新提交和删除记录功能
 * 
 * 主要职责：
 * - SEO数据统计和展示
 * - 搜索引擎提交管理
 * - 网站地图生成
 * - 提交历史记录管理
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "SEO管理")
@RestController
@RequestMapping("/api/seo")
public class SEOController {
    
    @Autowired
    private SEOService seoService;
    
    /**
     * 获取SEO统计
     * 
     * 业务逻辑：
     * 1. 调用Service层获取SEO统计数据
     * 2. 统计总提交数、成功数、失败数等指标
     * 3. 返回SEO统计概览信息
     * 
     * @return SEO统计数据，包含各项指标
     */
    @Operation(summary = "获取SEO统计")
    @GetMapping("/statistics")
    public Result<SEOStatsVO> getStatistics() {
        // 记录请求开始
        log.info("开始获取SEO统计数据");
        
        try {
            // 调用Service层获取SEO统计
            SEOStatsVO stats = seoService.getStats();
            
            // 记录成功结果
            log.info("SEO统计数据获取成功: totalSubmissions={}, successSubmissions={}, failedSubmissions={}, todaySubmissions={}", 
                stats.getTotalSubmissions(), stats.getSuccessSubmissions(), 
                stats.getFailedSubmissions(), stats.getTodaySubmissions());
            
            return Result.success(stats);
            
        } catch (Exception e) {
            // 记录异常
            log.error("SEO统计数据获取失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 生成网站地图
     * 
     * 业务逻辑：
     * 1. 调用Service层生成XML格式的网站地图
     * 2. 包含所有有效资源的URL信息
     * 3. 返回生成的网站地图内容
     * 
     * @return 生成的XML格式网站地图内容
     */
    @Operation(summary = "生成网站地图")
    @PostMapping("/sitemap/generate")
    @OperationLog(module = "SEO管理", type = "生成", description = "生成网站地图")
    public Result<String> generateSitemap() {
        // 记录请求开始
        log.info("开始生成网站地图");
        
        try {
            // 调用Service层生成网站地图
            String sitemap = seoService.generateSitemap();
            
            // 记录成功结果
            log.info("网站地图生成成功: length={}", sitemap.length());
            
            return Result.success(sitemap);
            
        } catch (Exception e) {
            // 记录异常
            log.error("网站地图生成失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 提交到百度
     * 
     * 业务逻辑：
     * 1. 调用Service层执行百度搜索引擎提交
     * 2. 批量提交所有有效资源URL
     * 3. 记录提交结果和统计信息
     * 
     * @return 提交结果，包含成功和失败统计
     */
    @Operation(summary = "提交到百度")
    @PostMapping("/submit/baidu")
    @OperationLog(module = "SEO管理", type = "提交", description = "提交到百度搜索引擎")
    public Result<SubmissionResultVO> submitToBaidu() {
        // 记录请求开始
        log.info("开始提交到百度搜索引擎");
        
        try {
            // 调用Service层执行百度提交
            SubmissionResultVO result = seoService.batchSubmit("baidu");
            
            // 记录成功结果
            log.info("百度搜索引擎提交完成: success={}, successCount={}, failedCount={}", 
                result.isSuccess(), result.getSuccessCount(), result.getFailedCount());
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常
            log.error("百度搜索引擎提交失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 提交到必应
     * 
     * 业务逻辑：
     * 1. 调用Service层执行必应搜索引擎提交
     * 2. 批量提交所有有效资源URL
     * 3. 记录提交结果和统计信息
     * 
     * @return 提交结果，包含成功和失败统计
     */
    @Operation(summary = "提交到必应")
    @PostMapping("/submit/bing")
    @OperationLog(module = "SEO管理", type = "提交", description = "提交到必应搜索引擎")
    public Result<SubmissionResultVO> submitToBing() {
        // 记录请求开始
        log.info("开始提交到必应搜索引擎");
        
        try {
            // 调用Service层执行必应提交
            SubmissionResultVO result = seoService.batchSubmit("bing");
            
            // 记录成功结果
            log.info("必应搜索引擎提交完成: success={}, successCount={}, failedCount={}", 
                result.isSuccess(), result.getSuccessCount(), result.getFailedCount());
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常
            log.error("必应搜索引擎提交失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 批量提交到所有搜索引擎
     * 
     * 业务逻辑：
     * 1. 调用Service层执行批量提交操作
     * 2. 同时提交到百度和必应搜索引擎
     * 3. 合并所有搜索引擎的提交结果
     * 4. 返回综合统计信息
     * 
     * @return 批量提交结果，包含所有搜索引擎的统计
     */
    @Operation(summary = "批量提交到所有搜索引擎")
    @PostMapping("/submit/batch")
    @OperationLog(module = "SEO管理", type = "批量提交", description = "批量提交到所有搜索引擎")
    public Result<SubmissionResultVO> batchSubmitAll() {
        // 记录请求开始
        log.info("开始批量提交到所有搜索引擎");
        
        try {
            // 调用Service层执行批量提交
            SubmissionResultVO result = seoService.batchSubmitAll();
            
            // 记录成功结果
            log.info("批量提交到所有搜索引擎完成: success={}, totalSuccessCount={}, totalFailedCount={}", 
                result.isSuccess(), result.getSuccessCount(), result.getFailedCount());
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常
            log.error("批量提交到所有搜索引擎失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取提交历史
     * 
     * 业务逻辑：
     * 1. 验证分页参数的有效性
     * 2. 调用Service层查询SEO提交历史记录
     * 3. 按提交时间降序排列
     * 4. 返回分页结果
     * 
     * @param page 页码，默认为1
     * @param pageSize 页大小，默认为20
     * @return SEO提交历史记录分页结果
     */
    @Operation(summary = "获取提交历史")
    @GetMapping("/history")
    public Result<PageResult<SEOSubmission>> getSubmissionHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        // 记录请求开始
        log.info("开始获取SEO提交历史: page={}, pageSize={}", page, pageSize);
        
        try {
            // 步骤1：验证分页参数
            // 检查分页参数的合理性
            if (page == null || page < 1) {
                page = 1;
                log.debug("修正页码参数: page=1");
            }
            
            if (pageSize == null || pageSize < 1) {
                pageSize = 20;
                log.debug("修正页大小参数: pageSize=20");
            }
            
            // 步骤2：调用Service层查询提交历史
            PageResult<SEOSubmission> result = seoService.getSubmissionHistory(page, pageSize);
            
            // 记录成功结果
            log.info("SEO提交历史获取成功: total={}, records={}, page={}, pageSize={}", 
                result.getTotal(), result.getRecords().size(), page, pageSize);
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常
            log.error("SEO提交历史获取失败: page={}, pageSize={}, error={}", 
                page, pageSize, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 重新提交
     * 
     * 业务逻辑：
     * 1. 验证提交记录ID的有效性
     * 2. 查找原始提交记录
     * 3. 基于原始记录重新提交到搜索引擎
     * 4. 记录重新提交的结果
     * 
     * @param id 原始提交记录ID
     * @return 重新提交结果
     * @throws ResourceNotFoundException 当提交记录不存在时抛出
     */
    @Operation(summary = "重新提交")
    @PostMapping("/resubmit/{id}")
    @OperationLog(module = "SEO管理", type = "重新提交", description = "重新提交SEO记录")
    public Result<SubmissionResultVO> resubmit(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始重新提交SEO记录: id={}", id);
        
        try {
            // 步骤1：验证参数
            // 检查提交记录ID是否有效
            if (id == null || id <= 0) {
                log.warn("提交记录ID无效: id={}", id);
                throw new IllegalArgumentException("提交记录ID无效");
            }
            
            // 步骤2：调用Service层重新提交
            SubmissionResultVO result = seoService.resubmit(id);
            
            // 记录成功结果
            log.info("SEO记录重新提交完成: id={}, success={}, successCount={}, failedCount={}", 
                id, result.isSuccess(), result.getSuccessCount(), result.getFailedCount());
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常
            log.error("SEO记录重新提交失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 删除提交记录
     * 
     * 业务逻辑：
     * 1. 验证提交记录ID的有效性
     * 2. 检查提交记录是否存在
     * 3. 执行删除操作
     * 4. 记录删除结果
     * 
     * @param id 提交记录ID
     * @return 操作结果
     * @throws ResourceNotFoundException 当提交记录不存在时抛出
     */
    @Operation(summary = "删除提交记录")
    @DeleteMapping("/history/{id}")
    @OperationLog(module = "SEO管理", type = "删除", description = "删除SEO提交记录")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始删除SEO提交记录: id={}", id);
        
        try {
            // 步骤1：验证参数
            // 检查提交记录ID是否有效
            if (id == null || id <= 0) {
                log.warn("提交记录ID无效: id={}", id);
                throw new IllegalArgumentException("提交记录ID无效");
            }
            
            // 步骤2：调用Service层删除记录
            seoService.deleteHistory(id);
            
            // 记录成功结果
            log.info("SEO提交记录删除成功: id={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("SEO提交记录删除失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
}
