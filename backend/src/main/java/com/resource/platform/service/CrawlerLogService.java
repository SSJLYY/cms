package com.resource.platform.service;

import com.resource.platform.common.PageResult;
import com.resource.platform.dto.CrawlerLogQueryDTO;
import com.resource.platform.entity.CrawlerLog;
import com.resource.platform.vo.CrawlerLogVO;

public interface CrawlerLogService {
    
    /**
     * 创建执行日志
     */
    CrawlerLog createLog(CrawlerLog log);
    
    /**
     * 更新执行日志
     */
    void updateLog(CrawlerLog log);
    
    /**
     * 分页查询日志
     */
    PageResult<CrawlerLogVO> queryLogs(CrawlerLogQueryDTO query);
    
    /**
     * 获取日志详情
     */
    CrawlerLogVO getLogDetail(Long id);
}
