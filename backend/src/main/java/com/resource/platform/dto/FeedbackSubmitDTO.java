package com.resource.platform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 提交反馈DTO
 */
@Data
public class FeedbackSubmitDTO {
    
    /**
     * 反馈类型
     */
    @NotNull(message = "反馈类型不能为空")
    private String type;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    @NotBlank(message = "反馈内容不能为空")
    private String content;
    
    /**
     * 联系人姓名
     */
    private String contactName;
    
    /**
     * 联系邮箱
     */
    private String contactEmail;
}
