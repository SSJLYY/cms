package com.resource.platform.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.common.PageResult;
import com.resource.platform.module.category.entity.Category;
import com.resource.platform.module.category.mapper.CategoryMapper;
import com.resource.platform.module.resource.entity.Resource;
import com.resource.platform.module.resource.mapper.ResourceMapper;
import com.resource.platform.module.system.dto.VisitQueryDTO;
import com.resource.platform.module.system.entity.AccessLog;
import com.resource.platform.module.system.mapper.AccessLogMapper;
import com.resource.platform.module.system.service.StatisticsService;
import com.resource.platform.module.system.vo.StatisticsOverviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_ACTIVITY_LIMIT = 10;
    private static final int MAX_ACTIVITY_LIMIT = 100;

    @Autowired
    private AccessLogMapper accessLogMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    private static class TimeRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        private TimeRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }

    @Override
    public StatisticsOverviewVO getOverview(String period) {
        String normalizedPeriod = normalizePeriod(period);
        TimeRange timeRange = getTimeRange(normalizedPeriod);

        StatisticsOverviewVO overview = new StatisticsOverviewVO();
        overview.setPeriod(normalizedPeriod);

        LambdaQueryWrapper<AccessLog> downloadWrapper = new LambdaQueryWrapper<>();
        downloadWrapper.ge(AccessLog::getCreateTime, timeRange.start);
        downloadWrapper.lt(AccessLog::getCreateTime, timeRange.end);
        downloadWrapper.eq(AccessLog::getActionType, "download");
        Long totalDownloads = accessLogMapper.selectCount(downloadWrapper);
        overview.setTotalDownloads(totalDownloads == null ? 0 : totalDownloads.intValue());

        LambdaQueryWrapper<AccessLog> visitWrapper = new LambdaQueryWrapper<>();
        visitWrapper.ge(AccessLog::getCreateTime, timeRange.start);
        visitWrapper.lt(AccessLog::getCreateTime, timeRange.end);
        visitWrapper.eq(AccessLog::getActionType, "visit");
        Long totalVisits = accessLogMapper.selectCount(visitWrapper);
        overview.setTotalVisits(totalVisits == null ? 0 : totalVisits.intValue());

        overview.setNewVisits("today".equals(normalizedPeriod) ? overview.getTotalVisits() : 0);

        log.info("Statistics overview loaded: period={}, start={}, end={}, downloads={}, visits={}",
            normalizedPeriod, timeRange.start, timeRange.end, overview.getTotalDownloads(), overview.getTotalVisits());
        return overview;
    }

    @Override
    public List<Map<String, Object>> getDownloadDistribution(String period) {
        TimeRange timeRange = getTimeRange(normalizePeriod(period));

        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AccessLog::getCreateTime, timeRange.start);
        wrapper.lt(AccessLog::getCreateTime, timeRange.end);
        wrapper.eq(AccessLog::getActionType, "download");
        wrapper.isNotNull(AccessLog::getResourceId);

        List<AccessLog> logs = accessLogMapper.selectList(wrapper);
        Map<Long, Long> countMap = logs.stream()
            .collect(Collectors.groupingBy(AccessLog::getResourceId, Collectors.counting()));

        List<Long> resourceIds = new ArrayList<>(countMap.keySet());
        Map<Long, Resource> resourceMap = resourceIds.isEmpty()
            ? Collections.emptyMap()
            : resourceMapper.selectBatchIds(resourceIds).stream()
                .collect(Collectors.toMap(Resource::getId, r -> r, (a, b) -> a));

        Set<Long> categoryIds = resourceMap.values().stream()
            .map(Resource::getCategoryId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, Category> categoryMap = categoryIds.isEmpty()
            ? Collections.emptyMap()
            : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));

        List<Map<String, Object>> result = new ArrayList<>();
        countMap.forEach((resourceId, count) -> {
            Resource resource = resourceMap.get(resourceId);
            if (resource != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", resource.getTitle());
                if (resource.getCategoryId() != null) {
                    Category category = categoryMap.get(resource.getCategoryId());
                    item.put("category", category != null ? category.getName() : "未分类");
                } else {
                    item.put("category", "未分类");
                }
                item.put("value", count);
                result.add(item);
            }
        });

        return result.stream()
            .sorted((a, b) -> Long.compare(((Number) b.get("value")).longValue(), ((Number) a.get("value")).longValue()))
            .limit(10)
            .collect(Collectors.toList());
    }

    @Override
    public PageResult<Map<String, Object>> getVisitDetails(VisitQueryDTO query) {
        VisitQueryDTO safeQuery = query == null ? new VisitQueryDTO() : query;
        String period = normalizePeriod(safeQuery.getPeriod());
        long safePageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() < 1 ? 1L : safeQuery.getPageNum();
        int safePageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() < 1
            ? 10
            : Math.min(safeQuery.getPageSize(), MAX_PAGE_SIZE);
        TimeRange timeRange = getTimeRange(period);

        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AccessLog::getCreateTime, timeRange.start);
        wrapper.lt(AccessLog::getCreateTime, timeRange.end);
        wrapper.eq(AccessLog::getActionType, "visit");
        wrapper.orderByDesc(AccessLog::getCreateTime);

        List<AccessLog> logs = accessLogMapper.selectList(wrapper);
        List<AccessLog> resourceLogs = logs.stream()
            .filter(log -> log.getResourceId() != null)
            .collect(Collectors.toList());

        List<Long> resourceIds = resourceLogs.stream()
            .map(AccessLog::getResourceId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, Resource> resourceMap = resourceIds.isEmpty()
            ? Collections.emptyMap()
            : resourceMapper.selectBatchIds(resourceIds).stream()
                .collect(Collectors.toMap(Resource::getId, r -> r, (a, b) -> a));

        Set<Long> categoryIds = resourceMap.values().stream()
            .map(Resource::getCategoryId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, Category> categoryMap = categoryIds.isEmpty()
            ? Collections.emptyMap()
            : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));

        List<Map<String, Object>> allVisitDetails = resourceLogs.stream()
            .collect(Collectors.groupingBy(log -> buildVisitGroupKey(
                log.getResourceId(),
                log.getReferer(),
                log.getBrowser()
            )))
            .entrySet().stream()
            .map(entry -> {
                List<AccessLog> groupLogs = entry.getValue();
                AccessLog sampleLog = groupLogs.get(0);
                Long resourceId = sampleLog.getResourceId();
                long visits = groupLogs.size();
                Resource resource = resourceMap.get(resourceId);

                Map<String, Object> item = new HashMap<>();
                if (resource != null) {
                    item.put("resource", resource.getTitle());
                    item.put("resourceId", resourceId);
                    if (resource.getCategoryId() != null) {
                        Category category = categoryMap.get(resource.getCategoryId());
                        item.put("category", category != null ? category.getName() : "未分类");
                    } else {
                        item.put("category", "未分类");
                    }
                } else {
                    item.put("resource", "资源ID: " + resourceId);
                    item.put("resourceId", resourceId);
                    item.put("category", "未知");
                }

                item.put("referer", sampleLog.getReferer() != null ? sampleLog.getReferer() : "直接访问");
                item.put("browser", sampleLog.getBrowser() != null ? sampleLog.getBrowser() : "未知浏览器");
                item.put("visits", visits);
                return item;
            })
            .sorted((a, b) -> Long.compare(((Number) b.get("visits")).longValue(), ((Number) a.get("visits")).longValue()))
            .collect(Collectors.toList());

        int fromIndex = (int) Math.min((safePageNum - 1) * safePageSize, allVisitDetails.size());
        int toIndex = (int) Math.min(fromIndex + safePageSize, allVisitDetails.size());

        PageResult<Map<String, Object>> result = new PageResult<>();
        result.setTotal((long) allVisitDetails.size());
        result.setRecords(allVisitDetails.subList(fromIndex, toIndex));
        result.setCurrent(safePageNum);
        result.setSize((long) safePageSize);
        result.setPages((long) Math.ceil((double) allVisitDetails.size() / safePageSize));
        return result;
    }

    private String buildVisitGroupKey(Long resourceId, String referer, String browser) {
        return String.valueOf(resourceId)
            + "|" + (referer == null ? "" : referer)
            + "|" + (browser == null ? "" : browser);
    }

    @Override
    public List<Map<String, Object>> getRealtimeActivities(Integer limit) {
        int safeLimit = limit == null || limit < 1 ? DEFAULT_ACTIVITY_LIMIT : Math.min(limit, MAX_ACTIVITY_LIMIT);
        LambdaQueryWrapper<AccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AccessLog::getCreateTime);
        wrapper.last("LIMIT " + safeLimit);

        List<AccessLog> logs = accessLogMapper.selectList(wrapper);
        List<Long> resourceIds = logs.stream()
            .map(AccessLog::getResourceId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Resource> resourceMap = resourceIds.isEmpty()
            ? Collections.emptyMap()
            : resourceMapper.selectBatchIds(resourceIds).stream()
                .collect(Collectors.toMap(Resource::getId, r -> r, (a, b) -> a));

        return logs.stream()
            .map(log -> {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", log.getActionType());
                activity.put("title", getActivityTitle(log.getActionType()));

                String resourceInfo = "未知资源";
                if (log.getResourceId() != null) {
                    Resource resource = resourceMap.get(log.getResourceId());
                    resourceInfo = resource != null ? resource.getTitle() : "资源ID: " + log.getResourceId();
                }

                String location = log.getIpAddress() != null ? log.getIpAddress() : "未知";
                activity.put("description", resourceInfo + " - 来自 " + location);
                activity.put("timestamp", log.getCreateTime().toString().replace("T", " "));
                return activity;
            })
            .collect(Collectors.toList());
    }

    private TimeRange getTimeRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end;
        switch (period) {
            case "yesterday":
                start = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = start.plusDays(1);
                break;
            case "week":
                start = now.minusDays(7);
                end = now;
                break;
            case "month":
                start = now.minusDays(30);
                end = now;
                break;
            case "today":
            default:
                start = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = now;
                break;
        }
        return new TimeRange(start, end);
    }

    private String normalizePeriod(String period) {
        if (period == null || period.trim().isEmpty()) {
            return "today";
        }
        switch (period) {
            case "today":
            case "yesterday":
            case "week":
            case "month":
                return period;
            default:
                return "today";
        }
    }

    private String getActivityTitle(String actionType) {
        switch (actionType) {
            case "download":
                return "用户下载了资源";
            case "visit":
                return "用户访问了资源";
            case "search":
                return "用户执行了搜索";
            default:
                return "用户活动";
        }
    }
}
