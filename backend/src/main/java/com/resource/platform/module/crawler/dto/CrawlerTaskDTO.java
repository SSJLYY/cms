package com.resource.platform.module.crawler.dto;

import com.resource.platform.module.category.dto.CategoryMappingItem;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class CrawlerTaskDTO {

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "目标URL不能为空")
    @Size(max = 500, message = "URL长度不能超过500个字符")
    @Pattern(regexp = "^https?://.*", message = "URL格式不正确，必须以 http:// 或 https:// 开头")
    private String targetUrl;

    @NotNull(message = "状态不能为空")
    private Boolean status;

    @NotNull(message = "抓取间隔不能为空")
    @Min(value = 1, message = "抓取间隔最小为1小时")
    @Max(value = 168, message = "抓取间隔最大为168小时")
    private Integer crawlInterval;

    @NotNull(message = "抓取深度不能为空")
    @Min(value = 1, message = "抓取深度最小为1层")
    @Max(value = 5, message = "抓取深度最大为5层")
    private Integer maxDepth;

    private CategoryMappingItem[] categoryMapping;

    @NotNull(message = "智能模式不能为空")
    private Boolean intelligentMode;

    private CustomRules customRules;
}
