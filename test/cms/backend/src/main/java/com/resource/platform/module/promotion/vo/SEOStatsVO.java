package com.resource.platform.module.promotion.vo;

import lombok.Data;

@Data
public class SEOStatsVO {
    private Integer totalSubmissions;
    private Integer successSubmissions;
    private Integer failedSubmissions;
    private Integer todaySubmissions;
}
