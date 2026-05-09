package com.resource.platform.module.revenue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.BizErrorCode;
import com.resource.platform.common.PageResult;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.module.revenue.entity.Revenue;
import com.resource.platform.module.revenue.mapper.RevenueMapper;
import com.resource.platform.module.revenue.service.RevenueService;
import com.resource.platform.module.revenue.vo.RevenueOverviewVO;
import com.resource.platform.module.revenue.vo.RevenueTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RevenueServiceImpl implements RevenueService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RevenueMapper revenueMapper;

    private static final Map<String, String> TYPE_NAME_MAP;

    static {
        Map<String, String> mutableMap = new HashMap<>();
        mutableMap.put("cloud_storage", "云存储");
        mutableMap.put("download_revenue", "下载收益");
        mutableMap.put("mobile_cloud", "移动云盘");
        mutableMap.put("mobile_cloud_backup", "移动云盘(备用)");
        mutableMap.put("uc_cloud", "UC网盘");
        mutableMap.put("12_cloud", "12云盘");
        mutableMap.put("lanzou_cloud", "蓝奏云");
        mutableMap.put("chengtong_cloud", "城通网盘");
        TYPE_NAME_MAP = Collections.unmodifiableMap(mutableMap);
    }

    private static class TimeRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        private TimeRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }

    @Override
    public RevenueOverviewVO getOverview(String period) {
        String normalizedPeriod = normalizePeriod(period);
        TimeRange timeRange = getTimeRange(normalizedPeriod);
        List<Revenue> revenues = listRevenuesByTimeRange(timeRange);

        RevenueOverviewVO overview = new RevenueOverviewVO();
        overview.setPeriod(normalizedPeriod);
        overview.setTotalRevenue(revenues.stream()
            .map(Revenue::getAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        overview.setTotalDownloads(revenues.stream()
            .map(Revenue::getDownloadCount)
            .filter(count -> count != null)
            .reduce(0, Integer::sum));
        overview.setRevenueItemCount(revenues.size());

        log.info("Revenue overview loaded: period={}, start={}, end={}, totalRevenue={}, totalDownloads={}",
            normalizedPeriod, timeRange.start, timeRange.end, overview.getTotalRevenue(), overview.getTotalDownloads());
        return overview;
    }

    @Override
    public List<RevenueTypeVO> getRevenueByType(String period) {
        String normalizedPeriod = normalizePeriod(period);
        TimeRange timeRange = getTimeRange(normalizedPeriod);
        List<Revenue> revenues = listRevenuesByTimeRange(timeRange);
        Map<String, List<Revenue>> grouped = revenues.stream()
            .collect(Collectors.groupingBy(Revenue::getRevenueType));

        List<RevenueTypeVO> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : TYPE_NAME_MAP.entrySet()) {
            RevenueTypeVO vo = new RevenueTypeVO();
            vo.setRevenueType(entry.getKey());
            vo.setTypeName(entry.getValue());
            vo.setTotalAmount(BigDecimal.ZERO);
            vo.setDownloadCount(0);
            vo.setAccumulatedRevenue(BigDecimal.ZERO);

            List<Revenue> typedRevenues = grouped.get(entry.getKey());
            if (typedRevenues != null && !typedRevenues.isEmpty()) {
                BigDecimal totalAmount = typedRevenues.stream()
                    .map(Revenue::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                int downloadCount = typedRevenues.stream()
                    .map(Revenue::getDownloadCount)
                    .filter(count -> count != null)
                    .reduce(0, Integer::sum);
                vo.setTotalAmount(totalAmount);
                vo.setDownloadCount(downloadCount);
                vo.setAccumulatedRevenue(totalAmount);
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public PageResult<Revenue> getRevenueList(Integer pageNum, Integer pageSize, String period, String revenueType, String status) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, MAX_PAGE_SIZE);

        Page<Revenue> page = new Page<>(safePageNum, safePageSize);
        LambdaQueryWrapper<Revenue> wrapper = new LambdaQueryWrapper<>();

        if (period != null && !period.trim().isEmpty()) {
          TimeRange timeRange = getTimeRange(normalizePeriod(period));
          wrapper.ge(Revenue::getCreateTime, timeRange.start);
          wrapper.lt(Revenue::getCreateTime, timeRange.end);
        }

        if (revenueType != null && !revenueType.trim().isEmpty()) {
            wrapper.eq(Revenue::getRevenueType, revenueType);
        }

        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Revenue::getStatus, status);
        }

        wrapper.orderByDesc(Revenue::getCreateTime);

        IPage<Revenue> pageResult = revenueMapper.selectPage(page, wrapper);
        PageResult<Revenue> result = new PageResult<>(pageResult.getTotal(), pageResult.getRecords());
        result.setCurrent(pageResult.getCurrent());
        result.setSize(pageResult.getSize());
        result.setPages(pageResult.getPages());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRevenue(Long id) {
        log.info("Deleting revenue record: id={}", id);

        if (id == null || id <= 0) {
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "收益记录ID无效");
        }

        Revenue existingRevenue = revenueMapper.selectById(id);
        if (existingRevenue == null) {
            throw new ResourceNotFoundException("收益记录不存在");
        }

        int rows = revenueMapper.deleteById(id);
        if (rows <= 0) {
            throw new BusinessException("删除收益记录失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteRevenue(List<Long> ids) {
        log.info("Batch deleting revenue records: ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("删除ID列表不能为空");
        }

        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new BusinessException(BizErrorCode.PARAM_ERROR, "包含无效的收益记录ID: " + id);
            }
        }

        List<Revenue> existingRevenues = revenueMapper.selectBatchIds(ids);
        if (existingRevenues.size() != ids.size()) {
            throw new ResourceNotFoundException("部分收益记录不存在或已被删除");
        }

        int rows = revenueMapper.deleteBatchIds(ids);
        if (rows != ids.size()) {
            throw new BusinessException("批量删除收益记录失败");
        }
        return true;
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

    private String formatTime(LocalDateTime time) {
        return time.format(DATE_TIME_FORMATTER);
    }

    private List<Revenue> listRevenuesByTimeRange(TimeRange timeRange) {
        LambdaQueryWrapper<Revenue> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Revenue::getCreateTime, timeRange.start);
        wrapper.lt(Revenue::getCreateTime, timeRange.end);
        return revenueMapper.selectList(wrapper);
    }
}
