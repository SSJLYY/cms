package com.resource.platform.dto;

import lombok.Data;

@Data
public class CrawlerTaskQueryDTO {
    
    private String name;
    
    private Integer status;
    
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
