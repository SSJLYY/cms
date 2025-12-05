package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.dto.ImageQueryDTO;
import com.resource.platform.entity.Image;
import com.resource.platform.entity.Resource;
import com.resource.platform.entity.ResourceImage;
import com.resource.platform.exception.FileUploadException;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.exception.ValidationException;
import com.resource.platform.mapper.ImageMapper;
import com.resource.platform.service.ImageService;
import com.resource.platform.service.StorageService;
import com.resource.platform.util.FileValidationUtil;
import com.resource.platform.util.ImageUtil;
import com.resource.platform.vo.ImageStatisticsVO;
import com.resource.platform.vo.ImageVO;
import com.resource.platform.vo.ResourceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片服务实现
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private StorageService storageService;
    
    @Autowired
    private com.resource.platform.mapper.ResourceImageMapper resourceImageMapper;
    
    @Autowired
    private com.resource.platform.mapper.ResourceMapper resourceMapper;

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;

    @Value("${file.image.thumbnail.width:300}")
    private int thumbnailWidth;

    @Value("${file.image.thumbnail.height:300}")
    private int thumbnailHeight;

    @Value("${storage.type:local}")
    private String storageType;

    @Override
    public ImageStatisticsVO getStatistics() {
        ImageStatisticsVO statistics = new ImageStatisticsVO();
        
        // 总图片数（只统计未删除的）
        Long totalImages = imageMapper.selectCount(
            new LambdaQueryWrapper<Image>().eq(Image::getDeleted, 0)
        );
        statistics.setTotalImages(totalImages);
        
        // 已使用图片数（只统计未删除的）
        Long usedImages = imageMapper.selectCount(
            new LambdaQueryWrapper<Image>()
                .eq(Image::getIsUsed, 1)
                .eq(Image::getDeleted, 0)
        );
        statistics.setUsedImages(usedImages);
        
        // 未使用图片数
        statistics.setUnusedImages(totalImages - usedImages);
        
        // 总存储大小（只统计未删除的）
        List<Image> allImages = imageMapper.selectList(
            new LambdaQueryWrapper<Image>().eq(Image::getDeleted, 0)
        );
        long totalSize = allImages.stream()
            .mapToLong(Image::getFileSize)
            .sum();
        statistics.setTotalSize(totalSize);
        
        // 今日上传数（只统计未删除的）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayUploads = imageMapper.selectCount(
            new LambdaQueryWrapper<Image>()
                .between(Image::getCreateTime, todayStart, todayEnd)
                .eq(Image::getDeleted, 0)
        );
        statistics.setTodayUploads(todayUploads);
        
        return statistics;
    }

    @Override
    public ImageVO uploadImage(MultipartFile file, Long uploaderId) {
        log.info("开始上传图片: {}", file.getOriginalFilename());
        
        // 验证文件
        String validationError = FileValidationUtil.validateImageFile(file);
        if (validationError != null) {
            log.error("文件验证失败: {}", validationError);
            throw new FileUploadException(validationError);
        }
        
        String fileUrl = null;
        String thumbnailUrl = null;
        
        try {
            // 获取图片尺寸
            log.info("获取图片尺寸...");
            int[] dimensions = ImageUtil.getImageDimensions(file);
            log.info("图片尺寸: {}x{}", dimensions[0], dimensions[1]);
            
            // 上传原图
            log.info("上传原图到存储服务...");
            fileUrl = storageService.upload(file, "images");
            log.info("原图上传成功: {}", fileUrl);
            
            // 生成缩略图
            log.info("生成缩略图...");
            byte[] thumbnailBytes = ImageUtil.generateThumbnail(
                file.getBytes(), thumbnailWidth, thumbnailHeight, true);
            log.info("缩略图生成成功，大小: {} bytes", thumbnailBytes.length);
            
            // 上传缩略图
            log.info("上传缩略图...");
            thumbnailUrl = storageService.upload(
                new java.io.ByteArrayInputStream(thumbnailBytes),
                "thumbnail_" + ImageUtil.generateFileName(file.getOriginalFilename()),
                "thumbnails"
            );
            log.info("缩略图上传成功: {}", thumbnailUrl);
            
            // 保存图片信息到数据库
            log.info("保存图片信息到数据库...");
            Image image = new Image();
            image.setFileName(ImageUtil.generateFileName(file.getOriginalFilename()));
            image.setOriginalName(file.getOriginalFilename());
            image.setFilePath(fileUrl);
            image.setFileUrl(fileUrl);
            image.setFileSize(file.getSize());
            image.setFileType(file.getContentType());
            image.setWidth(dimensions[0]);
            image.setHeight(dimensions[1]);
            image.setThumbnailUrl(thumbnailUrl);
            image.setStorageType(storageType);
            image.setIsUsed(0);
            image.setUploaderId(uploaderId);
            image.setDeleted(0); // 显式设置deleted为0
            
            log.info("准备插入数据库，图片信息: {}", image);
            int insertResult = imageMapper.insert(image);
            log.info("数据库插入结果: {}, 图片ID: {}", insertResult, image.getId());
            
            if (insertResult > 0) {
                log.info("图片上传成功，ID: {}", image.getId());
                return convertToVO(image);
            } else {
                log.error("数据库插入失败，返回值: {}", insertResult);
                throw new RuntimeException("数据库插入失败");
            }
            
        } catch (Exception e) {
            log.error("上传图片失败: {}", file.getOriginalFilename(), e);
            log.error("错误详情: {}", e.getMessage());
            log.error("错误堆栈: ", e);
            throw new FileUploadException("上传图片失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<ImageVO> uploadImages(List<MultipartFile> files, Long uploaderId) {
        List<ImageVO> result = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                ImageVO imageVO = uploadImage(file, uploaderId);
                result.add(imageVO);
            } catch (Exception e) {
                log.error("批量上传图片失败: {}", file.getOriginalFilename(), e);
            }
        }
        return result;
    }

    @Override
    public Page<ImageVO> queryImages(ImageQueryDTO queryDTO) {
        Page<Image> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的记录
        wrapper.eq(Image::getDeleted, 0);
        
        // 关键词搜索
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w
                .like(Image::getFileName, queryDTO.getKeyword())
                .or()
                .like(Image::getOriginalName, queryDTO.getKeyword())
            );
        }
        
        // 文件类型筛选
        if (queryDTO.getFileType() != null && !queryDTO.getFileType().isEmpty()) {
            wrapper.eq(Image::getFileType, queryDTO.getFileType());
        }
        
        // 存储类型筛选
        if (queryDTO.getStorageType() != null && !queryDTO.getStorageType().isEmpty()) {
            wrapper.eq(Image::getStorageType, queryDTO.getStorageType());
        }
        
        // 使用状态筛选
        if (queryDTO.getIsUsed() != null) {
            wrapper.eq(Image::getIsUsed, queryDTO.getIsUsed());
        }
        
        // 上传者筛选
        if (queryDTO.getUploaderId() != null) {
            wrapper.eq(Image::getUploaderId, queryDTO.getUploaderId());
        }
        
        // 时间范围筛选
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(Image::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(Image::getCreateTime, queryDTO.getEndTime());
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Image::getCreateTime);
        
        Page<Image> imagePage = imageMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<ImageVO> voPage = new Page<>();
        BeanUtils.copyProperties(imagePage, voPage, "records");
        voPage.setRecords(
            imagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList())
        );
        
        return voPage;
    }

    @Override
    public ImageVO getImageById(Long id) {
        Image image = imageMapper.selectById(id);
        if (image == null) {
            throw new ResourceNotFoundException("图片", id);
        }
        return convertToVO(image);
    }

    @Override
    @Transactional
    public void deleteImage(Long id) {
        Image image = imageMapper.selectById(id);
        if (image == null) {
            throw new ResourceNotFoundException("图片", id);
        }
        
        // 检查是否被使用
        if (image.getIsUsed() == 1) {
            // 获取使用该图片的资源列表
            List<ResourceVO> usingResources = getImageUsageDetails(id);
            if (!usingResources.isEmpty()) {
                String resourceNames = usingResources.stream()
                    .map(ResourceVO::getTitle)
                    .collect(Collectors.joining(", "));
                throw new ValidationException(
                    String.format("图片正在被以下资源使用，无法删除: %s", resourceNames)
                );
            } else {
                // 如果状态为已使用但实际没有关联，更新状态后再尝试删除
                log.warn("图片 {} 状态为已使用但无实际关联，更新状态", id);
                updateImageUsageStatus(id);
                image = imageMapper.selectById(id);
            }
        }
        
        // 删除存储的文件
        try {
            storageService.delete(image.getFileUrl());
            if (image.getThumbnailUrl() != null) {
                storageService.delete(image.getThumbnailUrl());
            }
        } catch (Exception e) {
            log.error("删除存储文件失败", e);
        }
        
        // 删除数据库记录
        imageMapper.deleteById(id);
        log.info("图片删除成功, imageId: {}", id);
    }

    @Override
    @Transactional
    public void deleteImages(List<Long> ids) {
        for (Long id : ids) {
            try {
                deleteImage(id);
            } catch (Exception e) {
                log.error("批量删除图片失败: {}", id, e);
            }
        }
    }

    @Override
    public boolean checkImageUsage(Long id) {
        Image image = imageMapper.selectById(id);
        if (image == null) {
            throw new ResourceNotFoundException("图片", id);
        }
        return image.getIsUsed() == 1;
    }

    private ImageVO convertToVO(Image image) {
        ImageVO vo = new ImageVO();
        BeanUtils.copyProperties(image, vo);
        return vo;
    }
    
    @Override
    @Transactional
    public void updateImageUsageStatus(Long imageId) {
        log.info("开始更新图片使用状态, imageId: {}", imageId);
        
        try {
            // 1. 查询该图片的所有关联关系
            LambdaQueryWrapper<ResourceImage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ResourceImage::getImageId, imageId);
            Long count = resourceImageMapper.selectCount(wrapper);
            
            log.debug("图片 {} 的关联数量: {}", imageId, count);
            
            // 2. 根据关联数量更新图片状态
            Image image = imageMapper.selectById(imageId);
            if (image != null) {
                Integer oldStatus = image.getIsUsed();
                Integer newStatus = (count > 0) ? 1 : 0;
                
                if (!newStatus.equals(oldStatus)) {
                    image.setIsUsed(newStatus);
                    imageMapper.updateById(image);
                    log.info("图片 {} 状态已更新: {} -> {}", imageId, oldStatus, newStatus);
                } else {
                    log.debug("图片 {} 状态无需更新，当前状态: {}", imageId, oldStatus);
                }
            } else {
                log.warn("图片不存在, imageId: {}", imageId);
            }
        } catch (Exception e) {
            log.error("更新图片使用状态失败, imageId: {}", imageId, e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void batchUpdateImageUsageStatus(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            log.debug("图片ID列表为空，跳过批量更新");
            return;
        }
        
        log.info("开始批量更新图片使用状态，数量: {}", imageIds.size());
        for (Long imageId : imageIds) {
            try {
                updateImageUsageStatus(imageId);
            } catch (Exception e) {
                log.error("批量更新图片状态失败, imageId: {}", imageId, e);
                // 继续处理其他图片
            }
        }
        log.info("批量更新图片使用状态完成");
    }
    
    @Override
    public List<ResourceVO> getImageUsageDetails(Long imageId) {
        log.info("查询图片使用详情, imageId: {}", imageId);
        
        // 1. 查询使用该图片的所有资源
        LambdaQueryWrapper<ResourceImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceImage::getImageId, imageId);
        List<ResourceImage> resourceImages = resourceImageMapper.selectList(wrapper);
        
        if (resourceImages.isEmpty()) {
            log.debug("图片 {} 未被任何资源使用", imageId);
            return Collections.emptyList();
        }
        
        // 2. 提取资源ID列表
        List<Long> resourceIds = resourceImages.stream()
            .map(ResourceImage::getResourceId)
            .collect(Collectors.toList());
        
        log.debug("图片 {} 被 {} 个资源使用", imageId, resourceIds.size());
        
        // 3. 批量查询资源信息
        List<Resource> resources = resourceMapper.selectBatchIds(resourceIds);
        
        // 4. 转换为简化的 ResourceVO
        return resources.stream()
            .map(this::convertToSimpleResourceVO)
            .collect(Collectors.toList());
    }
    
    /**
     * 转换为简化的 ResourceVO（仅包含基本信息）
     */
    private ResourceVO convertToSimpleResourceVO(Resource resource) {
        ResourceVO vo = new ResourceVO();
        vo.setId(resource.getId());
        vo.setTitle(resource.getTitle());
        vo.setDescription(resource.getDescription());
        vo.setStatus(resource.getStatus());
        vo.setCreateTime(resource.getCreateTime());
        return vo;
    }
}
