package com.resource.platform.module.promotion.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.BizErrorCode;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.common.Result;
import com.resource.platform.module.promotion.entity.SEOSubmission;
import com.resource.platform.module.promotion.service.SEOService;
import com.resource.platform.module.promotion.vo.SEOStatsVO;
import com.resource.platform.module.promotion.vo.SubmissionResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * SEO管理控制器
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

    @Operation(summary = "获取SEO统计")
    @GetMapping("/statistics")
    public Result<SEOStatsVO> getStatistics() {
        SEOStatsVO stats = seoService.getStats();
        return Result.success(stats);
    }

    @Operation(summary = "生成网站地图")
    @PostMapping("/sitemap/generate")
    @OperationLog(module = "SEO管理", type = "生成", description = "生成网站地图")
    public Result<String> generateSitemap() {
        String sitemap = seoService.generateSitemap();
        return Result.success(sitemap);
    }

    @Operation(summary = "提交到百度")
    @PostMapping("/submit/baidu")
    @OperationLog(module = "SEO管理", type = "提交", description = "提交到百度搜索引擎")
    public Result<SubmissionResultVO> submitToBaidu() {
        SubmissionResultVO result = seoService.batchSubmit("baidu");
        return Result.success(result);
    }

    @Operation(summary = "提交到必应")
    @PostMapping("/submit/bing")
    @OperationLog(module = "SEO管理", type = "提交", description = "提交到必应搜索引擎")
    public Result<SubmissionResultVO> submitToBing() {
        SubmissionResultVO result = seoService.batchSubmit("bing");
        return Result.success(result);
    }

    @Operation(summary = "批量提交到所有搜索引擎")
    @PostMapping("/submit/batch")
    @OperationLog(module = "SEO管理", type = "批量提交", description = "批量提交到所有搜索引擎")
    public Result<SubmissionResultVO> batchSubmitAll() {
        SubmissionResultVO result = seoService.batchSubmitAll();
        return Result.success(result);
    }

    @Operation(summary = "获取提交历史")
    @GetMapping("/history")
    public Result<PageResult<SEOSubmission>> getSubmissionHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        PageResult<SEOSubmission> result = seoService.getSubmissionHistory(page, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "重新提交")
    @PostMapping("/resubmit/{id}")
    @OperationLog(module = "SEO管理", type = "重新提交", description = "重新提交SEO记录")
    public Result<SubmissionResultVO> resubmit(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.warn("提交记录ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "提交记录ID无效");
        }

        SubmissionResultVO result = seoService.resubmit(id);
        return Result.success(result);
    }

    @Operation(summary = "删除提交记录")
    @DeleteMapping("/history/{id}")
    @OperationLog(module = "SEO管理", type = "删除", description = "删除SEO提交记录")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.warn("提交记录ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "提交记录ID无效");
        }

        seoService.deleteHistory(id);
        return Result.success();
    }
}
