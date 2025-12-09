package com.resource.platform.controller;

import com.resource.platform.common.Result;
import com.resource.platform.crawler.WebsiteStructure;
import com.resource.platform.service.IntelligentParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 爬虫调试控制器
 * 用于测试和调试爬虫功能
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler/debug")
@Tag(name = "爬虫调试", description = "爬虫调试相关接口")
public class CrawlerDebugController {

    @Autowired
    private IntelligentParserService intelligentParserService;

    @Operation(summary = "分析网站结构", description = "分析指定网站的结构，用于调试爬虫配置")
    @PostMapping("/analyze")
    public Result<Map<String, Object>> analyzeWebsite(
            @Parameter(description = "目标网站URL", required = true)
            @RequestParam String url) {
        
        try {
            log.info("开始分析网站结构: {}", url);
            
            // 1. 分析网站结构
            WebsiteStructure structure = intelligentParserService.analyzeWebsite(url);
            
            // 2. 获取页面详细信息
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(30000)
                    .get();
            
            // 3. 提取资源链接
            List<String> resourceLinks = intelligentParserService.extractResourceLinks(doc, structure);
            
            // 4. 分析页面链接分布
            Map<String, Integer> linkPatterns = analyzeLinkPatterns(doc, url);
            
            // 5. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("pageTitle", doc.title());
            result.put("structure", structure);
            result.put("resourceLinks", resourceLinks);
            result.put("resourceLinkCount", resourceLinks.size());
            result.put("linkPatterns", linkPatterns);
            result.put("totalLinks", doc.select("a[href]").size());
            
            // 6. 添加调试信息
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("baseUrl", getBaseUrl(url));
            debugInfo.put("domain", getDomain(url));
            
            // 分析所有链接
            Elements allLinks = doc.select("a[href]");
            List<Map<String, String>> linkDetails = new ArrayList<>();
            String baseUrl = getBaseUrl(url);
            
            for (Element link : allLinks) {
                String href = link.absUrl("href");
                if (href.startsWith(baseUrl) && !href.equals(url) && !href.contains("#")) {
                    Map<String, String> linkInfo = new HashMap<>();
                    linkInfo.put("url", href);
                    linkInfo.put("text", link.text().trim());
                    linkInfo.put("title", link.attr("title"));
                    linkDetails.add(linkInfo);
                    
                    if (linkDetails.size() >= 20) { // 限制返回数量
                        break;
                    }
                }
            }
            debugInfo.put("sampleInternalLinks", linkDetails);
            
            result.put("debugInfo", debugInfo);
            
            log.info("网站结构分析完成: url={}, 识别状态={}, 资源链接数={}", 
                    url, structure.isIdentified(), resourceLinks.size());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("分析网站结构失败: url={}", url, e);
            return Result.error("分析失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "测试CSS选择器", description = "测试指定CSS选择器在目标网站上的匹配结果")
    @PostMapping("/test-selector")
    public Result<Map<String, Object>> testSelector(
            @Parameter(description = "目标网站URL", required = true)
            @RequestParam String url,
            @Parameter(description = "CSS选择器", required = true)
            @RequestParam String selector) {
        
        try {
            log.info("测试CSS选择器: url={}, selector={}", url, selector);
            
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(30000)
                    .get();
            
            Elements elements = doc.select(selector);
            
            List<Map<String, String>> results = new ArrayList<>();
            for (Element element : elements) {
                Map<String, String> info = new HashMap<>();
                info.put("tagName", element.tagName());
                info.put("text", element.text().trim());
                info.put("href", element.absUrl("href"));
                info.put("html", element.outerHtml());
                results.add(info);
                
                if (results.size() >= 10) { // 限制返回数量
                    break;
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("selector", selector);
            result.put("matchCount", elements.size());
            result.put("matches", results);
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("测试CSS选择器失败: url={}, selector={}", url, selector, e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 分析链接模式分布
     */
    private Map<String, Integer> analyzeLinkPatterns(Document doc, String baseUrl) {
        Map<String, Integer> patterns = new HashMap<>();
        Elements links = doc.select("a[href]");
        String domain = getDomain(baseUrl);
        
        for (Element link : links) {
            String href = link.absUrl("href");
            
            if (href.contains(domain)) {
                // 分析内部链接模式
                if (href.matches(".*\\d+\\.html$")) {
                    patterns.merge("数字.html", 1, Integer::sum);
                }
                if (href.contains("/detail/")) {
                    patterns.merge("包含/detail/", 1, Integer::sum);
                }
                if (href.contains("/resource/")) {
                    patterns.merge("包含/resource/", 1, Integer::sum);
                }
                if (href.contains("/download/")) {
                    patterns.merge("包含/download/", 1, Integer::sum);
                }
                if (href.contains("/item/")) {
                    patterns.merge("包含/item/", 1, Integer::sum);
                }
                if (href.contains("/post/")) {
                    patterns.merge("包含/post/", 1, Integer::sum);
                }
                if (href.endsWith(".html")) {
                    patterns.merge("以.html结尾", 1, Integer::sum);
                }
            }
        }
        
        return patterns;
    }
    
    /**
     * 获取基础URL
     */
    private String getBaseUrl(String url) {
        return url.replaceAll("(https?://[^/]+).*", "$1");
    }
    
    /**
     * 获取域名
     */
    private String getDomain(String url) {
        return url.replaceAll("https?://([^/]+).*", "$1");
    }
}