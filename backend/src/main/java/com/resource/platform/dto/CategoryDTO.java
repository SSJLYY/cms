package com.resource.platform.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class CategoryDTO {
    @NotBlank(message = "分类名称不能为空")
    private String name;
}
