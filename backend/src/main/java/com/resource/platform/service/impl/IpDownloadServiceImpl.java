package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.entity.IpDownloadRecord;
import com.resource.platform.entity.IpResourceDownload;
import com.resource.platform.mapper.IpDownloadRecordMapper;
import com.resource.platform.mapper.IpResourceDownloadMapper;
import com.resource.platform.service.IpDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * IP下载限制服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IpDownloadServiceImpl implements IpDownloadService {
    
    private final IpDownloadRecordMapper ipDownloadRecordMapper;
    private final IpResourceDownloadMapper ipResourceDownloadMapper;
    
    /**
     * 每个IP每天最大下载次数
     */
    private static final int MAX_DOWNLOADS_PER_DAY = 2;
    
    @Override
    public boolean canDownload(String ipAddress) {
        int todayCount = getTodayDownloadCount(ipAddress);
        return todayCount < MAX_DOWNLOADS_PER_DAY;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordDownload(String ipAddress, Long resourceId) {
        LocalDate today = LocalDate.now();
        
        // 检查是否已下载过此资源
        if (hasDownloadedToday(ipAddress, resourceId)) {
            log.info("IP {} 今日已下载过资源 {}，不重复计数", ipAddress, resourceId);
            return;
        }
        
        // 记录资源下载详情
        IpResourceDownload resourceDownload = new IpResourceDownload();
        resourceDownload.setIpAddress(ipAddress);
        resourceDownload.setResourceId(resourceId);
        resourceDownload.setDownloadDate(today);
        ipResourceDownloadMapper.insert(resourceDownload);
        
        // 查询今日记录
        LambdaQueryWrapper<IpDownloadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IpDownloadRecord::getIpAddress, ipAddress)
               .eq(IpDownloadRecord::getDownloadDate, today);
        
        IpDownloadRecord record = ipDownloadRecordMapper.selectOne(wrapper);
        
        if (record == null) {
            // 创建新记录
            record = new IpDownloadRecord();
            record.setIpAddress(ipAddress);
            record.setDownloadDate(today);
            record.setDownloadCount(1);
            ipDownloadRecordMapper.insert(record);
            log.info("IP {} 首次下载记录创建", ipAddress);
        } else {
            // 更新下载次数
            record.setDownloadCount(record.getDownloadCount() + 1);
            ipDownloadRecordMapper.updateById(record);
            log.info("IP {} 下载次数更新为 {}", ipAddress, record.getDownloadCount());
        }
    }
    
    @Override
    public boolean hasDownloadedToday(String ipAddress, Long resourceId) {
        LocalDate today = LocalDate.now();
        
        LambdaQueryWrapper<IpResourceDownload> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IpResourceDownload::getIpAddress, ipAddress)
               .eq(IpResourceDownload::getResourceId, resourceId)
               .eq(IpResourceDownload::getDownloadDate, today);
        
        return ipResourceDownloadMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public int getRemainingDownloads(String ipAddress) {
        int todayCount = getTodayDownloadCount(ipAddress);
        int remaining = MAX_DOWNLOADS_PER_DAY - todayCount;
        return Math.max(0, remaining);
    }
    
    @Override
    public int getTodayDownloadCount(String ipAddress) {
        LocalDate today = LocalDate.now();
        
        LambdaQueryWrapper<IpDownloadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IpDownloadRecord::getIpAddress, ipAddress)
               .eq(IpDownloadRecord::getDownloadDate, today);
        
        IpDownloadRecord record = ipDownloadRecordMapper.selectOne(wrapper);
        
        return record == null ? 0 : record.getDownloadCount();
    }
}
