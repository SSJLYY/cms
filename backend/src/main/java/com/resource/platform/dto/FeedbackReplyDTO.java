package com.resource.platform.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FeedbackReplyDTO {
    private Long id;
    
    @NotBlank(message = "回复内容不能为空")
    private String reply;
}
