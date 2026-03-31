package com.resource.platform.module.category.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
