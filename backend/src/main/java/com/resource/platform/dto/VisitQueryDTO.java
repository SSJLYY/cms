package com.resource.platform.dto;

import lombok.Data;

@Data
public class VisitQueryDTO {
    private String period = "today";
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
