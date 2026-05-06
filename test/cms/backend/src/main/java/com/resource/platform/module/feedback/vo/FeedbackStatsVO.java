package com.resource.platform.module.feedback.vo;

import lombok.Data;

@Data
public class FeedbackStatsVO {
    private Integer totalFeedback;
    private Integer pendingFeedback;
    private Integer processedFeedback;
    private Integer todayFeedback;
}
