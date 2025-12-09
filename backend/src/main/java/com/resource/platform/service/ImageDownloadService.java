package com.resource.platform.service;

import com.resource.platform.vo.ImageVO;

import java.util.List;

/**
 * 图片下载服务接口
 * 用于爬虫模块下载和处理图片
 */
public interface ImageDownloadService {
    
    /**
     * 下载图片并上传到平台
     * @param imageUrl 图片URL
     * @param uploaderId 上传者ID（通常是系统用户）
     * @return 上传后的图片信息
     */
    ImageVO downloadAndUploadImage(String imageUrl, Long uploaderId);
    
    /**
     * 批量下载图片并上传到平台
     * @param imageUrls 图片URL列表
     * @param uploaderId 上传者ID
     * @return 成功上传的图片信息列表
     */
    List<ImageVO> downloadAndUploadImages(List<String> imageUrls, Long uploaderId);
    
    /**
     * 关联图片到资源
     * @param resourceId 资源ID
     * @param imageIds 图片ID列表
     */
    void associateImagesToResource(Long resourceId, List<Long> imageIds);
}
