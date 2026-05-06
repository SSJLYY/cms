package com.resource.platform.module.crawler.service;

import com.resource.platform.common.PageResult;
import com.resource.platform.module.crawler.dto.CrawlerLogQueryDTO;
import com.resource.platform.module.crawler.entity.CrawlerLog;
import com.resource.platform.module.crawler.vo.CrawlerLogVO;

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
