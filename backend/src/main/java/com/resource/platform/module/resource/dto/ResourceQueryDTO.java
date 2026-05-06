package com.resource.platform.module.resource.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

/**
 * 资源查询条件 DTO
 */
@Data
public class ResourceQueryDTO {

    /** 搜索关键词（标题/描述模糊匹配） */
    private String keyword;

    /** 分类ID筛选 */
    private Long categoryId;

    /** 资源状态：0-已下架，1-已发布 */
    private Integer status;

    @Pattern(regexp = "^(crawler|manual)$", message = "来源只能为 crawler 或 manual")
    private String source;

    /** 排序字段（安全列名） */
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "排序字段不合法")
    private String sortField;

    /** 排序方向：asc / desc */
    @Pattern(regexp = "^(asc|desc)$", message = "排序方向只能为 asc 或 desc")
    private String sortOrder;

    /** 当前页码（从1开始） */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize = 10;
}
