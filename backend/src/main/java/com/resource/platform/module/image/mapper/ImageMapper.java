package com.resource.platform.module.image.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resource.platform.module.image.entity.Image;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ImageMapper extends BaseMapper<Image> {

    /**
     * 统计未删除图片的总文件大小（SQL聚合，避免全表扫描后流式计算）
     *
     * @return 总文件大小（bytes），无图片时返回 0
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM resource_image WHERE deleted = 0")
    long selectTotalFileSize();
}

