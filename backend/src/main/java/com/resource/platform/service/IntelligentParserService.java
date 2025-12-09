package com.resource.platform.service;

import com.resource.platform.crawler.ResourceData;
import com.resource.platform.crawler.WebsiteStructure;
import org.jsoup.nodes.Document;

import java.util.List;

public interface IntelligentParserService {
    
    /**
     * 分析网站结构
     */
    WebsiteStructure analyzeWebsite(String url);
    
    /**
     * 提取资源列表链接
     */
    List<String> extractResourceLinks(Document doc, WebsiteStructure structure);
    
    /**
     * 提取资源详情
     */
    ResourceData extractResourceDetail(Document doc, WebsiteStructure structure);
    
    /**
     * 提取图片URL
     */
    List<String> extractImageUrls(Document doc, WebsiteStructure structure);
    
    /**
     * 提取下载链接
     */
    List<String> extractDownloadLinks(Document doc, WebsiteStructure structure);
}
