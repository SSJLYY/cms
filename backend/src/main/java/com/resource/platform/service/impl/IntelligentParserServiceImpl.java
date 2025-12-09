package com.resource.platform.service.impl;

import com.resource.platform.crawler.ResourceData;
import com.resource.platform.crawler.WebsiteStructure;
import com.resource.platform.service.IntelligentParserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IntelligentParserServiceImpl implements IntelligentParserService {
    
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int TIMEOUT = 30000;
    
    // 常见资源链接模式 - 按优先级排序
    private static final String[] RESOURCE_LINK_PATTERNS = {
        // 特定网站模式
        "a[href*='myqqjd.com/'][href*='.html']", // myqqjd.com的html链接
        "a[href*='/detail/']",
        "a[href*='/resource/']",
        "a[href*='/download/']",
        "a[href*='/item/']",
        "a[href*='/post/']",
        
        // 内容区域的链接
        ".content a[href$='.html']", // 内容中的html链接
        ".post-content a[href$='.html']", // 文章内容中的html链接
        ".entry-content a[href$='.html']", // 条目内容中的html链接
        "article a[href$='.html']", // 文章中的html链接
        ".main a[href$='.html']", // 主要内容区域的html链接
        
        // 标题链接
        "h1 a[href$='.html']",
        "h2 a[href$='.html']",
        "h3 a[href$='.html']",
        "h4 a[href$='.html']",
        "h5 a[href$='.html']",
        ".title a[href$='.html']",
        ".post-title a[href$='.html']",
        ".entry-title a[href$='.html']",
        ".resource-title a[href$='.html']",
        
        // 列表项链接
        "li a[href$='.html']",
        ".list a[href$='.html']",
        ".item a[href$='.html']",
        ".resource-item a[href$='.html']",
        
        // 通用模式
        "a[href$='.html']",  // 以.html结尾的链接
        "a[title][href$='.html']", // 有title属性的html链接
        "a[href*='.html']" // 包含.html的链接
    };
    
    // 常见标题选择器
    private static final String[] TITLE_SELECTORS = {
        "h1",
        ".title",
        ".post-title",
        ".resource-title",
        ".article-title",
        "meta[property=og:title]"
    };
    
    // 常见描述选择器
    private static final String[] DESCRIPTION_SELECTORS = {
        ".description",
        ".content",
        ".post-content",
        ".article-content",
        ".resource-description",
        "meta[name=description]",
        "meta[property=og:description]"
    };
    
    // 常见下载链接选择器
    private static final String[] DOWNLOAD_LINK_SELECTORS = {
        "a[href*='pan.baidu.com']",
        "a[href*='aliyundrive.com']",
        "a[href*='lanzou']",
        "a[href*='quark.cn']",
        "a[href*='xunlei.com']",
        "a.download-link",
        ".download-btn a",
        "a[href*='download']"
    };
    
    // 常见图片选择器
    private static final String[] IMAGE_SELECTORS = {
        ".resource-image img",
        ".post-image img",
        ".content img",
        ".gallery img",
        "article img"
    };
    
    // 常见分页选择器
    private static final String[] PAGINATION_SELECTORS = {
        ".pagination a",
        ".pager a",
        "a[rel=next]",
        ".next-page"
    };
    
    @Override
    public WebsiteStructure analyzeWebsite(String url) {
        WebsiteStructure structure = new WebsiteStructure();
        
        try {
            log.info("开始分析网站结构: {}", url);
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();
            
            log.info("页面标题: {}", doc.title());
            log.info("页面链接总数: {}", doc.select("a[href]").size());
            
            // 尝试识别资源链接模式
            String baseUrl = url.replaceAll("(https?://[^/]+).*", "$1");
            String domain = url.replaceAll("https?://([^/]+).*", "$1");
            
            log.info("分析网站: baseUrl={}, domain={}", baseUrl, domain);
            
            for (String pattern : RESOURCE_LINK_PATTERNS) {
                Elements links = doc.select(pattern);
                log.debug("测试选择器: {}, 原始匹配数: {}", pattern, links.size());
                
                // 过滤出有效的站内链接
                long internalLinks = links.stream()
                    .filter(link -> {
                        String href = link.absUrl("href");
                        boolean isValid = href.startsWith(baseUrl) && 
                                         !href.equals(url) && 
                                         !href.contains("#") &&
                                         !href.contains("javascript:") &&
                                         !href.contains("mailto:") &&
                                         href.length() > baseUrl.length() + 5; // 确保不是根路径
                        
                        if (isValid) {
                            log.debug("有效链接: {} -> {}", link.text().trim(), href);
                        }
                        return isValid;
                    })
                    .count();
                
                log.info("选择器 {} 匹配到 {} 个有效站内链接", pattern, internalLinks);
                
                if (internalLinks >= 1) { // 降低阈值，只要找到1个就认为有效
                    structure.setDetailLinkSelector(pattern);
                    log.info("选择资源链接选择器: {}, 匹配数量: {}", pattern, internalLinks);
                    break;
                }
            }
            
            // 如果仍然没有找到，使用更宽松的策略
            if (!StringUtils.hasText(structure.getDetailLinkSelector())) {
                log.info("使用宽松策略分析所有链接");
                Elements allLinks = doc.select("a[href]");
                List<String> candidateLinks = new ArrayList<>();
                
                for (Element link : allLinks) {
                    String href = link.absUrl("href");
                    String linkText = link.text().trim();
                    
                    // 更宽松的过滤条件
                    if (href.startsWith(baseUrl) && 
                        !href.equals(url) && 
                        !href.contains("#") &&
                        !href.contains("javascript:") &&
                        !href.contains("mailto:") &&
                        href.length() > baseUrl.length() + 3) {
                        
                        // 优先选择看起来像资源页面的链接
                        if (href.matches(".*\\d+\\.html$") || // 数字.html
                            href.contains("/detail/") ||
                            href.contains("/resource/") ||
                            href.contains("/download/") ||
                            href.contains("/item/") ||
                            href.contains("/post/") ||
                            (linkText.length() > 5 && !linkText.matches("^(首页|关于|联系|帮助|更多).*"))) {
                            candidateLinks.add(href);
                            log.debug("候选链接: {} -> {}", linkText, href);
                        }
                    }
                }
                
                if (!candidateLinks.isEmpty()) {
                    structure.setDetailLinkSelector("a[href]");
                    log.info("使用通用链接选择器，找到 {} 个候选链接", candidateLinks.size());
                } else {
                    log.warn("未找到任何有效的资源链接");
                }
            }
            
            // 尝试识别标题选择器
            for (String selector : TITLE_SELECTORS) {
                Elements elements = doc.select(selector);
                if (!elements.isEmpty()) {
                    structure.setTitleSelector(selector);
                    break;
                }
            }
            
            // 尝试识别描述选择器
            for (String selector : DESCRIPTION_SELECTORS) {
                Elements elements = doc.select(selector);
                if (!elements.isEmpty()) {
                    structure.setDescriptionSelector(selector);
                    break;
                }
            }
            
            // 尝试识别下载链接选择器
            for (String selector : DOWNLOAD_LINK_SELECTORS) {
                Elements elements = doc.select(selector);
                if (!elements.isEmpty()) {
                    structure.setDownloadLinkSelector(selector);
                    break;
                }
            }
            
            // 尝试识别图片选择器
            for (String selector : IMAGE_SELECTORS) {
                Elements elements = doc.select(selector);
                if (!elements.isEmpty()) {
                    structure.setImageSelector(selector);
                    break;
                }
            }
            
            // 尝试识别分页选择器
            for (String selector : PAGINATION_SELECTORS) {
                Elements elements = doc.select(selector);
                if (!elements.isEmpty()) {
                    structure.setPaginationSelector(selector);
                    break;
                }
            }
            
            // 判断是否识别成功
            structure.setIdentified(
                StringUtils.hasText(structure.getDetailLinkSelector()) &&
                StringUtils.hasText(structure.getTitleSelector())
            );
            
            log.info("网站结构分析完成: {}, 识别状态: {}", url, structure.isIdentified());
            
        } catch (IOException e) {
            log.error("网站结构分析失败: {}", url, e);
            structure.setIdentified(false);
        }
        
        return structure;
    }
    
    @Override
    public List<String> extractResourceLinks(Document doc, WebsiteStructure structure) {
        List<String> links = new ArrayList<>();
        
        String selector = structure.getDetailLinkSelector();
        if (!StringUtils.hasText(selector)) {
            log.warn("没有有效的链接选择器");
            return links;
        }
        
        // 获取基础URL
        String baseUrl = doc.baseUri();
        if (baseUrl.contains("/")) {
            baseUrl = baseUrl.replaceAll("(https?://[^/]+).*", "$1");
        }
        
        log.info("使用选择器提取链接: {}", selector);
        Elements elements = doc.select(selector);
        log.info("选择器匹配到 {} 个元素", elements.size());
        
        for (Element element : elements) {
            String href = element.absUrl("href");
            String linkText = element.text().trim();
            
            if (StringUtils.hasText(href) && 
                href.startsWith(baseUrl) && 
                !href.equals(doc.baseUri()) &&
                !href.contains("#") &&
                !href.contains("javascript:") &&
                !href.contains("mailto:") &&
                !links.contains(href)) {
                
                boolean shouldAdd = false;
                
                // 对于通用选择器，进一步过滤
                if ("a[href]".equals(selector)) {
                    // 更宽松的过滤条件
                    if (href.matches(".*\\d+\\.html$") || // 数字.html
                        href.contains("/detail/") || 
                        href.contains("/resource/") ||
                        href.contains("/download/") ||
                        href.contains("/item/") ||
                        href.contains("/post/") ||
                        href.endsWith(".html")) { // 任何html页面
                        
                        // 排除明显的导航链接
                        if (!linkText.matches("^(首页|关于|联系|帮助|更多|上一页|下一页|返回|back|home|about|contact).*")) {
                            shouldAdd = true;
                        }
                    }
                } else {
                    // 对于特定选择器，直接添加
                    shouldAdd = true;
                }
                
                if (shouldAdd) {
                    links.add(href);
                    log.debug("添加资源链接: {} -> {}", linkText, href);
                }
            }
        }
        
        log.info("提取到 {} 个资源链接", links.size());
        
        // 如果提取的链接很少，尝试更宽松的策略
        if (links.size() < 3) {
            log.info("链接数量较少，尝试更宽松的提取策略");
            List<String> additionalLinks = extractLinksWithRelaxedRules(doc, baseUrl);
            for (String link : additionalLinks) {
                if (!links.contains(link)) {
                    links.add(link);
                    log.debug("添加额外链接: {}", link);
                }
            }
            log.info("宽松策略后共提取到 {} 个资源链接", links.size());
        }
        
        return links;
    }
    
    /**
     * 使用更宽松的规则提取链接
     */
    private List<String> extractLinksWithRelaxedRules(Document doc, String baseUrl) {
        List<String> links = new ArrayList<>();
        Elements allLinks = doc.select("a[href]");
        
        for (Element link : allLinks) {
            String href = link.absUrl("href");
            String linkText = link.text().trim();
            
            if (StringUtils.hasText(href) && 
                href.startsWith(baseUrl) && 
                !href.equals(doc.baseUri()) &&
                !href.contains("#") &&
                !href.contains("javascript:") &&
                !href.contains("mailto:") &&
                href.length() > baseUrl.length() + 3) {
                
                // 包含数字的html页面很可能是资源页面
                if (href.matches(".*\\d+.*\\.html$") ||
                    // 或者链接文本看起来像资源标题（长度大于5且不是导航文字）
                    (linkText.length() > 5 && 
                     !linkText.matches("^(首页|关于|联系|帮助|更多|上一页|下一页|返回|back|home|about|contact|登录|注册|搜索).*") &&
                     href.endsWith(".html"))) {
                    
                    links.add(href);
                    if (links.size() >= 20) { // 限制数量避免过多
                        break;
                    }
                }
            }
        }
        
        return links;
    }
    
    @Override
    public ResourceData extractResourceDetail(Document doc, WebsiteStructure structure) {
        ResourceData data = new ResourceData();
        
        // 提取标题
        String title = extractTitle(doc, structure);
        data.setTitle(title);
        
        // 提取描述
        String description = extractDescription(doc, structure);
        data.setDescription(description);
        
        // 提取下载链接
        List<String> downloadLinks = extractDownloadLinks(doc, structure);
        data.setDownloadLinks(downloadLinks);
        
        // 提取图片
        List<String> imageUrls = extractImageUrls(doc, structure);
        data.setImageUrls(imageUrls);
        
        // 设置来源URL
        data.setSourceUrl(doc.location());
        
        log.info("资源详情提取完成: 标题={}, 下载链接数={}, 图片数={}", 
                title, downloadLinks.size(), imageUrls.size());
        
        return data;
    }
    
    @Override
    public List<String> extractImageUrls(Document doc, WebsiteStructure structure) {
        List<String> imageUrls = new ArrayList<>();
        
        String selector = structure.getImageSelector();
        if (!StringUtils.hasText(selector)) {
            // 使用默认选择器
            selector = "img";
        }
        
        Elements images = doc.select(selector);
        for (Element img : images) {
            String src = img.absUrl("src");
            if (StringUtils.hasText(src) && isValidImageUrl(src)) {
                imageUrls.add(src);
            }
        }
        
        return imageUrls;
    }
    
    @Override
    public List<String> extractDownloadLinks(Document doc, WebsiteStructure structure) {
        List<String> downloadLinks = new ArrayList<>();
        
        String selector = structure.getDownloadLinkSelector();
        if (!StringUtils.hasText(selector)) {
            // 使用所有下载链接选择器
            for (String s : DOWNLOAD_LINK_SELECTORS) {
                Elements links = doc.select(s);
                for (Element link : links) {
                    String href = link.absUrl("href");
                    if (StringUtils.hasText(href) && !downloadLinks.contains(href)) {
                        downloadLinks.add(href);
                    }
                }
            }
        } else {
            Elements links = doc.select(selector);
            for (Element link : links) {
                String href = link.absUrl("href");
                if (StringUtils.hasText(href) && !downloadLinks.contains(href)) {
                    downloadLinks.add(href);
                }
            }
        }
        
        return downloadLinks;
    }
    
    /**
     * 提取标题
     */
    private String extractTitle(Document doc, WebsiteStructure structure) {
        String selector = structure.getTitleSelector();
        
        if (StringUtils.hasText(selector)) {
            Element element = doc.selectFirst(selector);
            if (element != null) {
                // 如果是meta标签，获取content属性
                if (element.tagName().equals("meta")) {
                    String content = element.attr("content");
                    if (StringUtils.hasText(content)) {
                        return cleanTitle(content);
                    }
                } else {
                    String text = element.text();
                    if (StringUtils.hasText(text)) {
                        return cleanTitle(text);
                    }
                }
            }
        }
        
        // 回退到默认选择器
        for (String s : TITLE_SELECTORS) {
            Element element = doc.selectFirst(s);
            if (element != null) {
                if (element.tagName().equals("meta")) {
                    String content = element.attr("content");
                    if (StringUtils.hasText(content)) {
                        return cleanTitle(content);
                    }
                } else {
                    String text = element.text();
                    if (StringUtils.hasText(text) && text.length() > 3) {
                        return cleanTitle(text);
                    }
                }
            }
        }
        
        // 尝试从页面标题中提取
        String pageTitle = doc.title();
        if (StringUtils.hasText(pageTitle)) {
            // 移除网站名称后缀
            String cleanedTitle = pageTitle.replaceAll("\\s*[-_|]\\s*[^-_|]*$", "").trim();
            if (StringUtils.hasText(cleanedTitle) && cleanedTitle.length() > 3) {
                return cleanTitle(cleanedTitle);
            }
            return cleanTitle(pageTitle);
        }
        
        return "未知标题";
    }
    
    /**
     * 清理标题文本
     */
    private String cleanTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return title;
        }
        
        // 移除多余的空白字符
        title = title.replaceAll("\\s+", " ").trim();
        
        // 移除常见的网站后缀
        title = title.replaceAll("\\s*[-_|]\\s*(下载|资源|分享|网站|官网|首页).*$", "");
        
        // 限制长度
        if (title.length() > 200) {
            title = title.substring(0, 200) + "...";
        }
        
        return title;
    }
    
    /**
     * 提取描述
     */
    private String extractDescription(Document doc, WebsiteStructure structure) {
        String selector = structure.getDescriptionSelector();
        
        if (StringUtils.hasText(selector)) {
            Element element = doc.selectFirst(selector);
            if (element != null) {
                if (element.tagName().equals("meta")) {
                    String content = element.attr("content");
                    if (StringUtils.hasText(content)) {
                        return cleanDescription(content);
                    }
                } else {
                    String text = element.text();
                    if (StringUtils.hasText(text)) {
                        return cleanDescription(text);
                    }
                }
            }
        }
        
        // 回退到默认选择器
        for (String s : DESCRIPTION_SELECTORS) {
            Element element = doc.selectFirst(s);
            if (element != null) {
                if (element.tagName().equals("meta")) {
                    String content = element.attr("content");
                    if (StringUtils.hasText(content) && content.length() > 10) {
                        return cleanDescription(content);
                    }
                } else {
                    String text = element.text();
                    if (StringUtils.hasText(text) && text.length() > 20) {
                        return cleanDescription(text);
                    }
                }
            }
        }
        
        // 尝试提取页面中的段落文本作为描述
        Elements paragraphs = doc.select("p");
        for (Element p : paragraphs) {
            String text = p.text();
            if (StringUtils.hasText(text) && text.length() > 50 && text.length() < 1000) {
                return cleanDescription(text);
            }
        }
        
        return "";
    }
    
    /**
     * 清理描述文本
     */
    private String cleanDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return description;
        }
        
        // 移除多余的空白字符
        description = description.replaceAll("\\s+", " ").trim();
        
        // 限制长度
        if (description.length() > 1000) {
            description = description.substring(0, 1000) + "...";
        }
        
        return description;
    }
    
    /**
     * 验证图片URL是否有效
     */
    private boolean isValidImageUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".jpg") || 
               lowerUrl.endsWith(".jpeg") || 
               lowerUrl.endsWith(".png") || 
               lowerUrl.endsWith(".gif") || 
               lowerUrl.endsWith(".webp") ||
               lowerUrl.contains(".jpg?") ||
               lowerUrl.contains(".png?");
    }
}
