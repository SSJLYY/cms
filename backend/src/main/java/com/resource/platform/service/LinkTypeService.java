package com.resource.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.resource.platform.entity.LinkType;

import java.util.List;

/**
 * 网盘类型配置Service接口
 */
public interface LinkTypeService extends IService<LinkType> {
    
    /**
     * 获取所有启用的网盘类型（按排序）
     */
    List<LinkType> listEnabled();
    
    /**
     * 获取所有网盘类型（包括禁用的）
     */
    List<LinkType> listAll();
}
