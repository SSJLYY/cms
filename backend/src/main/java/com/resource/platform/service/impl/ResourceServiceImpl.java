package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.dto.DownloadLinkDTO;
import com.resource.platform.dto.ResourceDTO;
import com.resource.platform.dto.ResourceQueryDTO;
import com.resource.platform.entity.Category;
import com.resource.platform.entity.DownloadLink;
import com.resource.platform.entity.Image;
import com.resource.platform.entity.Resource;
import com.resource.platform.entity.ResourceImage;
import com.resource.platform.mapper.CategoryMapper;
import com.resource.platform.mapper.DownloadLinkMapper;
import com.resource.platform.mapper.ImageMapper;
import com.resource.platform.mapper.ResourceMapper;
import com.resource.platform.mapper.ResourceImageMapper;
import com.resource.platform.service.ImageService;
import com.resource.platform.service.ResourceService;
import com.resource.platform.vo.DownloadLinkVO;
import com.resource.platform.vo.ImageVO;
import com.resource.platform.vo.ResourceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DownloadLinkMapper downloadLinkMapper;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private ResourceImageMapper resourceImageMapper;
    
    @Autowired
    private ImageService imageService;

    @Override
    public List<ResourceVO> getPublishedResources() {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 1);
        wrapper.orderByDesc(Resource::getCreateTime);
        
        List<Resource> resources = resourceMapper.selectList(wrapper);
        return resources.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<ResourceVO> queryResources(ResourceQueryDTO query) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Resource::getTitle, query.getKeyword())
                    .or().like(Resource::getDescription, query.getKeyword()));
        }
        
        // 分类筛选
        if (query.getCategoryId() != null) {
            wrapper.eq(Resource::getCategoryId, query.getCategoryId());
        }
        
        // 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(Resource::getStatus, query.getStatus());
        }
        
        // 排序
        if (StringUtils.hasText(query.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());
            switch (query.getSortField()) {
                case "createTime":
                    wrapper.orderBy(true, isAsc, Resource::getCreateTime);
                    break;
                case "downloadCount":
                    wrapper.orderBy(true, isAsc, Resource::getDownloadCount);
                    break;
                default:
                    wrapper.orderByDesc(Resource::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Resource::getCreateTime);
        }
        
        Page<Resource> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Resource> resultPage = resourceMapper.selectPage(page, wrapper);
        
        List<ResourceVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(resultPage.getTotal(), voList);
    }

    @Override
    @Transactional
    public ResourceVO createResource(ResourceDTO dto) {
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        resource.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        resource.setDownloadCount(0);
        resource.setViewCount(0);
        resource.setSortOrder(0);
        resourceMapper.insert(resource);

        // 保存下载链接
        if (dto.getDownloadLinks() != null && !dto.getDownloadLinks().isEmpty()) {
            int sortOrder = 0;
            for (DownloadLinkDTO linkDTO : dto.getDownloadLinks()) {
                DownloadLink link = new DownloadLink();
                BeanUtils.copyProperties(linkDTO, link);
                link.setResourceId(resource.getId());
                link.setIsValid(1);
                link.setSortOrder(sortOrder++);
                downloadLinkMapper.insert(link);
            }
        }

        // 保存资源图片关联（最多5张）
        saveResourceImages(resource.getId(), dto.getImageIds(), dto.getCoverImageId());

        return convertToVO(resource);
    }

    @Override
    @Transactional
    public ResourceVO updateResource(Long id, ResourceDTO dto) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        // 1. 获取旧的图片关联
        LambdaQueryWrapper<ResourceImage> oldImageWrapper = new LambdaQueryWrapper<>();
        oldImageWrapper.eq(ResourceImage::getResourceId, id);
        List<ResourceImage> oldImages = resourceImageMapper.selectList(oldImageWrapper);
        Set<Long> oldImageIds = oldImages.stream()
            .map(ResourceImage::getImageId)
            .collect(Collectors.toSet());

        // 2. 更新资源基本信息
        BeanUtils.copyProperties(dto, resource);
        resource.setId(id);
        resourceMapper.updateById(resource);

        // 3. 删除旧的下载链接
        LambdaQueryWrapper<DownloadLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLink::getResourceId, id);
        downloadLinkMapper.delete(wrapper);

        // 4. 保存新的下载链接
        if (dto.getDownloadLinks() != null && !dto.getDownloadLinks().isEmpty()) {
            int sortOrder = 0;
            for (DownloadLinkDTO linkDTO : dto.getDownloadLinks()) {
                DownloadLink link = new DownloadLink();
                BeanUtils.copyProperties(linkDTO, link);
                link.setResourceId(id);
                link.setIsValid(1);
                link.setSortOrder(sortOrder++);
                downloadLinkMapper.insert(link);
            }
        }

        // 5. 删除旧的资源图片关联
        resourceImageMapper.delete(oldImageWrapper);

        // 6. 保存新的资源图片关联（最多5张）
        Set<Long> newImageIds = dto.getImageIds() != null ? 
            new HashSet<>(dto.getImageIds()) : new HashSet<>();
        saveResourceImages(id, dto.getImageIds(), dto.getCoverImageId());

        // 7. 找出被移除的图片，更新它们的状态
        Set<Long> removedImageIds = new HashSet<>(oldImageIds);
        removedImageIds.removeAll(newImageIds);
        if (!removedImageIds.isEmpty()) {
            imageService.batchUpdateImageUsageStatus(new ArrayList<>(removedImageIds));
        }

        return convertToVO(resource);
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        // 1. 获取资源关联的所有图片
        LambdaQueryWrapper<ResourceImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceImage::getResourceId, id);
        List<ResourceImage> resourceImages = resourceImageMapper.selectList(wrapper);
        List<Long> imageIds = resourceImages.stream()
            .map(ResourceImage::getImageId)
            .collect(Collectors.toList());

        // 2. 删除资源
        resourceMapper.deleteById(id);

        // 3. 删除资源图片关联
        resourceImageMapper.delete(wrapper);

        // 4. 删除下载链接
        LambdaQueryWrapper<DownloadLink> linkWrapper = new LambdaQueryWrapper<>();
        linkWrapper.eq(DownloadLink::getResourceId, id);
        downloadLinkMapper.delete(linkWrapper);

        // 5. 更新相关图片的使用状态
        if (!imageIds.isEmpty()) {
            imageService.batchUpdateImageUsageStatus(imageIds);
        }
    }

    @Override
    public void toggleStatus(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }
        
        resource.setStatus(resource.getStatus() == 1 ? 0 : 1);
        resourceMapper.updateById(resource);
    }

    @Override
    public void recordDownload(Long id) {
        LambdaUpdateWrapper<Resource> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Resource::getId, id);
        wrapper.setSql("download_count = download_count + 1");
        resourceMapper.update(null, wrapper);
    }

    /**
     * 保存资源图片关联
     */
    private void saveResourceImages(Long resourceId, List<Long> imageIds, Long coverImageId) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }
        
        // 限制最多5张图片
        List<Long> limitedImageIds = imageIds.stream()
                .limit(5)
                .collect(Collectors.toList());
        
        int sortOrder = 0;
        for (Long imageId : limitedImageIds) {
            ResourceImage resourceImage = new ResourceImage();
            resourceImage.setResourceId(resourceId);
            resourceImage.setImageId(imageId);
            resourceImage.setIsCover(imageId.equals(coverImageId) ? 1 : 0);
            resourceImage.setSortOrder(sortOrder++);
            resourceImageMapper.insert(resourceImage);
        }
        
        // 批量更新图片使用状态
        imageService.batchUpdateImageUsageStatus(limitedImageIds);
    }

    private ResourceVO convertToVO(Resource resource) {
        ResourceVO vo = new ResourceVO();
        BeanUtils.copyProperties(resource, vo);
        
        // 获取分类名称
        Category category = categoryMapper.selectById(resource.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        
        // 获取封面图片URL
        if (resource.getCoverImageId() != null) {
            Image coverImage = imageMapper.selectById(resource.getCoverImageId());
            if (coverImage != null) {
                vo.setCoverImageUrl(coverImage.getFileUrl());
            }
        }
        
        // 获取资源关联的所有图片
        LambdaQueryWrapper<ResourceImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ResourceImage::getResourceId, resource.getId());
        imageWrapper.orderByAsc(ResourceImage::getSortOrder);
        List<ResourceImage> resourceImages = resourceImageMapper.selectList(imageWrapper);
        
        List<ImageVO> imageVOs = resourceImages.stream().map(ri -> {
            Image image = imageMapper.selectById(ri.getImageId());
            if (image != null) {
                ImageVO imageVO = new ImageVO();
                BeanUtils.copyProperties(image, imageVO);
                return imageVO;
            }
            return null;
        }).filter(img -> img != null).collect(Collectors.toList());
        
        vo.setImages(imageVOs);
        
        // 获取下载链接
        LambdaQueryWrapper<DownloadLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLink::getResourceId, resource.getId());
        wrapper.orderByAsc(DownloadLink::getSortOrder);
        List<DownloadLink> links = downloadLinkMapper.selectList(wrapper);
        
        List<DownloadLinkVO> linkVOs = links.stream().map(link -> {
            DownloadLinkVO linkVO = new DownloadLinkVO();
            BeanUtils.copyProperties(link, linkVO);
            return linkVO;
        }).collect(Collectors.toList());
        
        vo.setDownloadLinks(linkVOs);
        return vo;
    }
}
