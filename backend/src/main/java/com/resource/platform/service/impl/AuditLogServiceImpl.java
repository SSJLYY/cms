package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.dto.AuditLogQueryDTO;
import com.resource.platform.entity.AuditLog;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.mapper.AuditLogMapper;
import com.resource.platform.service.AuditLogService;
import com.resource.platform.vo.AuditLogStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 审计日志服务实现
 */
@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    public AuditLogStatisticsVO getStatistics() {
        AuditLogStatisticsVO statistics = new AuditLogStatisticsVO();
        
        // 总审计日志数
        Long totalAuditLogs = auditLogMapper.selectCount(null);
        statistics.setTotalAuditLogs(totalAuditLogs);
        
        // 今日审计日志数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayAuditLogs = auditLogMapper.selectCount(
            new LambdaQueryWrapper<AuditLog>()
                .between(AuditLog::getCreateTime, todayStart, todayEnd)
        );
        statistics.setTodayAuditLogs(todayAuditLogs);
        
        // 成功操作数
        Long successOperations = auditLogMapper.selectCount(
            new LambdaQueryWrapper<AuditLog>()
                .eq(AuditLog::getStatus, "SUCCESS")
        );
        statistics.setSuccessOperations(successOperations);
        
        // 失败操作数
        Long failedOperations = auditLogMapper.selectCount(
            new LambdaQueryWrapper<AuditLog>()
                .eq(AuditLog::getStatus, "FAILED")
        );
        statistics.setFailedOperations(failedOperations);
        
        return statistics;
    }

    @Override
    public Page<AuditLog> queryAuditLogs(AuditLogQueryDTO queryDTO) {
        Page<AuditLog> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        
        // 用户ID筛选
        if (queryDTO.getUserId() != null) {
            wrapper.eq(AuditLog::getUserId, queryDTO.getUserId());
        }
        
        // 模块筛选
        if (queryDTO.getModule() != null && !queryDTO.getModule().isEmpty()) {
            wrapper.eq(AuditLog::getModule, queryDTO.getModule());
        }
        
        // 操作筛选
        if (queryDTO.getAction() != null && !queryDTO.getAction().isEmpty()) {
            wrapper.eq(AuditLog::getAction, queryDTO.getAction());
        }
        
        // 状态筛选
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            wrapper.eq(AuditLog::getStatus, queryDTO.getStatus());
        }
        
        // IP地址筛选
        if (queryDTO.getIp() != null && !queryDTO.getIp().isEmpty()) {
            wrapper.eq(AuditLog::getIpAddress, queryDTO.getIp());
        }
        
        // 时间范围筛选
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(AuditLog::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(AuditLog::getCreateTime, queryDTO.getEndTime());
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(AuditLog::getCreateTime);
        
        return auditLogMapper.selectPage(page, wrapper);
    }

    @Override
    public AuditLog getAuditLogById(Long id) {
        AuditLog auditLog = auditLogMapper.selectById(id);
        if (auditLog == null) {
            throw new ResourceNotFoundException("审计日志", id);
        }
        return auditLog;
    }
}
