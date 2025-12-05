package com.resource.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resource.platform.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccessLogMapper extends BaseMapper<AccessLog> {
}
