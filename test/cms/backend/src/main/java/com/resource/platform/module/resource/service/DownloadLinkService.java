package com.resource.platform.module.resource.service;

import com.resource.platform.module.resource.dto.DownloadLinkDTO;
import com.resource.platform.module.resource.vo.DownloadLinkVO;
import java.util.List;

public interface DownloadLinkService {
    /**
     * 获取资源的下载链接
     */
    List<DownloadLinkVO> getDownloadLinks(Long resourceId);
    
    /**
     * 添加下载链接
     */
    DownloadLinkVO createDownloadLink(DownloadLinkDTO dto);
    
    /**
     * 删除下载链接
     */
    void deleteDownloadLink(Long id);
}
