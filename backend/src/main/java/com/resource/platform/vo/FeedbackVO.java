package com.resource.platform.vo;

import lombok.Data;

@Data
public class FeedbackVO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private String contactName;
    private String contactEmail;
    private String status;
    private String reply;
    private String replyTime;
    private String createTime;
    private String updateTime;
}
