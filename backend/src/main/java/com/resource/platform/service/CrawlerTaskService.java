package com.resource.platform.service;

import com.resource.platform.common.PageResult;
import com.resource.platform.dto.CrawlerTaskDTO;
import com.resource.platform.dto.CrawlerTaskQueryDTO;
import com.resource.platform.vo.CrawlerTaskVO;

public interface CrawlerTaskService {
    
    /**
     * 创建爬虫任务
     */
    CrawlerTaskVO createTask(CrawlerTaskDTO dto);
    
    /**
     * 更新爬虫任务
     */
    CrawlerTaskVO updateTask(Long id, CrawlerTaskDTO dto);
    
    /**
     * 删除爬虫任务
     */
    void deleteTask(Long id, Boolean deleteResources);
    
    /**
     * 启用/禁用任务
     */
    CrawlerTaskVO toggleTaskStatus(Long id);
    
    /**
     * 分页查询任务
     */
    PageResult<CrawlerTaskVO> queryTasks(CrawlerTaskQueryDTO query);
    
    /**
     * 获取任务详情
     */
    CrawlerTaskVO getTaskDetail(Long id);
    
    /**
     * 手动触发任务
     */
    void triggerTask(Long id);
    
    /**
     * 验证目标URL
     */
    Boolean validateTargetUrl(String url);
}
