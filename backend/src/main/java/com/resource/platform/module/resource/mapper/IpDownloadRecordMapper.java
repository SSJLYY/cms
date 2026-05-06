package com.resource.platform.module.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resource.platform.module.resource.entity.IpDownloadRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * IP下载记录Mapper
 */
@Mapper
public interface IpDownloadRecordMapper extends BaseMapper<IpDownloadRecord> {

    @Update("UPDATE ip_download_record " +
            "SET download_count = download_count + 1 " +
            "WHERE ip_address = #{ipAddress} " +
            "AND download_date = #{downloadDate} " +
            "AND download_count < #{maxDownloads}")
    int incrementDownloadCountIfBelowLimit(@Param("ipAddress") String ipAddress,
                                           @Param("downloadDate") LocalDate downloadDate,
                                           @Param("maxDownloads") int maxDownloads);
}
