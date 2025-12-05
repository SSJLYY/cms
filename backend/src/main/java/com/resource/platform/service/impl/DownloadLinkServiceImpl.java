package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.dto.DownloadLinkDTO;
import com.resource.platform.entity.DownloadLink;
import com.resource.platform.mapper.DownloadLinkMapper;
import com.resource.platform.service.DownloadLinkService;
import com.resource.platform.vo.DownloadLinkVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DownloadLinkServiceImpl implements DownloadLinkService {

    @Autowired
    private DownloadLinkMapper downloadLinkMapper;

    @Override
    public List<DownloadLinkVO> getDownloadLinks(Long resourceId) {
        LambdaQueryWrapper<DownloadLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLink::getResourceId, resourceId);
        wrapper.orderByAsc(DownloadLink::getSortOrder);
        
        List<DownloadLink> links = downloadLinkMapper.selectList(wrapper);
        return links.stream().map(link -> {
            DownloadLinkVO vo = new DownloadLinkVO();
            BeanUtils.copyProperties(link, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public DownloadLinkVO createDownloadLink(DownloadLinkDTO dto) {
        DownloadLink link = new DownloadLink();
        BeanUtils.copyProperties(dto, link);
        link.setSortOrder(0);
        downloadLinkMapper.insert(link);

        DownloadLinkVO vo = new DownloadLinkVO();
        BeanUtils.copyProperties(link, vo);
        return vo;
    }

    @Override
    public void deleteDownloadLink(Long id) {
        downloadLinkMapper.deleteById(id);
    }
}
