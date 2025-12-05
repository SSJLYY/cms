package com.resource.platform.service;

import com.resource.platform.common.PageResult;
import com.resource.platform.dto.FriendLinkDTO;
import com.resource.platform.dto.FriendLinkQueryDTO;
import com.resource.platform.vo.FriendLinkVO;

import java.util.List;

/**
 * 友情链接服务接口
 */
public interface FriendLinkService {
    
    /**
     * 分页查询友情链接
     */
    PageResult<FriendLinkVO> queryPage(FriendLinkQueryDTO queryDTO);
    
    /**
     * 获取所有启用的友情链接
     */
    List<FriendLinkVO> listEnabled();
    
    /**
     * 根据ID获取友情链接
     */
    FriendLinkVO getById(Long id);
    
    /**
     * 创建友情链接
     */
    Long create(FriendLinkDTO dto);
    
    /**
     * 更新友情链接
     */
    void update(FriendLinkDTO dto);
    
    /**
     * 删除友情链接
     */
    void delete(Long id);
    
    /**
     * 批量删除友情链接
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 更新友情链接状态
     */
    void updateStatus(Long id, Integer status);
}
