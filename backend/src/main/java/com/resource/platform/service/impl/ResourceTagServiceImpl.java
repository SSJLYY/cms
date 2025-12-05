package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.entity.ResourceTag;
import com.resource.platform.entity.ResourceTagRelation;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.exception.ValidationException;
import com.resource.platform.mapper.ResourceTagMapper;
import com.resource.platform.mapper.ResourceTagRelationMapper;
import com.resource.platform.service.ResourceTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源标签服务实现
 */
@Slf4j
@Service
public class ResourceTagServiceImpl implements ResourceTagService {

    @Autowired
    private ResourceTagMapper resourceTagMapper;

    @Autowired
    private ResourceTagRelationMapper resourceTagRelationMapper;

    @Override
    @Transactional
    public ResourceTag createTag(String tagName) {
        // 检查标签是否已存在
        ResourceTag existing = resourceTagMapper.selectOne(
            new LambdaQueryWrapper<ResourceTag>()
                .eq(ResourceTag::getTagName, tagName)
        );
        
        if (existing != null) {
            return existing;
        }
        
        // 创建新标签
        ResourceTag tag = new ResourceTag();
        tag.setTagName(tagName);
        tag.setUseCount(0);
        resourceTagMapper.insert(tag);
        
        log.info("创建标签: {}", tagName);
        return tag;
    }

    @Override
    public List<ResourceTag> getAllTags() {
        return resourceTagMapper.selectList(
            new LambdaQueryWrapper<ResourceTag>()
                .orderByDesc(ResourceTag::getUseCount)
        );
    }

    @Override
    public List<ResourceTag> searchTags(String keyword) {
        return resourceTagMapper.selectList(
            new LambdaQueryWrapper<ResourceTag>()
                .like(ResourceTag::getTagName, keyword)
                .orderByDesc(ResourceTag::getUseCount)
        );
    }

    @Override
    public List<ResourceTag> getHotTags(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        
        return resourceTagMapper.selectList(
            new LambdaQueryWrapper<ResourceTag>()
                .orderByDesc(ResourceTag::getUseCount)
                .last("LIMIT " + limit)
        );
    }

    @Override
    @Transactional
    public void associateResourceWithTags(Long resourceId, List<Long> tagIds) {
        // 删除现有关联
        resourceTagRelationMapper.delete(
            new LambdaQueryWrapper<ResourceTagRelation>()
                .eq(ResourceTagRelation::getResourceId, resourceId)
        );
        
        // 创建新关联
        for (Long tagId : tagIds) {
            ResourceTagRelation relation = new ResourceTagRelation();
            relation.setResourceId(resourceId);
            relation.setTagId(tagId);
            resourceTagRelationMapper.insert(relation);
            
            // 更新标签使用次数
            ResourceTag tag = resourceTagMapper.selectById(tagId);
            if (tag != null) {
                tag.setUseCount(tag.getUseCount() + 1);
                resourceTagMapper.updateById(tag);
            }
        }
        
        log.info("关联资源 {} 和标签 {}", resourceId, tagIds);
    }

    @Override
    public List<ResourceTag> getResourceTags(Long resourceId) {
        // 查询资源的标签关联
        List<ResourceTagRelation> relations = resourceTagRelationMapper.selectList(
            new LambdaQueryWrapper<ResourceTagRelation>()
                .eq(ResourceTagRelation::getResourceId, resourceId)
        );
        
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取标签ID列表
        List<Long> tagIds = relations.stream()
            .map(ResourceTagRelation::getTagId)
            .collect(Collectors.toList());
        
        // 查询标签详情
        return resourceTagMapper.selectBatchIds(tagIds);
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        ResourceTag tag = resourceTagMapper.selectById(tagId);
        if (tag == null) {
            throw new ResourceNotFoundException("标签", tagId);
        }
        
        // 检查是否有资源使用该标签
        Long count = resourceTagRelationMapper.selectCount(
            new LambdaQueryWrapper<ResourceTagRelation>()
                .eq(ResourceTagRelation::getTagId, tagId)
        );
        
        if (count > 0) {
            throw new ValidationException("该标签正在被使用，无法删除");
        }
        
        resourceTagMapper.deleteById(tagId);
        log.info("删除标签: {}", tag.getTagName());
    }

    @Override
    public Long getTagCount() {
        return resourceTagMapper.selectCount(null);
    }
}
