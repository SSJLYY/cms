package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.entity.Revenue;
import com.resource.platform.mapper.RevenueMapper;
import com.resource.platform.service.RevenueService;
import com.resource.platform.vo.RevenueOverviewVO;
import com.resource.platform.vo.RevenueTypeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RevenueServiceImpl implements RevenueService {
    
    @Autowired
    private RevenueMapper revenueMapper;
    
    private static final Map<String, String> TYPE_NAME_MAP = new HashMap<>();
    
    static {
        TYPE_NAME_MAP.put("cloud_storage", "云存储");
        TYPE_NAME_MAP.put("download_revenue", "下载收益");
        TYPE_NAME_MAP.put("mobile_cloud", "移动云盘");
        TYPE_NAME_MAP.put("mobile_cloud_backup", "移动云盘（备用）");
        TYPE_NAME_MAP.put("uc_cloud", "UC网盘");
        TYPE_NAME_MAP.put("12_cloud", "12云盘");
        TYPE_NAME_MAP.put("lanzou_cloud", "蓝奏云");
        TYPE_NAME_MAP.put("chengtong_cloud", "城通网盘");
    }
    
    @Override
    public RevenueOverviewVO getOverview(String period) {
        RevenueOverviewVO overview = new RevenueOverviewVO();
        overview.setPeriod(period);
        
        String startTime = getStartTime(period);
        
        // 获取总收益
        BigDecimal totalRevenue = revenueMapper.getTotalRevenue(startTime);
        overview.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // 获取总下载次数
        Integer totalDownloads = revenueMapper.getTotalDownloads(startTime);
        overview.setTotalDownloads(totalDownloads != null ? totalDownloads : 0);
        
        // 获取收益项数量
        LambdaQueryWrapper<Revenue> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Revenue::getCreateTime, LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Long count = revenueMapper.selectCount(wrapper);
        overview.setRevenueItemCount(count.intValue());
        
        return overview;
    }
    
    @Override
    public List<RevenueTypeVO> getRevenueByType(String period) {
        String startTime = getStartTime(period);
        List<Map<String, Object>> dataList = revenueMapper.getRevenueByType(startTime);
        
        List<RevenueTypeVO> result = new ArrayList<>();
        
        // 确保所有类型都有数据，即使金额为0
        for (Map.Entry<String, String> entry : TYPE_NAME_MAP.entrySet()) {
            RevenueTypeVO vo = new RevenueTypeVO();
            vo.setRevenueType(entry.getKey());
            vo.setTypeName(entry.getValue());
            vo.setTotalAmount(BigDecimal.ZERO);
            vo.setDownloadCount(0);
            vo.setAccumulatedRevenue(BigDecimal.ZERO);
            
            // 查找对应的数据
            for (Map<String, Object> data : dataList) {
                if (entry.getKey().equals(data.get("revenue_type"))) {
                    vo.setTotalAmount((BigDecimal) data.get("total_amount"));
                    vo.setDownloadCount(((Number) data.get("total_downloads")).intValue());
                    vo.setAccumulatedRevenue((BigDecimal) data.get("total_amount"));
                    break;
                }
            }
            
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    public PageResult<Revenue> getRevenueList(Integer pageNum, Integer pageSize, String period, String revenueType, String status) {
        Page<Revenue> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Revenue> wrapper = new LambdaQueryWrapper<>();
        
        // 时间筛选
        if (period != null && !period.isEmpty()) {
            String startTime = getStartTime(period);
            wrapper.ge(Revenue::getCreateTime, LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        // 类型筛选
        if (revenueType != null && !revenueType.isEmpty()) {
            wrapper.eq(Revenue::getRevenueType, revenueType);
        }
        
        // 状态筛选
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Revenue::getStatus, status);
        }
        
        wrapper.orderByDesc(Revenue::getCreateTime);
        
        IPage<Revenue> pageResult = revenueMapper.selectPage(page, wrapper);
        
        return new PageResult<>(pageResult.getTotal(), pageResult.getRecords());
    }
    
    @Override
    public boolean deleteRevenue(Long id) {
        return revenueMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean batchDeleteRevenue(List<Long> ids) {
        return revenueMapper.deleteBatchIds(ids) > 0;
    }
    
    private String getStartTime(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        
        switch (period) {
            case "yesterday":
                startTime = now.minusDays(1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "week":
                startTime = now.minusDays(7);
                break;
            case "month":
                startTime = now.minusDays(30);
                break;
            case "today":
            default:
                startTime = now.withHour(0).withMinute(0).withSecond(0);
                break;
        }
        
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
