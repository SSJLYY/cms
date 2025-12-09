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
 * 图片服务实现类
 * 
 * 功能说明：
 * - 实现图片上传的核心业务逻辑
 * - 处理图片文件的验证和存储
 * - 管理图片的缩略图生成
 * - 处理图片的使用状态管理
 * - 提供图片的查询和删除服务
 * - 统计图片的使用情况和存储信息
 * 
 * @author 系统
 * @since 1.0
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

    /**
     * 获取图片统计信息
     * 
     * 业务逻辑：
     * 1. 统计总图片数量（只统计未删除的）
     * 2. 统计已使用和未使用的图片数量
     * 3. 计算总存储大小
     * 4. 统计今日上传数量
     * 5. 返回统计结果
     * 
     * @return 图片统计信息对象
     */
    @Override
    public ImageStatisticsVO getStatistics() {
        // 记录统计开始
        log.info("开始获取图片统计信息");
        
        // 创建统计结果对象
        ImageStatisticsVO statistics = new ImageStatisticsVO();
        
        // 步骤1：统计总图片数（只统计未删除的）
        log.debug("统计总图片数量");
        Long totalImages = imageMapper.selectCount(
            new LambdaQueryWrapper<Image>().eq(Image::getDeleted, 0)
        );
        statistics.setTotalImages(totalImages);
        log.debug("总图片数: {}", totalImages);
        
        // 步骤2：统计已使用图片数（只统计未删除的）
        log.debug("统计已使用图片数量");
        Long usedImages = imageMapper.selectCount(
            new LambdaQueryWrapper<Image>()
                .eq(Image::getIsUsed, 1)
                .eq(Image::getDeleted, 0)
        );
        statistics.setUsedImages(usedImages);
        log.debug("已使用图片数: {}", usedImages);
        
        // 步骤3：计算未使用图片数
        Long unusedImages = totalImages - usedImages;
        statistics.setUnusedImages(unusedImages);
        log.debug("未使用图片数: {}", unusedImages);
        
        // 步骤4：计算总存储大小（只统计未删除的）
        log.debug("计算总存储大小");
        List<Image> allImages = imageMapper.selectList(
            new LambdaQueryWrapper<Image>().eq(Image::getDeleted, 0)
        );
        
        // 使用流式计算累加文件大小
        long totalSize = allImages.stream()
            .mapToLong(Image::getFileSize)
            .sum();
        statistics.setTotalSize(totalSize);
        log.debug("总存储大小: {} bytes", totalSize);
        
        // 步骤5：统计今日上传数（只统计未删除的）
        log.debug("统计今日上传数量");
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        Long todayUploads = imageMapper.selectCount(
            new LambdaQueryWrapper<Image>()
                .between(Image::getCreateTime, todayStart, todayEnd)
                .eq(Image::getDeleted, 0)
        );
        statistics.setTodayUploads(todayUploads);
        log.debug("今日上传数: {}", todayUploads);
        
        // 记录统计成功
        log.info("获取图片统计信息成功: total={}, used={}, unused={}, totalSize={} bytes, todayUploads={}", 
            totalImages, usedImages, unusedImages, totalSize, todayUploads);
        
        return statistics;
    }

    /**
     * 上传单张图片
     * 
     * 业务逻辑：
     * 1. 验证文件格式、大小等
     * 2. 获取图片尺寸信息
     * 3. 上传原图到存储服务
     * 4. 生成并上传缩略图
     * 5. 保存图片信息到数据库
     * 6. 返回图片VO对象
     * 
     * @param file 上传的图片文件
     * @param uploaderId 上传者ID
     * @return 上传成功的图片VO对象
     * @throws FileUploadException 当文件验证失败或上传失败时抛出
     */
    @Override
    public ImageVO uploadImage(MultipartFile file, Long uploaderId) {
        // 记录上传开始
        log.info("开始上传图片: filename={}, size={} bytes, contentType={}, uploaderId={}", 
            file.getOriginalFilename(), file.getSize(), file.getContentType(), uploaderId);
        
        // 步骤1：验证文件
        // 检查文件格式、大小、内容等是否符合要求
        log.debug("验证图片文件: filename={}", file.getOriginalFilename());
        String validationError = FileValidationUtil.validateImageFile(file);
        if (validationError != null) {
            log.warn("文件验证失败: filename={}, error={}", file.getOriginalFilename(), validationError);
            throw new FileUploadException(validationError);
        }
        
        // 初始化URL变量
        String fileUrl = null;
        String thumbnailUrl = null;
        
        try {
            // 步骤2：获取图片尺寸
            // 读取图片的宽度和高度信息
            log.debug("获取图片尺寸: filename={}", file.getOriginalFilename());
            int[] dimensions = ImageUtil.getImageDimensions(file);
            log.debug("图片尺寸: {}x{}", dimensions[0], dimensions[1]);
            
            // 步骤3：上传原图
            // 将原始图片文件上传到存储服务
            log.debug("上传原图到存储服务: filename={}", file.getOriginalFilename());
            fileUrl = storageService.upload(file, "images");
            log.debug("原图上传成功: fileUrl={}", fileUrl);
            
            // 步骤4：生成缩略图
            // 按照配置的尺寸生成缩略图
            log.debug("生成缩略图: targetSize={}x{}", thumbnailWidth, thumbnailHeight);
            
            // 检查是否支持缩略图生成
            if (ImageUtil.isThumbnailSupported(file.getOriginalFilename())) {
                try {
                    byte[] thumbnailBytes = ImageUtil.generateThumbnail(
                        file.getBytes(), thumbnailWidth, thumbnailHeight, true);
                    log.debug("缩略图生成成功: size={} bytes", thumbnailBytes.length);
                    
                    // 步骤5：上传缩略图
                    // 将生成的缩略图上传到存储服务
                    String thumbnailFileName = "thumbnail_" + ImageUtil.generateFileName(file.getOriginalFilename());
                    log.debug("上传缩略图: filename={}", thumbnailFileName);
                    thumbnailUrl = storageService.upload(
                        new java.io.ByteArrayInputStream(thumbnailBytes),
                        thumbnailFileName,
                        "thumbnails"
                    );
                    log.debug("缩略图上传成功: thumbnailUrl={}", thumbnailUrl);
                } catch (Exception e) {
                    log.warn("缩略图生成失败，跳过缩略图: filename={}, error={}", 
                        file.getOriginalFilename(), e.getMessage());
                    thumbnailUrl = null; // 设置为null，表示没有缩略图
                }
            } else {
                log.info("不支持的图片格式，跳过缩略图生成: filename={}", file.getOriginalFilename());
                thumbnailUrl = null; // 设置为null，表示没有缩略图
            }
            
            // 步骤6：保存图片信息到数据库
            // 创建图片实体对象并设置各个字段
            log.debug("保存图片信息到数据库");
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
            image.setIsUsed(0); // 初始状态为未使用
            image.setUploaderId(uploaderId);
            image.setDeleted(0); // 显式设置为未删除
            
            // 执行数据库插入
            log.debug("插入图片记录到数据库: filename={}", image.getFileName());
            int insertResult = imageMapper.insert(image);
            
            // 验证插入结果
            if (insertResult > 0) {
                // 记录上传成功
                log.info("图片上传成功: imageId={}, filename={}, fileUrl={}, thumbnailUrl={}", 
                    image.getId(), image.getFileName(), fileUrl, thumbnailUrl);
                
                return convertToVO(image);
            } else {
                log.error("数据库插入失败: filename={}, insertResult={}", file.getOriginalFilename(), insertResult);
                throw new RuntimeException("数据库插入失败");
            }
            
        } catch (Exception e) {
            // 记录上传失败
            log.error("上传图片失败: filename={}, fileUrl={}, thumbnailUrl={}, error={}", 
                file.getOriginalFilename(), fileUrl, thumbnailUrl, e.getMessage(), e);
            throw new FileUploadException("上传图片失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     * 
     * 业务逻辑：
     * 1. 遍历文件列表
     * 2. 逐个调用单张上传方法
     * 3. 收集上传成功的图片
     * 4. 记录上传失败的文件（但不中断处理）
     * 5. 返回成功上传的图片列表
     * 
     * @param files 图片文件列表
     * @param uploaderId 上传者ID
     * @return 成功上传的图片VO列表
     */
    @Override
    @Transactional
    public List<ImageVO> uploadImages(List<MultipartFile> files, Long uploaderId) {
        // 记录批量上传开始
        log.info("开始批量上传图片: fileCount={}, uploaderId={}", files.size(), uploaderId);
        
        // 创建结果列表
        List<ImageVO> result = new ArrayList<>();
        
        // 统计上传结果
        int successCount = 0;
        int failCount = 0;
        
        // 步骤1：遍历文件列表
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            
            try {
                // 步骤2：调用单张上传方法
                log.debug("处理第{}个文件: filename={}", i + 1, file.getOriginalFilename());
                ImageVO imageVO = uploadImage(file, uploaderId);
                
                // 步骤3：收集成功的图片
                result.add(imageVO);
                successCount++;
                
                log.debug("第{}个文件上传成功: imageId={}", i + 1, imageVO.getId());
                
            } catch (Exception e) {
                // 步骤4：记录失败但继续处理
                failCount++;
                log.error("批量上传第{}个图片失败: filename={}, error={}", 
                    i + 1, file.getOriginalFilename(), e.getMessage(), e);
                // 不抛出异常，继续处理下一个文件
            }
        }
        
        // 记录批量上传结果
        log.info("批量上传图片完成: total={}, success={}, fail={}", 
            files.size(), successCount, failCount);
        
        return result;
    }

    /**
     * 分页查询图片列表
     * 
     * 业务逻辑：
     * 1. 构建查询条件（只查询未删除的图片）
     * 2. 根据关键词搜索文件名
     * 3. 根据文件类型、存储类型、使用状态筛选
     * 4. 根据上传者和时间范围筛选
     * 5. 按创建时间倒序排列
     * 6. 执行分页查询并转换为VO
     * 
     * @param queryDTO 查询条件对象
     * @return 图片分页结果
     */
    @Override
    public Page<ImageVO> queryImages(ImageQueryDTO queryDTO) {
        // 记录查询开始
        log.info("开始分页查询图片: page={}, pageSize={}, keyword={}, fileType={}, isUsed={}, uploaderId={}", 
            queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getKeyword(), 
            queryDTO.getFileType(), queryDTO.getIsUsed(), queryDTO.getUploaderId());
        
        // 创建分页对象
        Page<Image> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        
        // 基础条件：只查询未删除的记录
        wrapper.eq(Image::getDeleted, 0);
        
        // 步骤1：关键词搜索
        // 在文件名或原始文件名中模糊匹配
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            log.debug("添加关键词搜索条件: keyword={}", queryDTO.getKeyword());
            wrapper.and(w -> w
                .like(Image::getFileName, queryDTO.getKeyword())
                .or()
                .like(Image::getOriginalName, queryDTO.getKeyword())
            );
        }
        
        // 步骤2：文件类型筛选
        // 根据MIME类型精确匹配
        if (queryDTO.getFileType() != null && !queryDTO.getFileType().isEmpty()) {
            log.debug("添加文件类型筛选条件: fileType={}", queryDTO.getFileType());
            wrapper.eq(Image::getFileType, queryDTO.getFileType());
        }
        
        // 步骤3：存储类型筛选
        // 根据存储方式筛选（local、oss等）
        if (queryDTO.getStorageType() != null && !queryDTO.getStorageType().isEmpty()) {
            log.debug("添加存储类型筛选条件: storageType={}", queryDTO.getStorageType());
            wrapper.eq(Image::getStorageType, queryDTO.getStorageType());
        }
        
        // 步骤4：使用状态筛选
        // 根据是否被资源使用筛选
        if (queryDTO.getIsUsed() != null) {
            log.debug("添加使用状态筛选条件: isUsed={}", queryDTO.getIsUsed());
            wrapper.eq(Image::getIsUsed, queryDTO.getIsUsed());
        }
        
        // 步骤5：上传者筛选
        // 根据上传者ID筛选
        if (queryDTO.getUploaderId() != null) {
            log.debug("添加上传者筛选条件: uploaderId={}", queryDTO.getUploaderId());
            wrapper.eq(Image::getUploaderId, queryDTO.getUploaderId());
        }
        
        // 步骤6：时间范围筛选
        // 根据创建时间范围筛选
        if (queryDTO.getStartTime() != null) {
            log.debug("添加开始时间筛选条件: startTime={}", queryDTO.getStartTime());
            wrapper.ge(Image::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            log.debug("添加结束时间筛选条件: endTime={}", queryDTO.getEndTime());
            wrapper.le(Image::getCreateTime, queryDTO.getEndTime());
        }
        
        // 步骤7：排序设置
        // 按创建时间倒序，最新的图片排在前面
        wrapper.orderByDesc(Image::getCreateTime);
        
        // 步骤8：执行分页查询
        log.debug("执行分页查询: page={}, pageSize={}", queryDTO.getPage(), queryDTO.getPageSize());
        Page<Image> imagePage = imageMapper.selectPage(page, wrapper);
        
        // 步骤9：转换为VO对象
        // 创建VO分页对象并复制分页信息
        Page<ImageVO> voPage = new Page<>();
        BeanUtils.copyProperties(imagePage, voPage, "records");
        
        // 转换记录列表
        List<ImageVO> voRecords = imagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voRecords);
        
        // 记录查询成功
        log.info("分页查询图片成功: total={}, records={}", voPage.getTotal(), voRecords.size());
        
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
