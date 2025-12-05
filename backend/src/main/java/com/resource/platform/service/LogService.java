package com.resource.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.dto.LogQueryDTO;
import com.resource.platform.entity.SystemLog;
import com.resource.platform.vo.LogStatisticsVO;

import java.time.LocalDateTime;

/**
 * 日志服务接口
 */
public interface LogService {
    
    /**
     * 获取日志统计信息
     */
    LogStatisticsVO getStatistics();
    
    /**
     * 查询日志列表（分页）
     */
    Page<SystemLog> queryLogs(LogQueryDTO queryDTO);
    
    /**
     * 获取日志详情
     */
    SystemLog getLogById(Long id);
    
    /**
     * 清理日志
     * 
     * @param beforeTime 清理此时间之前的日志
     * @return 清理的日志数量
     */
    int cleanLogs(LocalDateTime beforeTime);
    
    /**
     * 导出日志
     * 
     * @param queryDTO 查询条件
     * @return 导出文件路径
     */
    String exportLogs(LogQueryDTO queryDTO);
}
