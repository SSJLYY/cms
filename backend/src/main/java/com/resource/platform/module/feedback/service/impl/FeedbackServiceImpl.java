package com.resource.platform.module.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.module.feedback.dto.FeedbackQueryDTO;
import com.resource.platform.module.feedback.dto.FeedbackReplyDTO;
import com.resource.platform.module.feedback.dto.FeedbackStatusDTO;
import com.resource.platform.module.feedback.dto.FeedbackSubmitDTO;
import com.resource.platform.module.feedback.entity.Feedback;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.module.feedback.mapper.FeedbackMapper;
import com.resource.platform.module.feedback.service.FeedbackService;
import com.resource.platform.module.feedback.vo.FeedbackStatsVO;
import com.resource.platform.module.feedback.vo.FeedbackVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * 反馈服务实现类
 * 
 * 功能说明：
 * - 实现用户反馈的核心业务逻辑
 * - 处理反馈的提交、查询和管理
 * - 管理反馈的状态流转
 * - 处理反馈回复和邮件通知
 * - 提供反馈统计信息
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class FeedbackServiceImpl implements FeedbackService {

    /** 分页查询每页最大条数 */
    private static final int MAX_PAGE_SIZE = 100;
    
    @Autowired
    private FeedbackMapper feedbackMapper;
    
    @Autowired
    private com.resource.platform.module.system.service.EmailService emailService;
    
    // 日期时间格式化器
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 提交用户反馈
     * 
     * 业务逻辑：
     * 1. 将DTO数据复制到实体对象
     * 2. 设置反馈状态为待处理
     * 3. 设置删除标记为未删除
     * 4. 保存反馈到数据库
     * 
     * @param dto 反馈提交数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFeedback(FeedbackSubmitDTO dto) {
        // 记录提交开始
        log.info("开始提交用户反馈: type={}, title={}, contactEmail={}", 
            dto.getType(), dto.getTitle(), dto.getContactEmail());
        
        // 步骤1：创建反馈实体对象
        Feedback feedback = new Feedback();
        BeanUtils.copyProperties(dto, feedback);
        
        // 步骤2：设置默认状态
        feedback.setStatus("PENDING"); // 待处理状态
        feedback.setDeleted(0); // 未删除
        
        // 步骤3：保存到数据库
        log.debug("保存反馈到数据库: title={}", dto.getTitle());
        int rows = feedbackMapper.insert(feedback);
        
        // 记录提交成功
        log.info("用户反馈提交成功: feedbackId={}, type={}, title={}, affectedRows={}", 
            feedback.getId(), dto.getType(), dto.getTitle(), rows);
    }
    
    /**
     * 获取反馈统计信息
     * 
     * 业务逻辑：
     * 1. 统计总反馈数量（未删除）
     * 2. 统计待处理反馈数量
     * 3. 统计已处理反馈数量
     * 4. 统计今日反馈数量
     * 
     * @return 反馈统计信息
     */
    @Override
    public FeedbackStatsVO getStats() {
        log.info("开始获取反馈统计信息");

        // 一次 SQL 聚合查询替代原来 4 次 selectCount，降低数据库往返开销
        Map<String, Long> summary = feedbackMapper.selectStatsSummary();

        FeedbackStatsVO stats = new FeedbackStatsVO();
        stats.setTotalFeedback(toInt(summary.get("total")));
        stats.setPendingFeedback(toInt(summary.get("pending")));
        stats.setProcessedFeedback(toInt(summary.get("processed")));
        stats.setTodayFeedback(toInt(summary.get("today")));

        log.info("获取反馈统计信息成功: total={}, pending={}, processed={}, today={}",
            stats.getTotalFeedback(), stats.getPendingFeedback(),
            stats.getProcessedFeedback(), stats.getTodayFeedback());

        return stats;
    }

    /** 安全转换 Long → int（null 视为 0） */
    private static int toInt(Long value) {
        return value == null ? 0 : value.intValue();
    }

    
    @Override
    public PageResult<FeedbackVO> getFeedbackList(FeedbackQueryDTO query) {
        long safePageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1L : query.getPageNum();
        int safePageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : Math.min(query.getPageSize(), MAX_PAGE_SIZE);
        Page<Feedback> page = new Page<>(safePageNum, safePageSize);
        
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getDeleted, 0);
        
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Feedback::getStatus, query.getStatus());
        }
        
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(Feedback::getType, query.getType());
        }
        
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Feedback::getTitle, query.getKeyword())
                    .or().like(Feedback::getContent, query.getKeyword()));
        }

        if (Boolean.TRUE.equals(query.getUnreplied())) {
            wrapper.and(w -> w.isNull(Feedback::getReply)
                    .or()
                    .eq(Feedback::getReply, ""));
        }
        
        wrapper.orderByDesc(Feedback::getCreateTime);
        
        IPage<Feedback> pageResult = feedbackMapper.selectPage(page, wrapper);
        
        return PageResult.of(pageResult, this::convertToVO);
    }
    
    @Override
    public FeedbackVO getFeedbackDetail(Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null || Integer.valueOf(1).equals(feedback.getDeleted())) {
            throw new BusinessException("反馈不存在");
        }
        return convertToVO(feedback);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyFeedback(FeedbackReplyDTO dto) {
        log.info("处理反馈回复: feedbackId={}", dto.getId());

        Feedback feedback = feedbackMapper.selectById(dto.getId());
        if (feedback == null || Integer.valueOf(1).equals(feedback.getDeleted())) {
            throw new BusinessException("反馈不存在");
        }

        log.debug("找到反馈记录: id={}, contactEmail={}", feedback.getId(), feedback.getContactEmail());

        // 先发送邮件，成功后再更新状态
        boolean emailSent = false;
        if (StringUtils.hasText(feedback.getContactEmail())) {
            emailSent = sendReplyEmail(feedback, dto.getReply());
            log.info("邮件发送结果: feedbackId={}, result={}", dto.getId(), emailSent ? "成功" : "失败");
        } else {
            emailSent = true; // 没有邮箱也认为成功
        }

        // 只有邮件发送成功才更新状态
        if (emailSent) {
            feedback.setReply(dto.getReply());
            feedback.setReplyTime(java.time.LocalDateTime.now());
            feedback.setStatus("COMPLETED");
            int rows = feedbackMapper.updateById(feedback);
            if (rows <= 0) {
                throw new BusinessException("更新反馈回复状态失败");
            }
            log.info("反馈回复处理完成: feedbackId={}", dto.getId());
        } else {
            throw new BusinessException("邮件发送失败，请检查邮件配置");
        }
    }
    
    private boolean sendReplyEmail(Feedback feedback, String replyContent) {
        try {
            log.info("邮件发送开始: 收件人={}", feedback.getContactEmail());

            String subject = "【资源下载平台】您的反馈已收到回复";
            log.debug("邮件主题: {}", subject);

            log.debug("开始构建邮件内容...");
            // 临时设置reply用于构建邮件内容
            String originalReply = feedback.getReply();
            feedback.setReply(replyContent);
            String htmlContent = buildReplyEmailContent(feedback);
            feedback.setReply(originalReply); // 恢复原值
            log.debug("邮件内容构建完成，长度: {}", htmlContent.length());

            log.debug("调用邮件服务发送邮件...");
            emailService.sendHtmlEmail(feedback.getContactEmail(), subject, htmlContent);
            log.info("邮件发送成功: 收件人={}", feedback.getContactEmail());
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败: 收件人={}, 错误类型={}, 错误信息={}",
                feedback.getContactEmail(), e.getClass().getName(), e.getMessage(), e);
            return false;
        }
    }
    
    private String buildReplyEmailContent(Feedback feedback) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head><meta charset='UTF-8'></head>");
        html.append("<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        html.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>");
        html.append("<h2 style='color: #409eff; border-bottom: 2px solid #409eff; padding-bottom: 10px;'>反馈回复通知</h2>");
        
        if (StringUtils.hasText(feedback.getContactName())) {
            html.append("<p>尊敬的 ").append(feedback.getContactName()).append("：</p>");
        } else {
            html.append("<p>您好：</p>");
        }
        
        html.append("<p>感谢您的反馈！我们已经对您的反馈进行了处理，以下是回复内容：</p>");
        html.append("<div style='background-color: #f5f5f5; padding: 15px; border-left: 4px solid #409eff; margin: 20px 0;'>");
        html.append("<h3 style='margin-top: 0; color: #666;'>您的反馈：</h3>");
        html.append("<p style='white-space: pre-wrap;'>").append(feedback.getContent()).append("</p>");
        html.append("</div>");
        html.append("<div style='background-color: #e8f4fd; padding: 15px; border-left: 4px solid #67c23a; margin: 20px 0;'>");
        html.append("<h3 style='margin-top: 0; color: #67c23a;'>我们的回复：</h3>");
        html.append("<p style='white-space: pre-wrap;'>").append(feedback.getReply()).append("</p>");
        html.append("</div>");
        html.append("<p style='color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #ddd; padding-top: 15px;'>");
        html.append("此邮件由系统自动发送，请勿直接回复。<br>");
        html.append("如有其他问题，欢迎继续向我们反馈。");
        html.append("</p>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        return html.toString();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(FeedbackStatusDTO dto) {
        Feedback feedback = feedbackMapper.selectById(dto.getId());
        if (feedback == null || Integer.valueOf(1).equals(feedback.getDeleted())) {
            throw new BusinessException("反馈不存在");
        }
        
        feedback.setStatus(dto.getStatus());
        int rows = feedbackMapper.updateById(feedback);
        if (rows <= 0) {
            throw new BusinessException("更新反馈状态失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeedback(Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null || Integer.valueOf(1).equals(feedback.getDeleted())) {
            throw new BusinessException("反馈不存在");
        }
        
        feedback.setDeleted(1);
        int rows = feedbackMapper.updateById(feedback);
        if (rows <= 0) {
            throw new BusinessException("删除反馈失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("反馈ID列表不能为空");
        }

        List<Long> invalidIds = ids.stream()
                .filter(id -> id == null || id <= 0)
                .collect(Collectors.toList());
        if (!invalidIds.isEmpty()) {
            throw new BusinessException("包含无效的反馈ID: " + invalidIds);
        }

        List<Feedback> feedbackList = feedbackMapper.selectBatchIds(ids);
        List<Long> existingIds = feedbackList.stream()
                .map(Feedback::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Long> missingIds = ids.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            throw new BusinessException("部分反馈不存在: " + missingIds);
        }

        List<Long> deletedIds = feedbackList.stream()
                .filter(feedback -> Integer.valueOf(1).equals(feedback.getDeleted()))
                .map(Feedback::getId)
                .collect(Collectors.toList());
        if (!deletedIds.isEmpty()) {
            throw new BusinessException("部分反馈已删除: " + deletedIds);
        }

        List<Long> failedIds = new ArrayList<>();
        for (Long id : ids) {
            try {
                deleteFeedback(id);
            } catch (RuntimeException ex) {
                failedIds.add(id);
                throw ex;
            }
        }

        if (!failedIds.isEmpty()) {
            throw new BusinessException("批量删除反馈存在失败记录: " + failedIds);
        }
    }
    
    private FeedbackVO convertToVO(Feedback feedback) {
        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        if (feedback.getCreateTime() != null) {
            vo.setCreateTime(feedback.getCreateTime().format(FORMATTER));
        }
        if (feedback.getUpdateTime() != null) {
            vo.setUpdateTime(feedback.getUpdateTime().format(FORMATTER));
        }
        if (feedback.getReplyTime() != null) {
            vo.setReplyTime(feedback.getReplyTime().format(FORMATTER));
        }
        return vo;
    }
}
