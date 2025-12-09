package com.resource.platform.service;

public interface CrawlerExecutionService {
    
    /**
     * 执行爬虫任务
     * @param taskId 任务ID
     * @param executeType 执行类型: 1-定时, 2-手动
     */
    void executeCrawlerTask(Long taskId, Integer executeType);
    
    /**
     * 检查任务是否正在执行
     */
    Boolean isTaskRunning(Long taskId);
    
    /**
     * 停止正在执行的任务
     */
    void stopTask(Long taskId);
}
