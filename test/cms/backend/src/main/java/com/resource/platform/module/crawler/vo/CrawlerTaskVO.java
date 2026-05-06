package com.resource.platform.module.crawler.vo;

import com.resource.platform.module.category.dto.CategoryMappingItem;
import com.resource.platform.module.crawler.dto.CustomRules;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CrawlerTaskVO {
    
    private Long id;
    
    private String name;
    
    private String targetUrl;
    
    private Boolean status;
    
    private Integer crawlInterval;
    
    private Integer maxDepth;
    
    private CategoryMappingItem[] categoryMapping;
    
    private Boolean intelligentMode;
    
    private CustomRules customRules;
    
    private CrawlerStatistics statistics;
    
    private LocalDateTime lastExecuteTime;
    
    private LocalDateTime nextExecuteTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
