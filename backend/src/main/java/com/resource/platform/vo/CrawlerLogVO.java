package com.resource.platform.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CrawlerLogVO {
    
    private Long id;
    
    private Long taskId;
    
    private String taskName;
    
    private Integer executeType;
    
    private String executeTypeText;
    
    private Integer status;
    
    private String statusText;
    
    private Integer crawledCount;
    
    private Integer successCount;
    
    private Integer failedCount;
    
    private Integer duration;
    
    private String errorMessage;
    
    private String errorType;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    public void setExecuteType(Integer executeType) {
        this.executeType = executeType;
        if (executeType != null) {
            switch (executeType) {
                case 1:
                    this.executeTypeText = "定时执行";
                    break;
                case 2:
                    this.executeTypeText = "手动执行";
                    break;
                default:
                    this.executeTypeText = "未知";
            }
        }
    }
    
    public void setStatus(Integer status) {
        this.status = status;
        if (status != null) {
            switch (status) {
                case 1:
                    this.statusText = "执行中";
                    break;
                case 2:
                    this.statusText = "成功";
                    break;
                case 3:
                    this.statusText = "失败";
                    break;
                default:
                    this.statusText = "未知";
            }
        }
    }
}
