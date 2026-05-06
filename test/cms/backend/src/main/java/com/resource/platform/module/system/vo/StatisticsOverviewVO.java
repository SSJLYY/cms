package com.resource.platform.module.system.vo;

import lombok.Data;

@Data
public class StatisticsOverviewVO {
    private Integer totalDownloads;
    private Integer totalVisits;
    private Integer newVisits;
    private String period;
}
