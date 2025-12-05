package com.resource.platform.controller;

import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.FeedbackQueryDTO;
import com.resource.platform.dto.FeedbackReplyDTO;
import com.resource.platform.dto.FeedbackStatusDTO;
import com.resource.platform.dto.FeedbackSubmitDTO;
import com.resource.platform.service.FeedbackService;
import com.resource.platform.vo.FeedbackStatsVO;
import com.resource.platform.vo.FeedbackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "反馈管理")
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    
    @Autowired
    private FeedbackService feedbackService;
    
    @Operation(summary = "提交反馈（公开接口）")
    @PostMapping("/public/submit")
    public Result<Void> submitFeedback(@Validated @RequestBody FeedbackSubmitDTO dto) {
        feedbackService.submitFeedback(dto);
        return Result.success();
    }
    
    @Operation(summary = "获取反馈统计")
    @GetMapping("/statistics")
    public Result<FeedbackStatsVO> getStats() {
        return Result.success(feedbackService.getStats());
    }
    
    @Operation(summary = "查询反馈列表")
    @PostMapping("/query")
    public Result<PageResult<FeedbackVO>> getFeedbackList(@RequestBody FeedbackQueryDTO query) {
        return Result.success(feedbackService.getFeedbackList(query));
    }
    
    @Operation(summary = "获取反馈详情")
    @GetMapping("/{id}")
    public Result<FeedbackVO> getFeedbackDetail(@PathVariable Long id) {
        return Result.success(feedbackService.getFeedbackDetail(id));
    }
    
    @Operation(summary = "回复反馈")
    @PostMapping("/{id}/reply")
    public Result<Void> replyFeedback(@PathVariable Long id, @RequestBody FeedbackReplyDTO dto) {
        dto.setId(id);
        feedbackService.replyFeedback(dto);
        return Result.success();
    }
    
    @Operation(summary = "修改反馈状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody FeedbackStatusDTO dto) {
        dto.setId(id);
        feedbackService.updateStatus(dto);
        return Result.success();
    }
    
    @Operation(summary = "删除反馈")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return Result.success();
    }
    
    @Operation(summary = "批量删除反馈")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        feedbackService.batchDelete(ids);
        return Result.success();
    }
}
