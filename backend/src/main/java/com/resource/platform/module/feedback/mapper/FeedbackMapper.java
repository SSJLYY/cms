package com.resource.platform.module.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resource.platform.module.feedback.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    /**
     * 一次 SQL 查询统计各维度反馈数量，避免多次 selectCount 往返
     * 返回：total, pending, processed, today
     */
    @Select("SELECT " +
            "  COUNT(*) AS total, " +
            "  SUM(status IN ('PENDING', 'PROCESSING')) AS pending, " +
            "  SUM(status IN ('COMPLETED', 'CLOSED')) AS processed, " +
            "  SUM(DATE(create_time) = CURDATE()) AS today " +
            "FROM feedback WHERE deleted = 0")
    Map<String, Long> selectStatsSummary();
}

