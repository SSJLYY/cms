package com.resource.platform.scheduler;

import com.resource.platform.entity.CrawlerTask;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 爬虫任务调度器
 * 负责管理爬虫任务的定时执行
 */
@Slf4j
@Component
public class CrawlerScheduler {

    @Autowired
    private Scheduler scheduler;

    /**
     * 调度任务
     * 根据任务配置创建定时任务
     *
     * @param task 爬虫任务
     */
    public void scheduleTask(CrawlerTask task) {
        try {
            // 创建JobDetail
            JobDetail jobDetail = JobBuilder.newJob(CrawlerJob.class)
                    .withIdentity(getJobKey(task.getId()))
                    .usingJobData("taskId", task.getId())
                    .build();

            // 创建Trigger
            Trigger trigger = buildTrigger(task);

            // 调度任务
            if (scheduler.checkExists(jobDetail.getKey())) {
                scheduler.rescheduleJob(trigger.getKey(), trigger);
                log.info("重新调度爬虫任务: taskId={}, name={}", task.getId(), task.getName());
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
                log.info("调度爬虫任务: taskId={}, name={}", task.getId(), task.getName());
            }
        } catch (SchedulerException e) {
            log.error("调度爬虫任务失败: taskId={}, name={}", task.getId(), task.getName(), e);
            throw new RuntimeException("调度任务失败", e);
        }
    }

    /**
     * 更新任务调度
     * 更新已存在任务的调度配置
     *
     * @param task 爬虫任务
     */
    public void updateSchedule(CrawlerTask task) {
        try {
            TriggerKey triggerKey = getTriggerKey(task.getId());
            
            if (scheduler.checkExists(triggerKey)) {
                // 创建新的Trigger
                Trigger newTrigger = buildTrigger(task);
                
                // 重新调度
                scheduler.rescheduleJob(triggerKey, newTrigger);
                log.info("更新爬虫任务调度: taskId={}, name={}", task.getId(), task.getName());
            } else {
                // 如果不存在，直接调度
                scheduleTask(task);
            }
        } catch (SchedulerException e) {
            log.error("更新爬虫任务调度失败: taskId={}, name={}", task.getId(), task.getName(), e);
            throw new RuntimeException("更新任务调度失败", e);
        }
    }

    /**
     * 移除任务调度
     * 从调度器中移除任务
     *
     * @param taskId 任务ID
     */
    public void removeSchedule(Long taskId) {
        try {
            JobKey jobKey = getJobKey(taskId);
            
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("移除爬虫任务调度: taskId={}", taskId);
            }
        } catch (SchedulerException e) {
            log.error("移除爬虫任务调度失败: taskId={}", taskId, e);
            throw new RuntimeException("移除任务调度失败", e);
        }
    }

    /**
     * 立即触发任务
     * 手动触发任务执行，不影响原有调度
     *
     * @param taskId 任务ID
     */
    public void triggerNow(Long taskId) {
        try {
            JobKey jobKey = getJobKey(taskId);
            
            if (scheduler.checkExists(jobKey)) {
                scheduler.triggerJob(jobKey);
                log.info("立即触发爬虫任务: taskId={}", taskId);
            } else {
                log.warn("任务不存在，无法触发: taskId={}", taskId);
                throw new RuntimeException("任务不存在");
            }
        } catch (SchedulerException e) {
            log.error("触发爬虫任务失败: taskId={}", taskId, e);
            throw new RuntimeException("触发任务失败", e);
        }
    }

    /**
     * 构建Trigger
     * 根据任务配置创建Cron或Simple Trigger
     *
     * @param task 爬虫任务
     * @return Trigger
     */
    private Trigger buildTrigger(CrawlerTask task) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(task.getId()))
                .startNow();

        // 根据爬取间隔创建调度策略
        Integer crawlInterval = task.getCrawlInterval();
        if (crawlInterval != null && crawlInterval > 0) {
            // 使用Simple Trigger，按小时间隔执行
            SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInHours(crawlInterval)
                    .repeatForever();
            
            triggerBuilder.withSchedule(scheduleBuilder);
        } else {
            // 如果没有设置间隔，创建一个只执行一次的触发器
            triggerBuilder.startAt(new Date());
        }

        return triggerBuilder.build();
    }

    /**
     * 获取JobKey
     *
     * @param taskId 任务ID
     * @return JobKey
     */
    private JobKey getJobKey(Long taskId) {
        return JobKey.jobKey("crawler-job-" + taskId, "crawler-group");
    }

    /**
     * 获取TriggerKey
     *
     * @param taskId 任务ID
     * @return TriggerKey
     */
    private TriggerKey getTriggerKey(Long taskId) {
        return TriggerKey.triggerKey("crawler-trigger-" + taskId, "crawler-group");
    }
}
