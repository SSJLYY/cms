package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.dto.FeedbackQueryDTO;
import com.resource.platform.dto.FeedbackReplyDTO;
import com.resource.platform.dto.FeedbackStatusDTO;
import com.resource.platform.dto.FeedbackSubmitDTO;
import com.resource.platform.entity.Feedback;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.mapper.FeedbackMapper;
import com.resource.platform.service.FeedbackService;
import com.resource.platform.vo.FeedbackStatsVO;
import com.resource.platform.vo.FeedbackVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    
    @Autowired
    private FeedbackMapper feedbackMapper;
    
    @Autowired
    private com.resource.platform.service.EmailService emailService;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void submitFeedback(FeedbackSubmitDTO dto) {
        Feedback feedback = new Feedback();
        BeanUtils.copyProperties(dto, feedback);
        feedback.setStatus("PENDING");
        feedback.setDeleted(0);
        feedbackMapper.insert(feedback);
    }
    
    @Override
    public FeedbackStatsVO getStats() {
        FeedbackStatsVO stats = new FeedbackStatsVO();
        
        // 总反馈数
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getDeleted, 0);
        stats.setTotalFeedback(feedbackMapper.selectCount(wrapper).intValue());
        
        // 待处理（PENDING状态）
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getStatus, "PENDING").eq(Feedback::getDeleted, 0);
        stats.setPendingFeedback(feedbackMapper.selectCount(wrapper).intValue());
        
        // 已处理（COMPLETED和CLOSED状态）
        wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Feedback::getStatus, "COMPLETED", "CLOSED").eq(Feedback::getDeleted, 0);
        stats.setProcessedFeedback(feedbackMapper.selectCount(wrapper).intValue());
        
        // 今日反馈
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getDeleted, 0)
               .ge(Feedback::getCreateTime, java.time.LocalDate.now().atStartOfDay());
        stats.setTodayFeedback(feedbackMapper.selectCount(wrapper).intValue());
        
        return stats;
    }
    
    @Override
    public PageResult<FeedbackVO> getFeedbackList(FeedbackQueryDTO query) {
        Page<Feedback> page = new Page<>(query.getPageNum(), query.getPageSize());
        
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
        
        wrapper.orderByDesc(Feedback::getCreateTime);
        
        IPage<Feedback> pageResult = feedbackMapper.selectPage(page, wrapper);
        
        List<FeedbackVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(pageResult.getTotal(), voList);
    }
    
    @Override
    public FeedbackVO getFeedbackDetail(Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null || feedback.getDeleted() == 1) {
            throw new BusinessException("反馈不存在");
        }
        return convertToVO(feedback);
    }
    
    @Override
    public void replyFeedback(FeedbackReplyDTO dto) {
        System.out.println("========== 开始处理反馈回复 ==========");
        System.out.println("反馈ID: " + dto.getId());
        System.out.println("回复内容: " + dto.getReply());
        
        Feedback feedback = feedbackMapper.selectById(dto.getId());
        if (feedback == null || feedback.getDeleted() == 1) {
            System.out.println("反馈不存在或已删除");
            throw new BusinessException("反馈不存在");
        }
        
        System.out.println("找到反馈记录: " + feedback.getId());
        System.out.println("联系邮箱: " + feedback.getContactEmail());
        
        // 先发送邮件，成功后再更新状态
        boolean emailSent = false;
        if (StringUtils.hasText(feedback.getContactEmail())) {
            System.out.println("准备发送邮件...");
            emailSent = sendReplyEmail(feedback, dto.getReply());
            System.out.println("邮件发送结果: " + (emailSent ? "成功" : "失败"));
        } else {
            System.out.println("没有联系邮箱，跳过邮件发送");
            emailSent = true; // 没有邮箱也认为成功
        }
        
        // 只有邮件发送成功才更新状态
        if (emailSent) {
            System.out.println("开始更新反馈状态...");
            feedback.setReply(dto.getReply());
            feedback.setReplyTime(java.time.LocalDateTime.now());
            feedback.setStatus("COMPLETED");
            int updateResult = feedbackMapper.updateById(feedback);
            System.out.println("数据库更新结果: " + updateResult);
            System.out.println("========== 反馈回复处理完成 ==========");
        } else {
            System.out.println("邮件发送失败，不更新反馈状态");
            System.out.println("========== 反馈回复处理失败 ==========");
            throw new BusinessException("邮件发送失败，请检查邮件配置");
        }
    }
    
    private boolean sendReplyEmail(Feedback feedback, String replyContent) {
        try {
            System.out.println("---------- 邮件发送开始 ----------");
            System.out.println("收件人: " + feedback.getContactEmail());
            
            String subject = "【资源下载平台】您的反馈已收到回复";
            System.out.println("邮件主题: " + subject);
            
            System.out.println("开始构建邮件内容...");
            // 临时设置reply用于构建邮件内容
            String originalReply = feedback.getReply();
            feedback.setReply(replyContent);
            String htmlContent = buildReplyEmailContent(feedback);
            feedback.setReply(originalReply); // 恢复原值
            System.out.println("邮件内容构建完成，长度: " + htmlContent.length());
            
            System.out.println("调用邮件服务发送邮件...");
            emailService.sendHtmlEmail(feedback.getContactEmail(), subject, htmlContent);
            System.out.println("邮件服务调用完成");
            System.out.println("---------- 邮件发送成功 ----------");
            return true;
        } catch (Exception e) {
            System.err.println("---------- 邮件发送失败 ----------");
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            System.err.println("---------- 邮件发送异常结束 ----------");
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
    public void updateStatus(FeedbackStatusDTO dto) {
        Feedback feedback = feedbackMapper.selectById(dto.getId());
        if (feedback == null || feedback.getDeleted() == 1) {
            throw new BusinessException("反馈不存在");
        }
        
        feedback.setStatus(dto.getStatus());
        feedbackMapper.updateById(feedback);
    }
    
    @Override
    public void deleteFeedback(Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        
        feedback.setDeleted(1);
        feedbackMapper.updateById(feedback);
    }
    
    @Override
    public void batchDelete(List<Long> ids) {
        ids.forEach(this::deleteFeedback);
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
