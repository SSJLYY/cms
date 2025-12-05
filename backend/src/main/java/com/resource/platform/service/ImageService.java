package com.resource.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.dto.ImageQueryDTO;
import com.resource.platform.entity.Image;
import com.resource.platform.vo.ImageStatisticsVO;
import com.resource.platform.vo.ImageVO;
import com.resource.platform.vo.ResourceVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 图片服务接口
 */
public interface ImageService {
    
    /**
     * 获取图片统计信息
     */
    ImageStatisticsVO getStatistics();
    
    /**
     * 上传单张图片
     */
    ImageVO uploadImage(MultipartFile file, Long uploaderId);
    
    /**
     * 批量上传图片
     */
    List<ImageVO> uploadImages(List<MultipartFile> files, Long uploaderId);
    
    /**
     * 查询图片列表
     */
    Page<ImageVO> queryImages(ImageQueryDTO queryDTO);
    
    /**
     * 获取图片详情
     */
    ImageVO getImageById(Long id);
    
    /**
     * 删除图片
     */
    void deleteImage(Long id);
    
    /**
     * 批量删除图片
     */
    void deleteImages(List<Long> ids);
    
    /**
     * 检查图片使用情况
     */
    boolean checkImageUsage(Long id);
    
    /**
     * 更新图片使用状态
     * 根据图片的实际关联情况自动更新 is_used 字段
     * 
     * @param imageId 图片ID
     */
    void updateImageUsageStatus(Long imageId);
    
    /**
     * 批量更新图片使用状态
     * 遍历图片ID列表，为每个图片更新使用状态
     * 
     * @param imageIds 图片ID列表
     */
    void batchUpdateImageUsageStatus(List<Long> imageIds);
    
    /**
     * 获取图片的使用详情
     * 返回使用该图片的所有资源列表
     * 
     * @param imageId 图片ID
     * @return 使用该图片的资源列表
     */
    List<ResourceVO> getImageUsageDetails(Long imageId);
}
