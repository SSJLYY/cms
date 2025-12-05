package com.resource.platform.vo;

import lombok.Data;

@Data
public class FeedbackStatsVO {
    private Integer totalFeedback;
    private Integer pendingFeedback;
    private Integer processedFeedback;
    private Integer todayFeedback;
}
