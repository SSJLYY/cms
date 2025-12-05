package com.resource.platform.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FeedbackStatusDTO {
    private Long id;
    
    @NotBlank(message = "状态不能为空")
    private String status;
}
