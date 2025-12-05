package com.resource.platform.controller;

import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.SubmissionDTO;
import com.resource.platform.entity.SEOSubmission;
import com.resource.platform.service.SEOService;
import com.resource.platform.vo.SEOStatsVO;
import com.resource.platform.vo.SubmissionResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "SEO管理")
@RestController
@RequestMapping("/api/seo")
public class SEOController {
    
    @Autowired
    private SEOService seoService;
    
    @Operation(summary = "获取SEO统计")
    @GetMapping("/statistics")
    public Result<SEOStatsVO> getStatistics() {
        return Result.success(seoService.getStats());
    }
    
    @Operation(summary = "生成网站地图")
    @PostMapping("/sitemap/generate")
    public Result<String> generateSitemap() {
        return Result.success(seoService.generateSitemap());
    }
    
    @Operation(summary = "提交到百度")
    @PostMapping("/submit/baidu")
    public Result<SubmissionResultVO> submitToBaidu() {
        return Result.success(seoService.batchSubmit("baidu"));
    }
    
    @Operation(summary = "提交到必应")
    @PostMapping("/submit/bing")
    public Result<SubmissionResultVO> submitToBing() {
        return Result.success(seoService.batchSubmit("bing"));
    }
    
    @Operation(summary = "批量提交到所有搜索引擎")
    @PostMapping("/submit/batch")
    public Result<SubmissionResultVO> batchSubmitAll() {
        return Result.success(seoService.batchSubmitAll());
    }
    
    @Operation(summary = "获取提交历史")
    @GetMapping("/history")
    public Result<PageResult<SEOSubmission>> getSubmissionHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(seoService.getSubmissionHistory(page, pageSize));
    }
    
    @Operation(summary = "重新提交")
    @PostMapping("/resubmit/{id}")
    public Result<SubmissionResultVO> resubmit(@PathVariable Long id) {
        return Result.success(seoService.resubmit(id));
    }
    
    @Operation(summary = "删除提交记录")
    @DeleteMapping("/history/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        seoService.deleteHistory(id);
        return Result.success();
    }
}
