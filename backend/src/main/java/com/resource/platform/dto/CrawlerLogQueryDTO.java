package com.resource.platform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CrawlerLogQueryDTO {
    
    private Long taskId;
    
    private Integer status;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer page = 1;
    
    private Integer size = 10;
    
    // Alias for size
    public Integer getPageSize() {
        return size;
    }
    
    public void setPageSize(Integer pageSize) {
        this.size = pageSize;
    }
}
