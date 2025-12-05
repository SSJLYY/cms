package com.resource.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.dto.AuditLogQueryDTO;
import com.resource.platform.entity.AuditLog;
import com.resource.platform.vo.AuditLogStatisticsVO;

/**
 * 审计日志服务接口
 */
public interface AuditLogService {
    
    /**
     * 获取审计日志统计信息
     */
    AuditLogStatisticsVO getStatistics();
    
    /**
     * 查询审计日志列表（分页）
     */
    Page<AuditLog> queryAuditLogs(AuditLogQueryDTO queryDTO);
    
    /**
     * 获取审计日志详情
     */
    AuditLog getAuditLogById(Long id);
}
