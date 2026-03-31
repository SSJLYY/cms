package com.resource.platform.config;

import io.micrometer.core.instrument.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 业务指标埋点组件
 *
 * <p>所有 Prometheus 指标统一在此注册，避免散落在各 Service 中难以维护。
 *
 * <p>指标命名规范：{系统名}.{业务域}.{动作}[.{单位}]
 * 例如：platform.resource.download.total
 *
 * <p>在 Grafana 中可用这些指标构建：
 * <ul>
 *   <li>下载量趋势图（rate/irate）</li>
 *   <li>资源创建/审核数量</li>
 *   <li>爬虫成功/失败率</li>
 *   <li>活跃用户数</li>
 *   <li>存储服务熔断状态</li>
 * </ul>
 */
@Slf4j
@Component
@Getter
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    // ==================== 资源相关指标 ====================

    /** 资源下载总次数（按网盘类型标签区分）*/
    private final Counter resourceDownloadTotal;

    /** 资源创建总次数 */
    private final Counter resourceCreateTotal;

    /** 资源审核通过次数 */
    private final Counter resourceApproveTotal;

    /** 资源审核拒绝次数 */
    private final Counter resourceRejectTotal;

    // ==================== 爬虫相关指标 ====================

    /** 爬虫任务执行总次数 */
    private final Counter crawlerTaskTotal;

    /** 爬虫任务成功次数 */
    private final Counter crawlerTaskSuccess;

    /** 爬虫任务失败次数 */
    private final Counter crawlerTaskFailed;

    /** 爬虫采集到的资源总数 */
    private final Counter crawlerResourceCollected;

    // ==================== 用户相关指标 ====================

    /** 用户登录次数 */
    private final Counter userLoginTotal;

    /** 用户登录失败次数 */
    private final Counter userLoginFailed;

    // ==================== 系统相关指标 ====================

    /** 文件上传总次数 */
    private final Counter fileUploadTotal;

    /** 文件上传失败次数 */
    private final Counter fileUploadFailed;

    // ==================== 实时 Gauge 指标 ====================

    /** 当前正在运行的爬虫任务数 */
    private final AtomicLong activeCrawlerTasks = new AtomicLong(0);

    @Autowired
    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 注册 Counter
        resourceDownloadTotal = Counter.builder("platform.resource.download.total")
            .description("资源下载总次数")
            .tag("type", "all")
            .register(meterRegistry);

        resourceCreateTotal = Counter.builder("platform.resource.create.total")
            .description("资源创建总次数")
            .register(meterRegistry);

        resourceApproveTotal = Counter.builder("platform.resource.audit.total")
            .description("资源审核通过次数")
            .tag("action", "approve")
            .register(meterRegistry);

        resourceRejectTotal = Counter.builder("platform.resource.audit.total")
            .description("资源审核拒绝次数")
            .tag("action", "reject")
            .register(meterRegistry);

        crawlerTaskTotal = Counter.builder("platform.crawler.task.total")
            .description("爬虫任务执行总次数")
            .register(meterRegistry);

        crawlerTaskSuccess = Counter.builder("platform.crawler.task.result.total")
            .description("爬虫任务成功次数")
            .tag("result", "success")
            .register(meterRegistry);

        crawlerTaskFailed = Counter.builder("platform.crawler.task.result.total")
            .description("爬虫任务失败次数")
            .tag("result", "failed")
            .register(meterRegistry);

        crawlerResourceCollected = Counter.builder("platform.crawler.resource.collected.total")
            .description("爬虫采集到的资源总数")
            .register(meterRegistry);

        userLoginTotal = Counter.builder("platform.user.login.total")
            .description("用户登录次数")
            .tag("result", "all")
            .register(meterRegistry);

        userLoginFailed = Counter.builder("platform.user.login.failed.total")
            .description("用户登录失败次数")
            .register(meterRegistry);

        fileUploadTotal = Counter.builder("platform.file.upload.total")
            .description("文件上传总次数")
            .register(meterRegistry);

        fileUploadFailed = Counter.builder("platform.file.upload.failed.total")
            .description("文件上传失败次数")
            .register(meterRegistry);

        // 注册 Gauge（实时值）
        Gauge.builder("platform.crawler.active.tasks", activeCrawlerTasks, AtomicLong::get)
            .description("当前正在运行的爬虫任务数")
            .register(meterRegistry);

        log.info("业务 Prometheus 指标注册完成");
    }

    // ==================== 便捷埋点方法 ====================

    /**
     * 记录资源下载事件
     *
     * @param linkType 网盘类型（quark/thunder/baidu 等）
     */
    public void recordDownload(String linkType) {
        Counter.builder("platform.resource.download.total")
            .description("资源下载总次数（按类型）")
            .tag("type", linkType != null ? linkType : "unknown")
            .register(meterRegistry)
            .increment();
    }

    /** 记录爬虫任务开始 */
    public void onCrawlerTaskStart() {
        crawlerTaskTotal.increment();
        activeCrawlerTasks.incrementAndGet();
    }

    /** 记录爬虫任务完成（成功）*/
    public void onCrawlerTaskSuccess(long collectedCount) {
        crawlerTaskSuccess.increment();
        crawlerResourceCollected.increment(collectedCount);
        activeCrawlerTasks.decrementAndGet();
    }

    /** 记录爬虫任务完成（失败）*/
    public void onCrawlerTaskFailed() {
        crawlerTaskFailed.increment();
        activeCrawlerTasks.decrementAndGet();
    }

    /**
     * 记录任意操作耗时（用于 Histogram）
     *
     * @param operationName 操作名称
     * @param durationMs    耗时毫秒
     */
    public void recordOperationTime(String operationName, long durationMs) {
        Timer.builder("platform.operation.duration")
            .description("业务操作耗时")
            .tag("operation", operationName)
            .register(meterRegistry)
            .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
