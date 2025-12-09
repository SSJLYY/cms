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
import lombok.extern.slf4j.Slf4j;
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

/**
 * 资源服务实现类
 * 
 * 功能说明：
 * - 实现资源的核心业务逻辑
 * - 处理资源的CRUD操作
 * - 管理资源与图片、下载链接的关联关系
 * - 处理资源的状态切换和下载统计
 * - 支持资源的分页查询和条件筛选
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
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

    /**
     * 获取已发布的资源列表
     * 
     * 业务逻辑：
     * 1. 构建查询条件（状态=1表示已发布）
     * 2. 按创建时间倒序排列
     * 3. 查询数据库
     * 4. 转换为VO对象列表
     * 
     * @return 已发布的资源列表
     */
    @Override
    public List<ResourceVO> getPublishedResources() {
        // 记录查询开始
        log.info("开始查询已发布资源列表");
        
        // 步骤1：构建查询条件
        // 只查询状态为1（已发布）的资源
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 1);
        
        // 步骤2：按创建时间倒序排列
        // 最新创建的资源排在前面
        wrapper.orderByDesc(Resource::getCreateTime);
        
        // 步骤3：执行数据库查询
        log.debug("执行数据库查询: status=1, orderBy=createTime DESC");
        List<Resource> resources = resourceMapper.selectList(wrapper);
        
        // 步骤4：转换为VO对象
        // 将实体对象转换为视图对象，包含关联的分类、图片、下载链接等信息
        List<ResourceVO> result = resources.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        // 记录查询成功
        log.info("查询已发布资源列表成功: count={}", result.size());
        
        return result;
    }

    /**
     * 分页查询资源列表
     * 
     * 业务逻辑：
     * 1. 根据关键词搜索（标题或描述）
     * 2. 根据分类ID筛选
     * 3. 根据状态筛选
     * 4. 根据指定字段排序
     * 5. 执行分页查询
     * 6. 转换为VO对象列表
     * 
     * @param query 查询条件对象
     * @return 分页结果
     */
    @Override
    public PageResult<ResourceVO> queryResources(ResourceQueryDTO query) {
        // 记录查询开始
        log.info("开始分页查询资源: page={}, pageSize={}, keyword={}, categoryId={}, status={}", 
            query.getPageNum(), query.getPageSize(), query.getKeyword(), 
            query.getCategoryId(), query.getStatus());
        
        // 构建查询条件
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        
        // 步骤1：关键词搜索
        // 在标题或描述中模糊匹配关键词
        if (StringUtils.hasText(query.getKeyword())) {
            log.debug("添加关键词搜索条件: keyword={}", query.getKeyword());
            wrapper.and(w -> w.like(Resource::getTitle, query.getKeyword())
                    .or().like(Resource::getDescription, query.getKeyword()));
        }
        
        // 步骤2：分类筛选
        // 根据分类ID精确匹配
        if (query.getCategoryId() != null) {
            log.debug("添加分类筛选条件: categoryId={}", query.getCategoryId());
            wrapper.eq(Resource::getCategoryId, query.getCategoryId());
        }
        
        // 步骤3：状态筛选
        // 根据状态精确匹配（0=下架，1=上架）
        if (query.getStatus() != null) {
            log.debug("添加状态筛选条件: status={}", query.getStatus());
            wrapper.eq(Resource::getStatus, query.getStatus());
        }
        
        // 步骤4：排序设置
        // 支持按创建时间或下载次数排序
        if (StringUtils.hasText(query.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());
            log.debug("添加排序条件: sortField={}, sortOrder={}", query.getSortField(), query.getSortOrder());
            
            switch (query.getSortField()) {
                case "createTime":
                    // 按创建时间排序
                    wrapper.orderBy(true, isAsc, Resource::getCreateTime);
                    break;
                case "downloadCount":
                    // 按下载次数排序
                    wrapper.orderBy(true, isAsc, Resource::getDownloadCount);
                    break;
                default:
                    // 默认按创建时间倒序
                    wrapper.orderByDesc(Resource::getCreateTime);
            }
        } else {
            // 未指定排序字段时，默认按创建时间倒序
            wrapper.orderByDesc(Resource::getCreateTime);
        }
        
        // 步骤5：执行分页查询
        Page<Resource> page = new Page<>(query.getPageNum(), query.getPageSize());
        log.debug("执行分页查询: page={}, pageSize={}", query.getPageNum(), query.getPageSize());
        Page<Resource> resultPage = resourceMapper.selectPage(page, wrapper);
        
        // 步骤6：转换为VO对象列表
        // 包含关联的分类、图片、下载链接等信息
        List<ResourceVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 记录查询成功
        log.info("分页查询资源成功: total={}, records={}", resultPage.getTotal(), voList.size());
        
        return new PageResult<>(resultPage.getTotal(), voList);
    }

    /**
     * 创建资源
     * 
     * 业务逻辑：
     * 1. 复制DTO数据到实体对象
     * 2. 设置默认值（状态、计数器、排序）
     * 3. 保存资源基本信息
     * 4. 保存资源关联的下载链接
     * 5. 保存资源关联的图片（最多5张）
     * 6. 转换为VO对象返回
     * 
     * @param dto 资源数据传输对象
     * @return 创建成功的资源VO对象
     */
    @Override
    @Transactional
    public ResourceVO createResource(ResourceDTO dto) {
        // 记录创建开始
        log.info("开始创建资源: title={}, categoryId={}", dto.getTitle(), dto.getCategoryId());
        
        // 步骤1：创建资源实体对象
        // 将DTO中的数据复制到实体对象
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        
        // 步骤2：设置默认值
        // 如果未指定状态，默认为1（上架）
        resource.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        // 初始化下载次数为0
        resource.setDownloadCount(0);
        // 初始化浏览次数为0
        resource.setViewCount(0);
        // 初始化排序值为0
        resource.setSortOrder(0);
        
        // 步骤3：保存资源基本信息到数据库
        log.debug("保存资源基本信息: title={}", dto.getTitle());
        resourceMapper.insert(resource);
        log.debug("资源基本信息保存成功: resourceId={}", resource.getId());

        // 步骤4：保存下载链接
        // 如果提供了下载链接列表，逐个保存
        if (dto.getDownloadLinks() != null && !dto.getDownloadLinks().isEmpty()) {
            log.debug("开始保存下载链接: resourceId={}, linkCount={}", 
                resource.getId(), dto.getDownloadLinks().size());
            
            int sortOrder = 0;
            for (DownloadLinkDTO linkDTO : dto.getDownloadLinks()) {
                // 创建下载链接实体
                DownloadLink link = new DownloadLink();
                BeanUtils.copyProperties(linkDTO, link);
                // 关联到当前资源
                link.setResourceId(resource.getId());
                // 设置为有效状态
                link.setIsValid(1);
                // 设置排序值
                link.setSortOrder(sortOrder++);
                // 保存到数据库
                downloadLinkMapper.insert(link);
            }
            
            log.debug("下载链接保存成功: resourceId={}, linkCount={}", 
                resource.getId(), dto.getDownloadLinks().size());
        }

        // 步骤5：保存资源图片关联（最多5张）
        // 建立资源与图片的多对多关系
        if (dto.getImageIds() != null && !dto.getImageIds().isEmpty()) {
            log.debug("开始保存资源图片关联: resourceId={}, imageCount={}", 
                resource.getId(), dto.getImageIds().size());
            saveResourceImages(resource.getId(), dto.getImageIds(), dto.getCoverImageId());
        }

        // 步骤6：转换为VO对象
        // 包含关联的分类、图片、下载链接等完整信息
        ResourceVO result = convertToVO(resource);
        
        // 记录创建成功
        log.info("创建资源成功: resourceId={}, title={}", resource.getId(), resource.getTitle());
        
        return result;
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

    /**
     * 切换资源状态
     * 
     * 业务逻辑：
     * 1. 查询资源是否存在
     * 2. 切换状态（1<->0）
     * 3. 更新数据库
     * 
     * @param id 资源ID
     * @throws RuntimeException 当资源不存在时抛出
     */
    @Override
    public void toggleStatus(Long id) {
        // 记录操作开始
        log.info("开始切换资源状态: resourceId={}", id);
        
        // 步骤1：查询资源是否存在
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            log.warn("资源不存在: resourceId={}", id);
            throw new RuntimeException("资源不存在");
        }
        
        // 记录当前状态
        int oldStatus = resource.getStatus();
        
        // 步骤2：切换状态
        // 1表示上架，0表示下架
        int newStatus = resource.getStatus() == 1 ? 0 : 1;
        resource.setStatus(newStatus);
        
        // 步骤3：更新数据库
        log.debug("更新资源状态: resourceId={}, oldStatus={}, newStatus={}", id, oldStatus, newStatus);
        resourceMapper.updateById(resource);
        
        // 记录操作成功
        log.info("切换资源状态成功: resourceId={}, oldStatus={}, newStatus={}", id, oldStatus, newStatus);
    }

    /**
     * 记录资源下载次数
     * 
     * 业务逻辑：
     * 1. 构建更新条件
     * 2. 使用SQL自增语句更新下载次数
     * 3. 避免并发问题
     * 
     * @param id 资源ID
     */
    @Override
    public void recordDownload(Long id) {
        // 记录下载计数开始
        log.debug("开始更新资源下载次数: resourceId={}", id);
        
        // 步骤1：构建更新条件
        LambdaUpdateWrapper<Resource> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Resource::getId, id);
        
        // 步骤2：使用SQL自增语句
        // 直接在数据库层面执行 download_count = download_count + 1
        // 这样可以避免并发情况下的计数不准确问题
        wrapper.setSql("download_count = download_count + 1");
        
        // 执行更新
        int rows = resourceMapper.update(null, wrapper);
        
        // 记录更新结果
        log.debug("更新资源下载次数完成: resourceId={}, affectedRows={}", id, rows);
    }

    @Override
    @Transactional
    public ResourceVO createCrawledResource(ResourceDTO dto, Long crawlerTaskId, String sourceUrl, List<Long> imageIds) {
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        
        // 设置爬虫相关字段
        resource.setCrawlerTaskId(crawlerTaskId);
        resource.setSourceUrl(sourceUrl);
        
        // 设置资源状态为"已下架"（0）
        resource.setStatus(0);
        
        // 初始化计数器
        resource.setDownloadCount(0);
        resource.setViewCount(0);
        resource.setSortOrder(0);
        
        // 插入资源
        resourceMapper.insert(resource);

        // 保存下载链接
        if (dto.getDownloadLinks() != null && !dto.getDownloadLinks().isEmpty()) {
            int sortOrder = 0;
            for (DownloadLinkDTO linkDTO : dto.getDownloadLinks()) {
                DownloadLink link = new DownloadLink();
                
                // 手动设置字段而不是使用BeanUtils.copyProperties
                link.setResourceId(resource.getId());
                link.setTitle(linkDTO.getTitle());
                link.setLinkName(linkDTO.getLinkName() != null ? linkDTO.getLinkName() : "下载链接");
                link.setLinkType(linkDTO.getLinkType() != null ? linkDTO.getLinkType() : "direct");
                link.setLinkUrl(linkDTO.getLinkUrl());
                link.setDownloadUrl(linkDTO.getDownloadUrl());
                link.setPassword(linkDTO.getPassword());
                link.setIsValid(1);
                link.setSortOrder(sortOrder++);
                
                log.debug("插入下载链接: linkName={}, linkType={}, linkUrl={}", 
                         link.getLinkName(), link.getLinkType(), link.getLinkUrl());
                
                downloadLinkMapper.insert(link);
            }
        }

        // 保存资源图片关联
        if (imageIds != null && !imageIds.isEmpty()) {
            // 如果没有指定封面图，使用第一张图片作为封面
            Long coverImageId = dto.getCoverImageId() != null ? dto.getCoverImageId() : imageIds.get(0);
            resource.setCoverImageId(coverImageId);
            resourceMapper.updateById(resource);
            
            saveResourceImages(resource.getId(), imageIds, coverImageId);
        }

        return convertToVO(resource);
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
    
    @Override
    @Transactional
    public int batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        log.info("开始批量更新资源状态: ids={}, status={}", ids, status);
        
        try {
            // 验证状态值
            if (status == null || (status != 0 && status != 1)) {
                throw new RuntimeException("无效的状态值，只能是0（已下架）或1（已发布）");
            }
            
            // 批量更新
            LambdaUpdateWrapper<Resource> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(Resource::getId, ids);
            updateWrapper.eq(Resource::getDeleted, 0); // 只更新未删除的资源
            updateWrapper.set(Resource::getStatus, status);
            
            int updatedCount = resourceMapper.update(null, updateWrapper);
            
            log.info("批量更新资源状态完成: 请求数量={}, 实际更新数量={}, status={}", 
                    ids.size(), updatedCount, status);
            
            return updatedCount;
            
        } catch (Exception e) {
            log.error("批量更新资源状态失败: ids={}, status={}, error={}", ids, status, e.getMessage(), e);
            throw new RuntimeException("批量更新资源状态失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        log.info("开始批量删除资源: ids={}", ids);
        
        try {
            int deletedCount = 0;
            
            for (Long id : ids) {
                try {
                    // 调用单个删除方法，确保完整的删除逻辑
                    deleteResource(id);
                    deletedCount++;
                } catch (Exception e) {
                    log.warn("删除资源失败，跳过: resourceId={}, error={}", id, e.getMessage());
                }
            }
            
            log.info("批量删除资源完成: 请求数量={}, 实际删除数量={}", ids.size(), deletedCount);
            
            return deletedCount;
            
        } catch (Exception e) {
            log.error("批量删除资源失败: ids={}, error={}", ids, e.getMessage(), e);
            throw new RuntimeException("批量删除资源失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public int batchMoveToCategory(List<Long> ids, Long categoryId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        log.info("开始批量移动资源到分类: ids={}, categoryId={}", ids, categoryId);
        
        try {
            // 验证分类是否存在
            Category category = categoryMapper.selectById(categoryId);
            if (category == null) {
                throw new RuntimeException("目标分类不存在");
            }
            
            // 批量更新分类
            LambdaUpdateWrapper<Resource> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(Resource::getId, ids);
            updateWrapper.eq(Resource::getDeleted, 0); // 只更新未删除的资源
            updateWrapper.set(Resource::getCategoryId, categoryId);
            
            int updatedCount = resourceMapper.update(null, updateWrapper);
            
            log.info("批量移动资源到分类完成: 请求数量={}, 实际移动数量={}, categoryId={}", 
                    ids.size(), updatedCount, categoryId);
            
            return updatedCount;
            
        } catch (Exception e) {
            log.error("批量移动资源到分类失败: ids={}, categoryId={}, error={}", ids, categoryId, e.getMessage(), e);
            throw new RuntimeException("批量移动资源到分类失败: " + e.getMessage());
        }
    }
}
