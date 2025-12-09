package com.resource.platform.service;

import com.resource.platform.common.PageResult;
import com.resource.platform.dto.ResourceDTO;
import com.resource.platform.dto.ResourceQueryDTO;
import com.resource.platform.vo.ResourceVO;
import java.util.List;

public interface ResourceService {
    /**
     * 获取已发布的资源列表
     */
    List<ResourceVO> getPublishedResources();
    
    /**
     * 分页查询资源（支持搜索和筛选）
     */
    PageResult<ResourceVO> queryResources(ResourceQueryDTO query);
    
    /**
     * 创建资源
     */
    ResourceVO createResource(ResourceDTO dto);
    
    /**
     * 更新资源
     */
    ResourceVO updateResource(Long id, ResourceDTO dto);
    
    /**
     * 删除资源
     */
    void deleteResource(Long id);
    
    /**
     * 切换资源状态
     */
    void toggleStatus(Long id);
    
    /**
     * 记录下载次数
     */
    void recordDownload(Long id);
    
    /**
     * 创建爬取的资源
     * @param dto 资源DTO
     * @param crawlerTaskId 爬虫任务ID
     * @param sourceUrl 来源URL
     * @param imageIds 图片ID列表
     * @return 创建的资源
     */
    ResourceVO createCrawledResource(ResourceDTO dto, Long crawlerTaskId, String sourceUrl, List<Long> imageIds);
    
    /**
     * 批量更新资源状态
     * @param ids 资源ID列表
     * @param status 目标状态：0-已下架，1-已发布
     * @return 更新成功的数量
     */
    int batchUpdateStatus(List<Long> ids, Integer status);
    
    /**
     * 批量删除资源
     * @param ids 资源ID列表
     * @return 删除成功的数量
     */
    int batchDelete(List<Long> ids);
    
    /**
     * 批量移动资源到指定分类
     * @param ids 资源ID列表
     * @param categoryId 目标分类ID
     * @return 移动成功的数量
     */
    int batchMoveToCategory(List<Long> ids, Long categoryId);
}
