package com.resource.platform.vo;

import lombok.Data;

@Data
public class CrawlerStatistics {
    
    private Integer totalCrawled;
    
    private Integer totalSuccess;
    
    private Integer totalFailed;
    
    private Double successRate;
    
    public CrawlerStatistics() {
    }
    
    public CrawlerStatistics(Integer totalCrawled, Integer totalSuccess, Integer totalFailed) {
        this.totalCrawled = totalCrawled;
        this.totalSuccess = totalSuccess;
        this.totalFailed = totalFailed;
        this.successRate = calculateSuccessRate();
    }
    
    private Double calculateSuccessRate() {
        if (totalCrawled == null || totalCrawled == 0) {
            return 0.0;
        }
        if (totalSuccess == null) {
            return 0.0;
        }
        return Math.round(totalSuccess * 10000.0 / totalCrawled) / 100.0;
    }
}
