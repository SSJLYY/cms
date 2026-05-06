package com.resource.platform.module.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.resource.platform.module.resource.entity.IpDownloadRecord;
import com.resource.platform.module.resource.entity.IpResourceDownload;
import com.resource.platform.module.resource.mapper.IpDownloadRecordMapper;
import com.resource.platform.module.resource.mapper.IpResourceDownloadMapper;
import com.resource.platform.module.resource.service.IpDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class IpDownloadServiceImpl implements IpDownloadService {

    private static final int MAX_DOWNLOADS_PER_DAY = 2;

    private final IpDownloadRecordMapper ipDownloadRecordMapper;
    private final IpResourceDownloadMapper ipResourceDownloadMapper;

    @Override
    public boolean canDownload(String ipAddress) {
        return getTodayDownloadCount(ipAddress) < MAX_DOWNLOADS_PER_DAY;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DownloadDecision recordDownload(String ipAddress, Long resourceId) {
        LocalDate today = LocalDate.now();

        if (hasDownloadedToday(ipAddress, resourceId)) {
            return DownloadDecision.ALREADY_DOWNLOADED;
        }

        if (getTodayDownloadCount(ipAddress) >= MAX_DOWNLOADS_PER_DAY) {
            return DownloadDecision.LIMIT_REACHED;
        }

        IpResourceDownload resourceDownload = new IpResourceDownload();
        resourceDownload.setIpAddress(ipAddress);
        resourceDownload.setResourceId(resourceId);
        resourceDownload.setDownloadDate(today);

        try {
            ipResourceDownloadMapper.insert(resourceDownload);
        } catch (DuplicateKeyException e) {
            log.info("IP {} already downloaded resource {} today", ipAddress, resourceId);
            return DownloadDecision.ALREADY_DOWNLOADED;
        }

        LambdaQueryWrapper<IpDownloadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IpDownloadRecord::getIpAddress, ipAddress)
                .eq(IpDownloadRecord::getDownloadDate, today);

        IpDownloadRecord record = ipDownloadRecordMapper.selectOne(wrapper);
        if (record == null) {
            record = new IpDownloadRecord();
            record.setIpAddress(ipAddress);
            record.setDownloadDate(today);
            record.setDownloadCount(1);
            try {
                ipDownloadRecordMapper.insert(record);
                log.info("Created first download record for IP {}", ipAddress);
                return DownloadDecision.RECORDED;
            } catch (DuplicateKeyException e) {
                record = ipDownloadRecordMapper.selectOne(wrapper);
            }
        }

        if (record == null) {
            log.warn("Download record missing after duplicate retry, ip={}", ipAddress);
            return DownloadDecision.RECORDED;
        }

        int updated = ipDownloadRecordMapper.incrementDownloadCountIfBelowLimit(ipAddress, today, MAX_DOWNLOADS_PER_DAY);
        if (updated == 0) {
            log.info("Download limit reached for IP {} during concurrent update", ipAddress);
            LambdaUpdateWrapper<IpResourceDownload> deleteWrapper = new LambdaUpdateWrapper<>();
            deleteWrapper.eq(IpResourceDownload::getIpAddress, ipAddress)
                    .eq(IpResourceDownload::getResourceId, resourceId)
                    .eq(IpResourceDownload::getDownloadDate, today);
            ipResourceDownloadMapper.delete(deleteWrapper);
            return DownloadDecision.LIMIT_REACHED;
        }
        log.info("Updated download count for IP {}", ipAddress);
        return DownloadDecision.RECORDED;
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
        return Math.max(0, MAX_DOWNLOADS_PER_DAY - getTodayDownloadCount(ipAddress));
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
