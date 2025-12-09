package com.resource.platform.scheduler;

import com.resource.platform.service.CrawlerExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 爬虫任务Job
 * Quartz定时任务执行器
 */
@Slf4j
@Component
public class CrawlerJob implements Job {

    @Autowired
    private CrawlerExecutionService crawlerExecutionService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 获取任务ID
        Long taskId = context.getJobDetail().getJobDataMap().getLong("taskId");
        
        log.info("开始执行定时爬虫任务: taskId={}", taskId);
        
        try {
            // 执行爬虫任务 (1表示定时触发)
            crawlerExecutionService.executeCrawlerTask(taskId, 1);
            log.info("定时爬虫任务执行完成: taskId={}", taskId);
        } catch (Exception e) {
            log.error("定时爬虫任务执行失败: taskId={}", taskId, e);
            // 不抛出异常，避免影响后续调度
            // Quartz会记录失败信息
        }
    }
}
