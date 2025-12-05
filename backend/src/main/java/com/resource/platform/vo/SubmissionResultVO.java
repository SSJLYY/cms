package com.resource.platform.vo;

import lombok.Data;

@Data
public class SubmissionResultVO {
    private Boolean success;
    private String message;
    private Integer successCount;
    private Integer failedCount;
}
