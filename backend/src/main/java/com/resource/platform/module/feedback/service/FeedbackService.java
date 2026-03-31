package com.resource.platform.module.feedback.service;

import com.resource.platform.common.PageResult;
import com.resource.platform.module.feedback.dto.FeedbackQueryDTO;
import com.resource.platform.module.feedback.dto.FeedbackReplyDTO;
import com.resource.platform.module.feedback.dto.FeedbackStatusDTO;
import com.resource.platform.module.feedback.dto.FeedbackSubmitDTO;
import com.resource.platform.module.feedback.vo.FeedbackStatsVO;
import com.resource.platform.module.feedback.vo.FeedbackVO;

import java.util.List;

public interface FeedbackService {
    /**
     * 提交反馈（公开接口）
     */
    void submitFeedback(FeedbackSubmitDTO dto);
    
    /**
     * 获取反馈统计
     */
    FeedbackStatsVO getStats();
    
    /**
     * 获取反馈列表
     */
    PageResult<FeedbackVO> getFeedbackList(FeedbackQueryDTO query);
    
    /**
     * 获取反馈详情
     */
    FeedbackVO getFeedbackDetail(Long id);
    
    /**
     * 回复反馈
     */
    void replyFeedback(FeedbackReplyDTO dto);
    
    /**
     * 修改反馈状态
     */
    void updateStatus(FeedbackStatusDTO dto);
    
    /**
     * 删除反馈
     */
    void deleteFeedback(Long id);
    
    /**
     * 批量删除反馈
     */
    void batchDelete(List<Long> ids);
}
