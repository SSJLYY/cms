package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.resource.platform.entity.LinkType;
import com.resource.platform.mapper.LinkTypeMapper;
import com.resource.platform.service.LinkTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 网盘类型配置Service实现类
 */
@Service
public class LinkTypeServiceImpl extends ServiceImpl<LinkTypeMapper, LinkType> implements LinkTypeService {
    
    @Override
    public List<LinkType> listEnabled() {
        return list(new LambdaQueryWrapper<LinkType>()
                .eq(LinkType::getStatus, 1)
                .orderByAsc(LinkType::getSortOrder));
    }
    
    @Override
    public List<LinkType> listAll() {
        return list(new LambdaQueryWrapper<LinkType>()
                .orderByAsc(LinkType::getSortOrder));
    }
}
