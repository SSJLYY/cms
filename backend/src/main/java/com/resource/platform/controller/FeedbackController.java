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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 反馈管理控制器
 * 
 * 功能说明：
 * - 提供用户反馈提交接口（公开接口）
 * - 提供反馈的查询和管理接口
 * - 支持反馈的回复和状态更新
 * - 提供反馈统计信息接口
 * - 支持反馈的删除和批量删除
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "反馈管理")
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    
    @Autowired
    private FeedbackService feedbackService;
    
    /**
     * 提交反馈（公开接口）
     * 
     * 业务逻辑：
     * 1. 验证反馈数据的完整性
     * 2. 设置反馈状态为待处理
     * 3. 保存反馈到数据库
     * 
     * @param dto 反馈提交数据
     * @return 提交成功响应
     */
    @Operation(summary = "提交反馈（公开接口）")
    @PostMapping("/public/submit")
    public Result<Void> submitFeedback(@Validated @RequestBody FeedbackSubmitDTO dto) {
        // 记录反馈提交开始
        log.info("开始处理用户反馈提交: type={}, title={}, contactEmail={}", 
            dto.getType(), dto.getTitle(), dto.getContactEmail());
        
        try {
            // 调用服务层提交反馈
            feedbackService.submitFeedback(dto);
            
            // 记录提交成功
            log.info("用户反馈提交成功: type={}, title={}", dto.getType(), dto.getTitle());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录提交失败
            log.error("用户反馈提交失败: type={}, title={}, error={}", 
                dto.getType(), dto.getTitle(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取反馈统计
     * 
     * 业务逻辑：
     * 1. 统计总反馈数量
     * 2. 统计待处理反馈数量
     * 3. 统计已处理反馈数量
     * 4. 统计今日反馈数量
     * 
     * @return 反馈统计信息
     */
    @Operation(summary = "获取反馈统计")
    @GetMapping("/statistics")
    public Result<FeedbackStatsVO> getStats() {
        // 记录统计请求开始
        log.info("开始获取反馈统计信息");
        
        try {
            // 调用服务层获取统计
            FeedbackStatsVO stats = feedbackService.getStats();
            
            // 记录统计成功
            log.info("获取反馈统计信息成功: total={}, pending={}, processed={}, today={}", 
                stats.getTotalFeedback(), stats.getPendingFeedback(), 
                stats.getProcessedFeedback(), stats.getTodayFeedback());
            
            return Result.success(stats);
            
        } catch (Exception e) {
            // 记录统计失败
            log.error("获取反馈统计信息失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 查询反馈列表
     * 
     * 业务逻辑：
     * 1. 接收查询条件（状态、类型、关键词等）
     * 2. 执行分页查询
     * 3. 转换为VO对象列表
     * 4. 返回分页结果
     * 
     * @param query 查询条件
     * @return 反馈分页结果
     */
    @Operation(summary = "查询反馈列表")
    @PostMapping("/query")
    public Result<PageResult<FeedbackVO>> getFeedbackList(@RequestBody FeedbackQueryDTO query) {
        // 记录查询请求开始
        log.info("开始查询反馈列表: page={}, pageSize={}, status={}, type={}, keyword={}", 
            query.getPageNum(), query.getPageSize(), query.getStatus(), query.getType(), query.getKeyword());
        
        try {
            // 调用服务层执行查询
            PageResult<FeedbackVO> result = feedbackService.getFeedbackList(query);
            
            // 记录查询成功
            log.info("查询反馈列表成功: total={}, records={}", 
                result.getTotal(), result.getRecords().size());
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("查询反馈列表失败: query={}, error={}", query, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取反馈详情
     * 
     * 业务逻辑：
     * 1. 根据ID查询反馈信息
     * 2. 验证反馈是否存在
     * 3. 转换为VO对象
     * 4. 返回反馈详情
     * 
     * @param id 反馈ID
     * @return 反馈详情
     * @throws BusinessException 当反馈不存在时抛出
     */
    @Operation(summary = "获取反馈详情")
    @GetMapping("/{id}")
    public Result<FeedbackVO> getFeedbackDetail(@PathVariable Long id) {
        // 记录查询请求开始
        log.info("开始获取反馈详情: feedbackId={}", id);
        
        try {
            // 调用服务层查询详情
            FeedbackVO feedback = feedbackService.getFeedbackDetail(id);
            
            // 记录查询成功
            log.info("获取反馈详情成功: feedbackId={}, title={}, status={}", 
                id, feedback.getTitle(), feedback.getStatus());
            
            return Result.success(feedback);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("获取反馈详情失败: feedbackId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 回复反馈
     * 
     * 业务逻辑：
     * 1. 验证反馈是否存在
     * 2. 发送回复邮件给用户
     * 3. 更新反馈状态为已完成
     * 4. 记录回复时间和内容
     * 
     * @param id 反馈ID
     * @param dto 回复内容
     * @return 回复成功响应
     * @throws BusinessException 当反馈不存在或邮件发送失败时抛出
     */
    @Operation(summary = "回复反馈")
    @PostMapping("/{id}/reply")
    public Result<Void> replyFeedback(@PathVariable Long id, @RequestBody FeedbackReplyDTO dto) {
        // 记录回复请求开始
        log.info("开始回复反馈: feedbackId={}, replyLength={}", id, 
            dto.getReply() != null ? dto.getReply().length() : 0);
        
        try {
            // 设置反馈ID
            dto.setId(id);
            
            // 调用服务层回复反馈
            feedbackService.replyFeedback(dto);
            
            // 记录回复成功
            log.info("回复反馈成功: feedbackId={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录回复失败
            log.error("回复反馈失败: feedbackId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 修改反馈状态
     * 
     * 业务逻辑：
     * 1. 验证反馈是否存在
     * 2. 更新反馈状态
     * 3. 保存状态变更
     * 
     * @param id 反馈ID
     * @param dto 状态更新数据
     * @return 更新成功响应
     * @throws BusinessException 当反馈不存在时抛出
     */
    @Operation(summary = "修改反馈状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody FeedbackStatusDTO dto) {
        // 记录状态更新请求开始
        log.info("开始更新反馈状态: feedbackId={}, newStatus={}", id, dto.getStatus());
        
        try {
            // 设置反馈ID
            dto.setId(id);
            
            // 调用服务层更新状态
            feedbackService.updateStatus(dto);
            
            // 记录更新成功
            log.info("更新反馈状态成功: feedbackId={}, newStatus={}", id, dto.getStatus());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录更新失败
            log.error("更新反馈状态失败: feedbackId={}, newStatus={}, error={}", 
                id, dto.getStatus(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 删除反馈
     * 
     * 业务逻辑：
     * 1. 验证反馈是否存在
     * 2. 逻辑删除反馈（设置deleted=1）
     * 
     * @param id 反馈ID
     * @return 删除成功响应
     * @throws BusinessException 当反馈不存在时抛出
     */
    @Operation(summary = "删除反馈")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFeedback(@PathVariable Long id) {
        // 记录删除请求开始
        log.info("开始删除反馈: feedbackId={}", id);
        
        try {
            // 调用服务层删除反馈
            feedbackService.deleteFeedback(id);
            
            // 记录删除成功
            log.info("删除反馈成功: feedbackId={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录删除失败
            log.error("删除反馈失败: feedbackId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 批量删除反馈
     * 
     * 业务逻辑：
     * 1. 遍历反馈ID列表
     * 2. 逐个调用删除方法
     * 3. 记录删除结果
     * 
     * @param ids 反馈ID列表
     * @return 批量删除成功响应
     */
    @Operation(summary = "批量删除反馈")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        // 记录批量删除请求开始
        log.info("开始批量删除反馈: count={}, ids={}", ids.size(), ids);
        
        try {
            // 调用服务层批量删除
            feedbackService.batchDelete(ids);
            
            // 记录批量删除成功
            log.info("批量删除反馈成功: count={}", ids.size());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录批量删除失败
            log.error("批量删除反馈失败: ids={}, error={}", ids, e.getMessage(), e);
            throw e;
        }
    }
}
