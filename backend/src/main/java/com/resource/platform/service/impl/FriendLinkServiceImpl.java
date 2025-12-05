package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.dto.FriendLinkDTO;
import com.resource.platform.dto.FriendLinkQueryDTO;
import com.resource.platform.entity.FriendLink;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.mapper.FriendLinkMapper;
import com.resource.platform.service.FriendLinkService;
import com.resource.platform.vo.FriendLinkVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 友情链接服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendLinkServiceImpl implements FriendLinkService {
    
    private final FriendLinkMapper friendLinkMapper;
    
    @Override
    public PageResult<FriendLinkVO> queryPage(FriendLinkQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getName()), FriendLink::getName, queryDTO.getName())
               .eq(queryDTO.getStatus() != null, FriendLink::getStatus, queryDTO.getStatus())
               .orderByAsc(FriendLink::getSortOrder)
               .orderByDesc(FriendLink::getCreateTime);
        
        // 分页查询
        Page<FriendLink> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<FriendLink> result = friendLinkMapper.selectPage(page, wrapper);
        
        // 转换为VO
        List<FriendLinkVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(result.getTotal(), voList);
    }
    
    @Override
    public List<FriendLinkVO> listEnabled() {
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendLink::getStatus, 1)
               .orderByAsc(FriendLink::getSortOrder)
               .orderByDesc(FriendLink::getCreateTime);
        
        List<FriendLink> list = friendLinkMapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public FriendLinkVO getById(Long id) {
        FriendLink friendLink = friendLinkMapper.selectById(id);
        if (friendLink == null) {
            throw new ResourceNotFoundException("友情链接不存在");
        }
        return convertToVO(friendLink);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(FriendLinkDTO dto) {
        // 验证URL格式
        if (!isValidUrl(dto.getUrl())) {
            throw new BusinessException("URL格式不正确");
        }
        
        // 检查名称是否重复
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendLink::getName, dto.getName());
        if (friendLinkMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("网站名称已存在");
        }
        
        FriendLink friendLink = new FriendLink();
        BeanUtils.copyProperties(dto, friendLink);
        
        // 设置默认排序
        if (friendLink.getSortOrder() == null) {
            friendLink.setSortOrder(0);
        }
        
        friendLinkMapper.insert(friendLink);
        log.info("创建友情链接成功，ID: {}, 名称: {}", friendLink.getId(), friendLink.getName());
        return friendLink.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FriendLinkDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("友情链接ID不能为空");
        }
        
        // 验证URL格式
        if (!isValidUrl(dto.getUrl())) {
            throw new BusinessException("URL格式不正确");
        }
        
        FriendLink existingLink = friendLinkMapper.selectById(dto.getId());
        if (existingLink == null) {
            throw new ResourceNotFoundException("友情链接不存在");
        }
        
        // 检查名称是否重复（排除自己）
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendLink::getName, dto.getName())
               .ne(FriendLink::getId, dto.getId());
        if (friendLinkMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("网站名称已存在");
        }
        
        FriendLink friendLink = new FriendLink();
        BeanUtils.copyProperties(dto, friendLink);
        
        friendLinkMapper.updateById(friendLink);
        log.info("更新友情链接成功，ID: {}, 名称: {}", friendLink.getId(), friendLink.getName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FriendLink friendLink = friendLinkMapper.selectById(id);
        if (friendLink == null) {
            throw new ResourceNotFoundException("友情链接不存在");
        }
        
        friendLinkMapper.deleteById(id);
        log.info("删除友情链接成功，ID: {}, 名称: {}", id, friendLink.getName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("删除ID列表不能为空");
        }
        
        friendLinkMapper.deleteBatchIds(ids);
        log.info("批量删除友情链接成功，数量: {}", ids.size());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        FriendLink friendLink = friendLinkMapper.selectById(id);
        if (friendLink == null) {
            throw new ResourceNotFoundException("友情链接不存在");
        }
        
        friendLink.setStatus(status);
        friendLinkMapper.updateById(friendLink);
        log.info("更新友情链接状态成功，ID: {}, 状态: {}", id, status);
    }
    
    /**
     * 转换为VO
     */
    private FriendLinkVO convertToVO(FriendLink friendLink) {
        FriendLinkVO vo = new FriendLinkVO();
        BeanUtils.copyProperties(friendLink, vo);
        return vo;
    }
    
    /**
     * 验证URL格式
     */
    private boolean isValidUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
