package com.resource.platform.crawler;

import lombok.Data;

@Data
public class WebsiteStructure {
    
    /**
     * 资源列表页选择器
     */
    private String listPageSelector;
    
    /**
     * 资源详情链接选择器
     */
    private String detailLinkSelector;
    
    /**
     * 标题选择器
     */
    private String titleSelector;
    
    /**
     * 描述选择器
     */
    private String descriptionSelector;
    
    /**
     * 下载链接选择器
     */
    private String downloadLinkSelector;
    
    /**
     * 图片选择器
     */
    private String imageSelector;
    
    /**
     * 分类选择器
     */
    private String categorySelector;
    
    /**
     * 分页选择器
     */
    private String paginationSelector;
    
    /**
     * 是否识别成功
     */
    private boolean identified;
}
