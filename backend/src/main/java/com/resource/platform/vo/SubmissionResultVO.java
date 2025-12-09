package com.resource.platform.vo;

import lombok.Data;

@Data
public class SubmissionResultVO {
    private Boolean success;
    private String message;
    private Integer successCount;
    private Integer failedCount;
    
    // 为了兼容性，添加isSuccess()方法
    public Boolean isSuccess() {
        return success;
    }
}
