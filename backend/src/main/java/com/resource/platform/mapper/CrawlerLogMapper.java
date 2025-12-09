package com.resource.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resource.platform.entity.CrawlerLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CrawlerLogMapper extends BaseMapper<CrawlerLog> {
    
    /**
     * 查询任务的最近执行日志
     */
    @Select("SELECT * FROM crawler_log WHERE task_id = #{taskId} " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<CrawlerLog> selectRecentLogsByTaskId(Long taskId, int limit);
    
    /**
     * 查询任务的执行统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalExecutions, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as successExecutions, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as failedExecutions, " +
            "SUM(success_count) as totalSuccessResources, " +
            "SUM(failed_count) as totalFailedResources " +
            "FROM crawler_log WHERE task_id = #{taskId}")
    Map<String, Object> selectTaskExecutionStatistics(Long taskId);
}
