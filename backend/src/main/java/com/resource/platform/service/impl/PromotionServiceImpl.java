package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.dto.AdvertisementDTO;
import com.resource.platform.entity.Advertisement;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.mapper.AdvertisementMapper;
import com.resource.platform.service.PromotionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromotionServiceImpl implements PromotionService {
    
    @Autowired
    private AdvertisementMapper advertisementMapper;
    
    @Override
    public PageResult<Advertisement> getAdvertisementList(Integer page, Integer pageSize, String position) {
        Page<Advertisement> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();
        if (position != null && !position.isEmpty() && !"all".equals(position)) {
            wrapper.eq(Advertisement::getPosition, position);
        }
        wrapper.orderByAsc(Advertisement::getSortOrder);
        wrapper.orderByDesc(Advertisement::getCreateTime);
        
        Page<Advertisement> resultPage = advertisementMapper.selectPage(pageParam, wrapper);
        
        PageResult<Advertisement> pageResult = new PageResult<>(
            resultPage.getTotal(),
            resultPage.getRecords()
        );
        
        return pageResult;
    }
    
    @Override
    public Advertisement getAdvertisementById(Long id) {
        Advertisement advertisement = advertisementMapper.selectById(id);
        if (advertisement == null) {
            throw new ResourceNotFoundException("广告不存在");
        }
        return advertisement;
    }
    
    @Override
    public void createAdvertisement(AdvertisementDTO dto) {
        Advertisement advertisement = new Advertisement();
        BeanUtils.copyProperties(dto, advertisement);
        advertisementMapper.insert(advertisement);
    }
    
    @Override
    public void updateAdvertisement(Long id, AdvertisementDTO dto) {
        Advertisement advertisement = advertisementMapper.selectById(id);
        if (advertisement == null) {
            throw new ResourceNotFoundException("广告不存在");
        }
        
        BeanUtils.copyProperties(dto, advertisement);
        advertisement.setId(id);
        advertisementMapper.updateById(advertisement);
    }
    
    @Override
    public void deleteAdvertisement(Long id) {
        Advertisement advertisement = advertisementMapper.selectById(id);
        if (advertisement == null) {
            throw new ResourceNotFoundException("广告不存在");
        }
        advertisementMapper.deleteById(id);
    }
    
    @Override
    public void updateStatus(Long id, Integer status) {
        Advertisement advertisement = advertisementMapper.selectById(id);
        if (advertisement == null) {
            throw new ResourceNotFoundException("广告不存在");
        }
        
        advertisement.setStatus(status);
        advertisementMapper.updateById(advertisement);
    }
    
    @Override
    public void updateSortOrder(Long id, Integer sortOrder) {
        Advertisement advertisement = advertisementMapper.selectById(id);
        if (advertisement == null) {
            throw new ResourceNotFoundException("广告不存在");
        }
        
        advertisement.setSortOrder(sortOrder);
        advertisementMapper.updateById(advertisement);
    }
    
    @Override
    public void recordClick(Long id) {
        Advertisement advertisement = advertisementMapper.selectById(id);
        if (advertisement == null) {
            throw new ResourceNotFoundException("广告不存在");
        }
        
        advertisement.setClickCount(advertisement.getClickCount() + 1);
        advertisementMapper.updateById(advertisement);
    }
    
    @Override
    public List<Map<String, String>> getPositionOptions() {
        List<Map<String, String>> options = new ArrayList<>();
        
        Map<String, String> option1 = new HashMap<>();
        option1.put("value", "homepage");
        option1.put("label", "首页");
        options.add(option1);
        
        Map<String, String> option2 = new HashMap<>();
        option2.put("value", "download");
        option2.put("label", "下载页");
        options.add(option2);
        
        Map<String, String> option3 = new HashMap<>();
        option3.put("value", "category");
        option3.put("label", "分类页");
        options.add(option3);
        
        Map<String, String> option4 = new HashMap<>();
        option4.put("value", "custom");
        option4.put("label", "自定义页");
        options.add(option4);
        
        return options;
    }
    
    @Override
    public List<Advertisement> getActiveAdvertisements(String position) {
        LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Advertisement::getStatus, 1);
        wrapper.eq(Advertisement::getPosition, position);
        
        // 检查时间范围
        LocalDateTime now = LocalDateTime.now();
        wrapper.and(w -> w.isNull(Advertisement::getStartTime)
                .or()
                .le(Advertisement::getStartTime, now));
        wrapper.and(w -> w.isNull(Advertisement::getEndTime)
                .or()
                .ge(Advertisement::getEndTime, now));
        
        wrapper.orderByAsc(Advertisement::getSortOrder);
        
        return advertisementMapper.selectList(wrapper);
    }
}
