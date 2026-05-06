package com.resource.platform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * <p>架构说明：
 * 爬虫线程池与业务线程池完全隔离，防止爬虫任务阻塞业务请求处理。
 * <ul>
 *   <li>{@code crawlerExecutor}：用于爬虫任务执行，核心数小、队列有界</li>
 *   <li>{@code asyncExecutor}：用于业务异步任务（邮件、日志写入等），不影响主流程</li>
 * </ul>
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    // ===== 爬虫线程池参数 =====
    @Value("${crawler.thread-pool.core-size:3}")
    private int crawlerCoreSize;

    @Value("${crawler.thread-pool.max-size:5}")
    private int crawlerMaxSize;

    @Value("${crawler.thread-pool.queue-capacity:50}")
    private int crawlerQueueCapacity;

    /**
     * 爬虫专用线程池
     *
     * <p>设计原则：
     * <ul>
     *   <li>核心线程数和最大线程数都很小，防止抢占业务资源</li>
     *   <li>队列有界，防止内存溢出</li>
     *   <li>拒绝策略：调用方线程执行（背压机制），防止队列无限增长</li>
     *   <li>优雅关闭：等待任务完成再停止</li>
     * </ul>
     */
    @Bean("crawlerExecutor")
    public ThreadPoolTaskExecutor crawlerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(crawlerCoreSize);
        executor.setMaxPoolSize(crawlerMaxSize);
        executor.setQueueCapacity(crawlerQueueCapacity);
        executor.setThreadNamePrefix("crawler-");
        executor.setKeepAliveSeconds(60);
        // 拒绝策略：背压（调用方线程直接执行），防止任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 优雅关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("爬虫线程池初始化完成: core={}, max={}, queue={}", 
            crawlerCoreSize, crawlerMaxSize, crawlerQueueCapacity);
        return executor;
    }

    /**
     * 业务异步任务线程池
     *
     * <p>用于：邮件发送、日志异步写入、事件处理等不阻塞主流程的业务操作
     */
    @Bean("asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-biz-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new LoggingCallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("业务异步线程池初始化完成: core=5, max=20, queue=500");
        return executor;
    }

    /**
     * 带日志记录的 CallerRunsPolicy
     * 当线程池满载时，记录告警日志并在调用方线程执行任务
     */
    private static class LoggingCallerRunsPolicy implements RejectedExecutionHandler {
        private final ThreadPoolExecutor.CallerRunsPolicy delegate = new ThreadPoolExecutor.CallerRunsPolicy();

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("线程池已满，任务将在调用方线程执行: poolSize={}, queueSize={}", 
                executor.getPoolSize(), executor.getQueue().size());
            delegate.rejectedExecution(r, executor);
        }
    }
}
