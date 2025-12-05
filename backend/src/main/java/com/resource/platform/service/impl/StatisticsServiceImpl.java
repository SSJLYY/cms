package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.dto.VisitQueryDTO;
import com.resource.platform.entity.AccessLog;
import com.resource.platform.entity.Resource;
import com.resource.platform.mapper.AccessLogMapper;
import com.resource.platform.mapper.CategoryMapper;
import com.resource.platform.mapper.ResourceMapper;
import com.resource.platform.service.StatisticsService;
import com.resource.platform.vo.StatisticsOverviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    @Autowired
    private AccessLogMapper accessLogMapper;
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Override
    public StatisticsOverviewVO getOverview(String period) {
        StatisticsOverviewVO overview = new StatisticsOverviewVO();
        overview.setPeriod(period);
        
        LocalDateTime startTime = getStartTime(period);
        
        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AccessLog::getCreateTime, startTime);
        
        // 总下载量
        wrapper.eq(AccessLog::getActionType, "download");
        overview.setTotalDownloads(accessLogMapper.selectCount(wrapper).intValue());
        
        // 总访问量
        wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AccessLog::getCreateTime, startTime);
        wrapper.eq(AccessLog::getActionType, "visit");
        overview.setTotalVisits(accessLogMapper.selectCount(wrapper).intValue());
        
        // 新增访问（今日）
        if ("today".equals(period)) {
            overview.setNewVisits(overview.getTotalVisits());
        } else {
            overview.setNewVisits(0);
        }
        
        return overview;
    }
    
    @Override
    public List<Map<String, Object>> getDownloadDistribution(String period) {
        LocalDateTime startTime = getStartTime(period);
        
        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AccessLog::getCreateTime, startTime);
        wrapper.eq(AccessLog::getActionType, "download");
        wrapper.isNotNull(AccessLog::getResourceId);
        
        List<AccessLog> logs = accessLogMapper.selectList(wrapper);
        
        Map<Long, Long> countMap = logs.stream()
                .collect(Collectors.groupingBy(AccessLog::getResourceId, Collectors.counting()));
        
        List<Map<String, Object>> result = new ArrayList<>();
        countMap.forEach((resourceId, count) -> {
            Resource resource = resourceMapper.selectById(resourceId);
            if (resource != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", resource.getTitle());
                item.put("value", count);
                result.add(item);
            }
        });
        
        return result.stream()
                .sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                .limit(10)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResult<Map<String, Object>> getVisitDetails(VisitQueryDTO query) {
        Page<AccessLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AccessLog::getCreateTime, getStartTime(query.getPeriod()));
        wrapper.orderByDesc(AccessLog::getCreateTime);
        
        IPage<AccessLog> pageResult = accessLogMapper.selectPage(page, wrapper);
        
        // 统计每个资源的访问次数
        Map<Long, Long> visitCountMap = pageResult.getRecords().stream()
                .filter(log -> log.getResourceId() != null)
                .collect(Collectors.groupingBy(AccessLog::getResourceId, Collectors.counting()));
        
        // 获取唯一的资源并构建结果
        List<Map<String, Object>> voList = visitCountMap.entrySet().stream()
                .map(entry -> {
                    Long resourceId = entry.getKey();
                    Long visits = entry.getValue();
                    
                    Resource resource = resourceMapper.selectById(resourceId);
                    
                    Map<String, Object> map = new HashMap<>();
                    if (resource != null) {
                        map.put("resource", resource.getTitle());
                        map.put("resourceId", resourceId);
                        
                        // 获取分类信息
                        if (resource.getCategoryId() != null) {
                            com.resource.platform.entity.Category category = 
                                    categoryMapper.selectById(resource.getCategoryId());
                            map.put("category", category != null ? category.getName() : "未分类");
                        } else {
                            map.put("category", "未分类");
                        }
                    } else {
                        map.put("resource", "资源ID: " + resourceId);
                        map.put("resourceId", resourceId);
                        map.put("category", "未知");
                    }
                    
                    // 获取该资源的最新访问记录
                    AccessLog latestLog = pageResult.getRecords().stream()
                            .filter(log -> resourceId.equals(log.getResourceId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (latestLog != null) {
                        map.put("referer", latestLog.getReferer() != null ? latestLog.getReferer() : "直接访问");
                        map.put("browser", latestLog.getBrowser() != null ? latestLog.getBrowser() : "未知浏览器");
                    } else {
                        map.put("referer", "直接访问");
                        map.put("browser", "未知浏览器");
                    }
                    
                    map.put("visits", visits.intValue());
                    return map;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("visits"), (Integer) a.get("visits")))
                .collect(Collectors.toList());
        
        return new PageResult<>(pageResult.getTotal(), voList);
    }
    
    @Override
    public List<Map<String, Object>> getRealtimeActivities(Integer limit) {
        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AccessLog::getCreateTime);
        wrapper.last("LIMIT " + (limit != null ? limit : 10));
        
        List<AccessLog> logs = accessLogMapper.selectList(wrapper);
        
        return logs.stream()
                .map(log -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("type", log.getActionType());
                    activity.put("title", getActivityTitle(log.getActionType()));
                    
                    // 获取资源信息
                    String resourceInfo = "未知资源";
                    if (log.getResourceId() != null) {
                        Resource resource = resourceMapper.selectById(log.getResourceId());
                        if (resource != null) {
                            resourceInfo = resource.getTitle();
                        } else {
                            resourceInfo = "资源ID: " + log.getResourceId();
                        }
                    }
                    
                    String location = log.getIpAddress() != null ? log.getIpAddress() : "未知";
                    activity.put("description", resourceInfo + " - 来自 " + location);
                    activity.put("timestamp", log.getCreateTime().toString().replace("T", " "));
                    return activity;
                })
                .collect(Collectors.toList());
    }
    
    private LocalDateTime getStartTime(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case "yesterday":
                return now.minusDays(1).withHour(0).withMinute(0).withSecond(0);
            case "week":
                return now.minusDays(7);
            case "month":
                return now.minusDays(30);
            case "today":
            default:
                return now.withHour(0).withMinute(0).withSecond(0);
        }
    }
    
    private String getActivityTitle(String actionType) {
        switch (actionType) {
            case "download":
                return "用户下载了资源";
            case "visit":
                return "新用户访问";
            case "search":
                return "用户搜索";
            default:
                return "用户活动";
        }
    }
}
