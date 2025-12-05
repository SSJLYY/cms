package com.resource.platform.dto;

import lombok.Data;

@Data
public class ResourceQueryDTO {
    private String keyword;
    private Long categoryId;
    private Integer status;
    private String sortField;
    private String sortOrder;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
