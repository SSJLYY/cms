package com.resource.platform.module.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.module.resource.entity.ResourceTag;
import com.resource.platform.module.resource.entity.ResourceTagRelation;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.exception.ValidationException;
import com.resource.platform.module.resource.mapper.ResourceTagMapper;
import com.resource.platform.module.resource.mapper.ResourceTagRelationMapper;
import com.resource.platform.module.resource.service.ResourceTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资源标签服务实现
 */
@Slf4j
@Service
public class ResourceTagServiceImpl implements ResourceTagService {
    private static final int DEFAULT_HOT_TAG_LIMIT = 10;
    private static final int MAX_HOT_TAG_LIMIT = 100;

    @Autowired
    private ResourceTagMapper resourceTagMapper;

    @Autowired
    private ResourceTagRelationMapper resourceTagRelationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
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
            limit = DEFAULT_HOT_TAG_LIMIT;
        } else if (limit > MAX_HOT_TAG_LIMIT) {
            limit = MAX_HOT_TAG_LIMIT;
        }
        
        return resourceTagMapper.selectList(
            new LambdaQueryWrapper<ResourceTag>()
                .orderByDesc(ResourceTag::getUseCount)
                .last("LIMIT " + limit)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void associateResourceWithTags(Long resourceId, List<Long> tagIds) {
        List<ResourceTagRelation> existingRelations = resourceTagRelationMapper.selectList(
            new LambdaQueryWrapper<ResourceTagRelation>()
                .eq(ResourceTagRelation::getResourceId, resourceId)
        );
        Set<Long> existingTagIds = existingRelations.stream()
            .map(ResourceTagRelation::getTagId)
            .collect(Collectors.toSet());
        Set<Long> uniqueTagIds = tagIds == null ? new LinkedHashSet<>() : tagIds.stream()
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // 删除现有关联
        resourceTagRelationMapper.delete(
            new LambdaQueryWrapper<ResourceTagRelation>()
                .eq(ResourceTagRelation::getResourceId, resourceId)
        );

        for (Long existingTagId : existingTagIds) {
            if (!uniqueTagIds.contains(existingTagId)) {
                ResourceTag tag = resourceTagMapper.selectById(existingTagId);
                if (tag != null) {
                    int currentUseCount = tag.getUseCount() == null ? 0 : tag.getUseCount();
                    tag.setUseCount(Math.max(0, currentUseCount - 1));
                    resourceTagMapper.updateById(tag);
                }
            }
        }

        if (uniqueTagIds.isEmpty()) {
            log.info("资源 {} 清空标签关联", resourceId);
            return;
        }
        
        // 创建新关联
        for (Long tagId : uniqueTagIds) {
            ResourceTagRelation relation = new ResourceTagRelation();
            relation.setResourceId(resourceId);
            relation.setTagId(tagId);
            resourceTagRelationMapper.insert(relation);
            
            // 更新标签使用次数
            if (!existingTagIds.contains(tagId)) {
                ResourceTag tag = resourceTagMapper.selectById(tagId);
                if (tag != null) {
                    int currentUseCount = tag.getUseCount() == null ? 0 : tag.getUseCount();
                    tag.setUseCount(currentUseCount + 1);
                    resourceTagMapper.updateById(tag);
                }
            }
        }
        
        log.info("关联资源 {} 和标签 {}", resourceId, uniqueTagIds);
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
    @Transactional(rollbackFor = Exception.class)
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
