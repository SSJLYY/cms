package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.dto.CrawlerLogQueryDTO;
import com.resource.platform.entity.CrawlerLog;
import com.resource.platform.mapper.CrawlerLogMapper;
import com.resource.platform.service.CrawlerLogService;
import com.resource.platform.vo.CrawlerLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CrawlerLogServiceImpl implements CrawlerLogService {

    @Autowired
    private CrawlerLogMapper crawlerLogMapper;

    @Override
    public CrawlerLog createLog(CrawlerLog log) {
        crawlerLogMapper.insert(log);
        return log;
    }

    @Override
    public void updateLog(CrawlerLog log) {
        crawlerLogMapper.updateById(log);
    }

    @Override
    public PageResult<CrawlerLogVO> queryLogs(CrawlerLogQueryDTO query) {
        LambdaQueryWrapper<CrawlerLog> wrapper = new LambdaQueryWrapper<>();
        
        // 任务ID筛选
        if (query.getTaskId() != null) {
            wrapper.eq(CrawlerLog::getTaskId, query.getTaskId());
        }
        
        // 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(CrawlerLog::getStatus, query.getStatus());
        }
        
        // 时间范围筛选
        if (query.getStartTime() != null) {
            wrapper.ge(CrawlerLog::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(CrawlerLog::getCreateTime, query.getEndTime());
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(CrawlerLog::getCreateTime);
        
        Page<CrawlerLog> page = new Page<>(query.getPage(), query.getPageSize());
        Page<CrawlerLog> resultPage = crawlerLogMapper.selectPage(page, wrapper);
        
        List<CrawlerLogVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    public CrawlerLogVO getLogDetail(Long id) {
        CrawlerLog log = crawlerLogMapper.selectById(id);
        if (log == null) {
            return null;
        }
        return convertToVO(log);
    }

    /**
     * 转换为VO
     */
    private CrawlerLogVO convertToVO(CrawlerLog log) {
        CrawlerLogVO vo = new CrawlerLogVO();
        BeanUtils.copyProperties(log, vo);
        
        // 设置执行类型文本
        if (log.getExecuteType() != null) {
            vo.setExecuteTypeText(log.getExecuteType() == 1 ? "定时" : "手动");
        }
        
        // 设置状态文本
        if (log.getStatus() != null) {
            switch (log.getStatus()) {
                case 1:
                    vo.setStatusText("执行中");
                    break;
                case 2:
                    vo.setStatusText("成功");
                    break;
                case 3:
                    vo.setStatusText("失败");
                    break;
                default:
                    vo.setStatusText("未知");
            }
        }
        
        return vo;
    }
}
