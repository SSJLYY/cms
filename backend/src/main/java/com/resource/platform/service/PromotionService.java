package com.resource.platform.service;

import com.resource.platform.common.PageResult;
import com.resource.platform.dto.AdvertisementDTO;
import com.resource.platform.entity.Advertisement;

import java.util.List;
import java.util.Map;

public interface PromotionService {
    /**
     * 获取广告列表（分页）
     */
    PageResult<Advertisement> getAdvertisementList(Integer page, Integer pageSize, String position);
    
    /**
     * 获取广告详情
     */
    Advertisement getAdvertisementById(Long id);
    
    /**
     * 创建广告
     */
    void createAdvertisement(AdvertisementDTO dto);
    
    /**
     * 更新广告
     */
    void updateAdvertisement(Long id, AdvertisementDTO dto);
    
    /**
     * 删除广告
     */
    void deleteAdvertisement(Long id);
    
    /**
     * 更新广告状态
     */
    void updateStatus(Long id, Integer status);
    
    /**
     * 更新广告排序
     */
    void updateSortOrder(Long id, Integer sortOrder);
    
    /**
     * 记录点击
     */
    void recordClick(Long id);
    
    /**
     * 获取所有广告位置选项
     */
    List<Map<String, String>> getPositionOptions();
    
    /**
     * 获取用户端广告（按位置）
     */
    List<Advertisement> getActiveAdvertisements(String position);
}
