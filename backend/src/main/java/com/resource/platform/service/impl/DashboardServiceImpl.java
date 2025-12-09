package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.entity.*;
import com.resource.platform.mapper.*;
import com.resource.platform.service.DashboardService;
import com.resource.platform.vo.DashboardMetricsVO;
import com.resource.platform.vo.TrendDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 控制面板服务实现类
 * 
 * 功能说明：
 * - 实现仪表盘数据统计的核心业务逻辑
 * - 提供系统核心指标的计算和汇总
 * - 生成趋势数据和图表数据
 * - 监控系统状态和资源使用情况
 * - 统计待处理事项和热门内容
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private AccessLogMapper accessLogMapper;

    @Override
    public DashboardMetricsVO getMetrics() {
        DashboardMetricsVO metrics = new DashboardMetricsVO();
        
        // 获取今天的开始时间
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        
        // 总资源数（未删除的）
        Long totalResources = resourceMapper.selectCount(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getDeleted, 0)
        );
        metrics.setTotalResources(totalResources);
        
        // 今日新增资源
        Long todayResources = resourceMapper.selectCount(
            new LambdaQueryWrapper<Resource>()
                .ge(Resource::getCreateTime, todayStart)
                .eq(Resource::getDeleted, 0)
        );
        metrics.setTodayResources(todayResources);
        
        // 总下载量（从资源表的 download_count 字段累加）
        List<Resource> allResources = resourceMapper.selectList(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getDeleted, 0)
                .select(Resource::getDownloadCount)
        );
        Long totalDownloads = allResources.stream()
            .mapToLong(r -> r.getDownloadCount() != null ? r.getDownloadCount() : 0)
            .sum();
        metrics.setTotalDownloads(totalDownloads);
        
        // 今日下载量（从访问日志表统计）
        Long todayDownloads = accessLogMapper.selectCount(
            new LambdaQueryWrapper<AccessLog>()
                .eq(AccessLog::getActionType, "download")
                .ge(AccessLog::getCreateTime, todayStart)
        );
        metrics.setTodayDownloads(todayDownloads);
        
        // 总用户数
        Long totalUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
        );
        metrics.setTotalUsers(totalUsers);
        
        // 今日新增用户
        Long todayUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, todayStart)
                .eq(User::getDeleted, 0)
        );
        metrics.setTodayUsers(todayUsers);
        
        // 总分类数
        Long totalCategories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getDeleted, 0)
        );
        metrics.setTotalCategories(totalCategories);
        
        // 待审核资源数
        Long pendingResources = resourceMapper.selectCount(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getAuditStatus, "pending")
                .eq(Resource::getDeleted, 0)
        );
        metrics.setPendingResources(pendingResources);
        
        return metrics;
    }

    @Override
    public TrendDataVO getTrendData(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        
        TrendDataVO trendData = new TrendDataVO();
        List<String> dates = new ArrayList<>();
        List<Long> resourceData = new ArrayList<>();
        List<Long> downloadData = new ArrayList<>();
        List<Long> userData = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        // 生成最近N天的数据
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(formatter));
            
            // 当天的开始和结束时间
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            
            // 统计当天新增资源数
            Long dayResourceCount = resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>()
                    .ge(Resource::getCreateTime, dayStart)
                    .le(Resource::getCreateTime, dayEnd)
                    .eq(Resource::getDeleted, 0)
            );
            resourceData.add(dayResourceCount);
            
            // 统计当天下载量
            Long dayDownloadCount = accessLogMapper.selectCount(
                new LambdaQueryWrapper<AccessLog>()
                    .eq(AccessLog::getActionType, "download")
                    .ge(AccessLog::getCreateTime, dayStart)
                    .le(AccessLog::getCreateTime, dayEnd)
            );
            downloadData.add(dayDownloadCount);
            
            // 统计当天新增用户数
            Long dayUserCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, dayStart)
                    .le(User::getCreateTime, dayEnd)
                    .eq(User::getDeleted, 0)
            );
            userData.add(dayUserCount);
        }
        
        trendData.setDates(dates);
        trendData.setResourceData(resourceData);
        trendData.setDownloadData(downloadData);
        trendData.setUserData(userData);
        
        return trendData;
    }

    @Override
    public Object getHotResources(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        
        // 查询热门资源（按下载量排序）
        List<Resource> hotResources = resourceMapper.selectList(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getStatus, 1)  // 已发布
                .eq(Resource::getDeleted, 0)
                .orderByDesc(Resource::getDownloadCount)
                .last("LIMIT " + limit)
        );
        
        return hotResources;
    }

    @Override
    public Object getLatestResources(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        
        // 查询最新资源（按创建时间排序）
        List<Resource> latestResources = resourceMapper.selectList(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getStatus, 1)  // 已发布
                .eq(Resource::getDeleted, 0)
                .orderByDesc(Resource::getCreateTime)
                .last("LIMIT " + limit)
        );
        
        return latestResources;
    }

    @Override
    public Map<String, Long> getPendingTasks() {
        Map<String, Long> tasks = new HashMap<>();
        
        // 待审核资源
        Long pendingResources = resourceMapper.selectCount(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getAuditStatus, "pending")
                .eq(Resource::getDeleted, 0)
        );
        tasks.put("pendingResources", pendingResources);
        
        // 待处理反馈（状态为 unread 或 processing）
        Long pendingFeedback = feedbackMapper.selectCount(
            new LambdaQueryWrapper<Feedback>()
                .in(Feedback::getStatus, "unread", "processing")
                .eq(Feedback::getDeleted, 0)
        );
        tasks.put("pendingFeedback", pendingFeedback);
        
        // 待回复反馈（reply 为空）
        Long unrepliedFeedback = feedbackMapper.selectCount(
            new LambdaQueryWrapper<Feedback>()
                .and(wrapper -> wrapper.isNull(Feedback::getReply).or().eq(Feedback::getReply, ""))
                .eq(Feedback::getDeleted, 0)
        );
        tasks.put("unrepliedFeedback", unrepliedFeedback);
        
        return tasks;
    }

    @Override
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 获取操作系统的 MXBean
            com.sun.management.OperatingSystemMXBean osBean = 
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            
            // CPU使用率（系统级别）
            double cpuUsage = osBean.getSystemCpuLoad() * 100;
            // 如果获取失败，使用进程CPU使用率
            if (cpuUsage < 0) {
                cpuUsage = osBean.getProcessCpuLoad() * 100;
            }
            status.put("cpuUsage", Math.round(cpuUsage * 100.0) / 100.0);
            
        } catch (Exception e) {
            log.warn("获取CPU使用率失败", e);
            status.put("cpuUsage", 0.0);
        }
        
        // 内存使用率（JVM）
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / maxMemory * 100;
        status.put("memoryUsage", Math.round(memoryUsage * 100.0) / 100.0);
        
        // 磁盘使用率
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double diskUsage = (double) usedSpace / totalSpace * 100;
            status.put("diskUsage", Math.round(diskUsage * 100.0) / 100.0);
        } catch (Exception e) {
            log.warn("获取磁盘使用率失败", e);
            status.put("diskUsage", 0.0);
        }
        
        return status;
    }
}
