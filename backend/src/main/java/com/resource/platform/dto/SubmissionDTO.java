package com.resource.platform.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class SubmissionDTO {
    @NotBlank(message = "搜索引擎不能为空")
    private String engine;
    
    @NotEmpty(message = "URL列表不能为空")
    private List<String> urls;
}
