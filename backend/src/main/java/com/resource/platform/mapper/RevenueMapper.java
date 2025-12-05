package com.resource.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resource.platform.entity.Revenue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface RevenueMapper extends BaseMapper<Revenue> {
    
    /**
     * 按收益类型统计总金额
     */
    @Select("SELECT revenue_type, SUM(amount) as total_amount, SUM(download_count) as total_downloads " +
            "FROM revenue " +
            "WHERE create_time >= #{startTime} " +
            "GROUP BY revenue_type")
    List<Map<String, Object>> getRevenueByType(String startTime);
    
    /**
     * 获取总收益
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM revenue WHERE create_time >= #{startTime}")
    BigDecimal getTotalRevenue(String startTime);
    
    /**
     * 获取总下载次数
     */
    @Select("SELECT COALESCE(SUM(download_count), 0) FROM revenue WHERE create_time >= #{startTime}")
    Integer getTotalDownloads(String startTime);
}
