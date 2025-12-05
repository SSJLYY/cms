package com.resource.platform.service;

import com.resource.platform.common.PageResult;
import com.resource.platform.entity.SEOSubmission;
import com.resource.platform.vo.SEOStatsVO;
import com.resource.platform.vo.SubmissionResultVO;

public interface SEOService {
    /**
     * 获取SEO统计
     */
    SEOStatsVO getStats();
    
    /**
     * 生成网站地图
     */
    String generateSitemap();
    
    /**
     * 批量提交到指定搜索引擎
     */
    SubmissionResultVO batchSubmit(String engine);
    
    /**
     * 批量提交到所有搜索引擎
     */
    SubmissionResultVO batchSubmitAll();
    
    /**
     * 获取提交历史（分页）
     */
    PageResult<SEOSubmission> getSubmissionHistory(Integer page, Integer pageSize);
    
    /**
     * 重新提交
     */
    SubmissionResultVO resubmit(Long id);
    
    /**
     * 删除提交记录
     */
    void deleteHistory(Long id);
}
