package com.resource.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resource.platform.entity.CrawlerTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CrawlerTaskMapper extends BaseMapper<CrawlerTask> {
    
    /**
     * 查询需要执行的任务（已启用且到达执行时间）
     */
    @Select("SELECT * FROM crawler_task WHERE status = 1 AND deleted = 0 " +
            "AND (next_execute_time IS NULL OR next_execute_time <= #{now})")
    List<CrawlerTask> selectTasksToExecute(LocalDateTime now);
    
    /**
     * 查询任务统计信息
     */
    @Select("SELECT COUNT(*) as total, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as enabled, " +
            "SUM(total_success) as totalSuccess, " +
            "SUM(total_failed) as totalFailed " +
            "FROM crawler_task WHERE deleted = 0")
    java.util.Map<String, Object> selectTaskStatistics();
}
