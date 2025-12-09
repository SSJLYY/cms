package com.resource.platform.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CategoryMappingItem {
    
    @NotBlank(message = "分类键不能为空")
    private String key;
    
    @NotNull(message = "分类值不能为空")
    private Long value;
    
    public CategoryMappingItem() {}
    
    public CategoryMappingItem(String key, Long value) {
        this.key = key;
        this.value = value;
    }
}