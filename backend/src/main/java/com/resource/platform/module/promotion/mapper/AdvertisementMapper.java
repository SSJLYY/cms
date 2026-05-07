package com.resource.platform.module.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resource.platform.module.promotion.entity.Advertisement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdvertisementMapper extends BaseMapper<Advertisement> {
    @Update("UPDATE advertisement " +
            "SET click_count = COALESCE(click_count, 0) + 1 " +
            "WHERE id = #{id} " +
            "AND deleted = 0 " +
            "AND status = 1 " +
            "AND (start_time IS NULL OR start_time <= #{now}) " +
            "AND (end_time IS NULL OR end_time >= #{now})")
    int incrementClickCount(@Param("id") Long id, @Param("now") java.time.LocalDateTime now);
}
