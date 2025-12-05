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
}
