package com.resource.platform.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.module.category.entity.Category;
import com.resource.platform.module.category.mapper.CategoryMapper;
import com.resource.platform.module.feedback.entity.Feedback;
import com.resource.platform.module.feedback.mapper.FeedbackMapper;
import com.resource.platform.module.resource.mapper.ResourceMapper;
import com.resource.platform.module.resource.entity.Resource;
import com.resource.platform.module.system.entity.AccessLog;
import com.resource.platform.module.user.mapper.UserMapper;
import com.resource.platform.module.user.entity.User;
import com.resource.platform.module.system.mapper.AccessLogMapper;
import com.resource.platform.module.system.service.DashboardService;
import com.resource.platform.module.system.vo.DashboardMetricsVO;
import com.resource.platform.module.system.vo.TrendDataVO;
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
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_TREND_DAYS = 7;
    private static final int MAX_TREND_DAYS = 365;

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
        
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        
        Long totalResources = resourceMapper.selectCount(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getDeleted, 0)
        );
        metrics.setTotalResources(totalResources);
        
        Long todayResources = resourceMapper.selectCount(
            new LambdaQueryWrapper<Resource>()
                .ge(Resource::getCreateTime, todayStart)
                .eq(Resource::getDeleted, 0)
        );
        metrics.setTodayResources(todayResources);
        
        List<Resource> allResources = resourceMapper.selectList(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getDeleted, 0)
                .select(Resource::getDownloadCount)
        );
        Long totalDownloads = allResources.stream()
            .mapToLong(r -> r.getDownloadCount() != null ? r.getDownloadCount() : 0)
            .sum();
        metrics.setTotalDownloads(totalDownloads);
        
        Long todayDownloads = accessLogMapper.selectCount(
            new LambdaQueryWrapper<AccessLog>()
                .eq(AccessLog::getActionType, "download")
                .ge(AccessLog::getCreateTime, todayStart)
        );
        metrics.setTodayDownloads(todayDownloads);
        
        Long totalUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
        );
        metrics.setTotalUsers(totalUsers);
        
        Long todayUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, todayStart)
                .eq(User::getDeleted, 0)
        );
        metrics.setTodayUsers(todayUsers);
        
        Long totalCategories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getDeleted, 0)
        );
        metrics.setTotalCategories(totalCategories);
        
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
            days = DEFAULT_TREND_DAYS;
        } else if (days > MAX_TREND_DAYS) {
            days = MAX_TREND_DAYS;
        }
        
        TrendDataVO trendData = new TrendDataVO();
        List<String> dates = new ArrayList<>();
        List<Long> resourceData = new ArrayList<>();
        List<Long> downloadData = new ArrayList<>();
        List<Long> userData = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(formatter));
            
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            
            Long dayResourceCount = resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>()
                    .ge(Resource::getCreateTime, dayStart)
                    .le(Resource::getCreateTime, dayEnd)
                    .eq(Resource::getDeleted, 0)
            );
            resourceData.add(dayResourceCount);
            
            Long dayDownloadCount = accessLogMapper.selectCount(
                new LambdaQueryWrapper<AccessLog>()
                    .eq(AccessLog::getActionType, "download")
                    .ge(AccessLog::getCreateTime, dayStart)
                    .le(AccessLog::getCreateTime, dayEnd)
            );
            downloadData.add(dayDownloadCount);
            
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
            limit = DEFAULT_LIMIT;
        } else if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        
        List<Resource> hotResources = resourceMapper.selectList(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getStatus, 1)
                .eq(Resource::getDeleted, 0)
                .orderByDesc(Resource::getDownloadCount)
                .last("LIMIT " + limit)
        );
        
        return hotResources;
    }

    @Override
    public Object getLatestResources(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = DEFAULT_LIMIT;
        } else if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        
        List<Resource> latestResources = resourceMapper.selectList(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getStatus, 1)
                .eq(Resource::getDeleted, 0)
                .orderByDesc(Resource::getCreateTime)
                .last("LIMIT " + limit)
        );
        
        return latestResources;
    }

    @Override
    public Map<String, Long> getPendingTasks() {
        Map<String, Long> tasks = new HashMap<>();
        
        Long pendingResources = resourceMapper.selectCount(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getAuditStatus, "pending")
                .eq(Resource::getDeleted, 0)
        );
        tasks.put("pendingResources", pendingResources);
        
        Long pendingFeedback = feedbackMapper.selectCount(
            new LambdaQueryWrapper<Feedback>()
                .in(Feedback::getStatus, "unread", "processing")
                .eq(Feedback::getDeleted, 0)
        );
        tasks.put("pendingFeedback", pendingFeedback);
        
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
            com.sun.management.OperatingSystemMXBean osBean = 
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            
            double cpuUsage = osBean.getSystemCpuLoad() * 100;
            if (cpuUsage < 0) {
                cpuUsage = osBean.getProcessCpuLoad() * 100;
            }
            status.put("cpuUsage", clampPercentage(cpuUsage));
            
        } catch (Exception e) {
            log.warn("获取CPU使用率失败", e);
            status.put("cpuUsage", 0.0);
        }
        
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / maxMemory * 100;
        status.put("memoryUsage", clampPercentage(memoryUsage));
        
        try {
            java.io.File root = new java.io.File(System.getProperty("user.dir")).getAbsoluteFile();
            while (root.getParentFile() != null) {
                root = root.getParentFile();
            }
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            if (totalSpace <= 0) {
                throw new IllegalStateException("磁盘总空间无效");
            }
            long usedSpace = totalSpace - freeSpace;
            double diskUsage = (double) usedSpace / totalSpace * 100;
            status.put("diskUsage", clampPercentage(diskUsage));
        } catch (Exception e) {
            log.warn("获取磁盘使用率失败", e);
            status.put("diskUsage", 0.0);
        }
        
        return status;
    }

    private double clampPercentage(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        double normalized = Math.max(0.0, Math.min(100.0, value));
        return Math.round(normalized * 100.0) / 100.0;
    }
}