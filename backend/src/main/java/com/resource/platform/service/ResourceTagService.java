package com.resource.platform.service;

import com.resource.platform.entity.ResourceTag;

import java.util.List;

/**
 * 资源标签服务接口
 */
public interface ResourceTagService {
    
    /**
     * 创建标签
     */
    ResourceTag createTag(String tagName);
    
    /**
     * 获取所有标签
     */
    List<ResourceTag> getAllTags();
    
    /**
     * 根据名称搜索标签
     */
    List<ResourceTag> searchTags(String keyword);
    
    /**
     * 获取热门标签
     */
    List<ResourceTag> getHotTags(Integer limit);
    
    /**
     * 关联资源和标签
     */
    void associateResourceWithTags(Long resourceId, List<Long> tagIds);
    
    /**
     * 获取资源的标签
     */
    List<ResourceTag> getResourceTags(Long resourceId);
    
    /**
     * 删除标签
     */
    void deleteTag(Long tagId);
    
    /**
     * 获取标签统计
     */
    Long getTagCount();
}
