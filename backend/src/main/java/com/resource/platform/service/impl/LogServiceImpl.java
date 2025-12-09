package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.dto.LogQueryDTO;
import com.resource.platform.entity.SystemLog;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.mapper.SystemLogMapper;
import com.resource.platform.service.LogService;
import com.resource.platform.vo.LogStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 日志服务实现类
 * 
 * 功能说明：
 * - 实现系统日志的核心业务逻辑
 * - 处理日志的查询、统计和管理
 * - 提供日志的清理和导出功能
 * - 支持多维度的日志筛选和分析
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private SystemLogMapper systemLogMapper;

    /**
     * 获取日志统计信息
     * 
     * 业务逻辑：
     * 1. 统计总日志数量
     * 2. 统计今日日志数量
     * 3. 统计成功和失败日志数量
     * 4. 计算平均响应时间
     * 5. 返回统计结果
     * 
     * @return 日志统计信息
     */
    @Override
    public LogStatisticsVO getStatistics() {
        // 记录统计开始
        log.info("开始获取日志统计信息");
        
        // 创建统计结果对象
        LogStatisticsVO statistics = new LogStatisticsVO();
        
        // 步骤1：统计总日志数
        log.debug("统计总日志数量");
        Long totalLogs = systemLogMapper.selectCount(null);
        statistics.setTotalLogs(totalLogs);
        log.debug("总日志数: {}", totalLogs);
        
        // 步骤2：统计今日日志数
        log.debug("统计今日日志数量");
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayLogs = systemLogMapper.selectCount(
            new LambdaQueryWrapper<SystemLog>()
                .between(SystemLog::getCreateTime, todayStart, todayEnd)
        );
        statistics.setTodayLogs(todayLogs);
        log.debug("今日日志数: {}", todayLogs);
        
        // 步骤3：统计成功日志数
        log.debug("统计成功日志数量");
        Long successLogs = systemLogMapper.selectCount(
            new LambdaQueryWrapper<SystemLog>()
                .eq(SystemLog::getStatus, "SUCCESS")
        );
        statistics.setSuccessLogs(successLogs);
        log.debug("成功日志数: {}", successLogs);
        
        // 步骤4：统计失败日志数
        log.debug("统计失败日志数量");
        Long errorLogs = systemLogMapper.selectCount(
            new LambdaQueryWrapper<SystemLog>()
                .eq(SystemLog::getStatus, "ERROR")
        );
        statistics.setErrorLogs(errorLogs);
        log.debug("失败日志数: {}", errorLogs);
        
        // 步骤5：计算平均响应时间
        log.debug("计算平均响应时间");
        List<SystemLog> allLogs = systemLogMapper.selectList(
            new LambdaQueryWrapper<SystemLog>()
                .isNotNull(SystemLog::getDuration)
        );
        
        long avgDuration;
        if (!allLogs.isEmpty()) {
            // 计算所有日志的总耗时
            long totalDuration = allLogs.stream()
                .mapToLong(SystemLog::getDuration)
                .sum();
            // 计算平均值
            avgDuration = totalDuration / allLogs.size();
        } else {
            avgDuration = 0L;
        }
        statistics.setAvgDuration(avgDuration);
        log.debug("平均响应时间: {}ms", avgDuration);
        
        // 记录统计成功
        log.info("获取日志统计信息成功: total={}, today={}, success={}, error={}, avgDuration={}ms", 
            totalLogs, todayLogs, successLogs, errorLogs, avgDuration);
        
        return statistics;
    }

    @Override
    public Page<SystemLog> queryLogs(LogQueryDTO queryDTO) {
        Page<SystemLog> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        
        // 模块筛选
        if (queryDTO.getModule() != null && !queryDTO.getModule().isEmpty()) {
            wrapper.eq(SystemLog::getModule, queryDTO.getModule());
        }
        
        // 类型筛选
        if (queryDTO.getType() != null && !queryDTO.getType().isEmpty()) {
            wrapper.eq(SystemLog::getType, queryDTO.getType());
        }
        
        // 状态筛选
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            wrapper.eq(SystemLog::getStatus, queryDTO.getStatus());
        }
        
        // 时间范围筛选
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(SystemLog::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(SystemLog::getCreateTime, queryDTO.getEndTime());
        }
        
        // 关键词搜索
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w
                .like(SystemLog::getDescription, queryDTO.getKeyword())
                .or()
                .like(SystemLog::getRequestUrl, queryDTO.getKeyword())
                .or()
                .like(SystemLog::getErrorMessage, queryDTO.getKeyword())
            );
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(SystemLog::getCreateTime);
        
        return systemLogMapper.selectPage(page, wrapper);
    }

    @Override
    public SystemLog getLogById(Long id) {
        SystemLog log = systemLogMapper.selectById(id);
        if (log == null) {
            throw new ResourceNotFoundException("日志", id);
        }
        return log;
    }

    @Override
    public int cleanLogs(LocalDateTime beforeTime) {
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(SystemLog::getCreateTime, beforeTime);
        
        int count = systemLogMapper.selectCount(wrapper).intValue();
        systemLogMapper.delete(wrapper);
        
        log.info("清理了 {} 条日志记录（{}之前）", count, beforeTime);
        return count;
    }

    @Override
    public String exportLogs(LogQueryDTO queryDTO) {
        // 查询所有符合条件的日志
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getModule() != null && !queryDTO.getModule().isEmpty()) {
            wrapper.eq(SystemLog::getModule, queryDTO.getModule());
        }
        if (queryDTO.getType() != null && !queryDTO.getType().isEmpty()) {
            wrapper.eq(SystemLog::getType, queryDTO.getType());
        }
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            wrapper.eq(SystemLog::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(SystemLog::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(SystemLog::getCreateTime, queryDTO.getEndTime());
        }
        
        wrapper.orderByDesc(SystemLog::getCreateTime);
        
        List<SystemLog> logs = systemLogMapper.selectList(wrapper);
        
        // 生成CSV内容
        StringBuilder csv = new StringBuilder();
        csv.append("ID,模块,类型,描述,请求URL,请求方法,IP地址,状态,耗时(ms),创建时间\n");
        
        for (SystemLog log : logs) {
            csv.append(log.getId()).append(",")
               .append(log.getModule()).append(",")
               .append(log.getType()).append(",")
               .append(log.getDescription()).append(",")
               .append(log.getRequestUrl()).append(",")
               .append(log.getRequestMethod()).append(",")
               .append(log.getIpAddress()).append(",")
               .append(log.getStatus()).append(",")
               .append(log.getDuration()).append(",")
               .append(log.getCreateTime()).append("\n");
        }
        
        // 保存到临时文件
        try {
            String fileName = "logs_export_" + System.currentTimeMillis() + ".csv";
            String filePath = "/tmp/" + fileName;
            File file = new File(filePath);
            FileUtils.writeStringToFile(file, csv.toString(), StandardCharsets.UTF_8);
            
            log.info("导出日志成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            log.error("导出日志失败", e);
            throw new RuntimeException("导出日志失败: " + e.getMessage());
        }
    }
}
