package com.resource.platform.crawler;

import lombok.Data;
import java.util.List;

@Data
public class ResourceData {
    
    /**
     * 资源标题
     */
    private String title;
    
    /**
     * 资源描述
     */
    private String description;
    
    /**
     * 资源分类
     */
    private String category;
    
    /**
     * 下载链接列表
     */
    private List<String> downloadLinks;
    
    /**
     * 图片URL列表
     */
    private List<String> imageUrls;
    
    /**
     * 来源URL
     */
    private String sourceUrl;
}
