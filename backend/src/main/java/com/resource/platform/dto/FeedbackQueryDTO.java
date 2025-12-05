package com.resource.platform.dto;

import lombok.Data;

@Data
public class FeedbackQueryDTO {
    private String keyword;
    private String status;
    private String type;
    private Integer page = 1;
    private Integer pageSize = 10;
    
    // 兼容pageNum
    public Integer getPageNum() {
        return page;
    }
    
    public void setPageNum(Integer pageNum) {
        this.page = pageNum;
    }
}
