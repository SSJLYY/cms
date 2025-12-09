package com.resource.platform.crawler;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * robots.txt解析器
 * 负责下载、解析和缓存robots.txt文件
 */
@Slf4j
@Component
public class RobotsTxtParser {

    // robots.txt缓存，key为域名
    private final Map<String, RobotsRules> cache = new ConcurrentHashMap<>();
    
    // 缓存过期时间（24小时）
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000;

    /**
     * 检查URL是否允许爬取
     */
    public boolean isAllowed(String urlString) {
        try {
            URL url = new URL(urlString);
            String domain = url.getProtocol() + "://" + url.getHost();
            
            RobotsRules rules = getRobotsRules(domain);
            
            if (rules == null || rules.isAllowAll()) {
                return true;
            }
            
            String path = url.getPath();
            if (path.isEmpty()) {
                path = "/";
            }
            
            return rules.isAllowed(path);
            
        } catch (Exception e) {
            log.error("检查robots.txt失败: url={}", urlString, e);
            // 出错时默认允许
            return true;
        }
    }

    /**
     * 获取爬取延迟（秒）
     */
    public int getCrawlDelay(String urlString) {
        try {
            URL url = new URL(urlString);
            String domain = url.getProtocol() + "://" + url.getHost();
            
            RobotsRules rules = getRobotsRules(domain);
            
            if (rules == null) {
                return 0;
            }
            
            return rules.getCrawlDelay();
            
        } catch (Exception e) {
            log.error("获取crawl-delay失败: url={}", urlString, e);
            return 0;
        }
    }

    /**
     * 获取robots.txt规则（带缓存）
     */
    private RobotsRules getRobotsRules(String domain) {
        RobotsRules cached = cache.get(domain);
        
        // 检查缓存是否有效
        if (cached != null && !cached.isExpired()) {
            return cached;
        }
        
        // 下载并解析robots.txt
        RobotsRules rules = downloadAndParse(domain);
        
        if (rules != null) {
            cache.put(domain, rules);
        }
        
        return rules;
    }

    /**
     * 下载并解析robots.txt
     */
    private RobotsRules downloadAndParse(String domain) {
        String robotsUrl = domain + "/robots.txt";
        
        try {
            log.info("下载robots.txt: {}", robotsUrl);
            
            URL url = new URL(robotsUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; ResourcePlatformBot/1.0)");
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 404) {
                // robots.txt不存在，允许所有
                log.info("robots.txt不存在，允许所有: {}", robotsUrl);
                return RobotsRules.allowAll();
            }
            
            if (responseCode != 200) {
                log.warn("下载robots.txt失败: url={}, code={}", robotsUrl, responseCode);
                return null;
            }
            
            // 读取内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            
            // 解析规则
            return parseRobotsTxt(content.toString());
            
        } catch (Exception e) {
            log.error("下载robots.txt异常: url={}", robotsUrl, e);
            return null;
        }
    }

    /**
     * 解析robots.txt内容
     */
    private RobotsRules parseRobotsTxt(String content) {
        RobotsRules rules = new RobotsRules();
        
        String[] lines = content.split("\n");
        boolean isOurUserAgent = false;
        
        for (String line : lines) {
            line = line.trim();
            
            // 跳过注释和空行
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            // 解析字段
            int colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                continue;
            }
            
            String field = line.substring(0, colonIndex).trim().toLowerCase();
            String value = line.substring(colonIndex + 1).trim();
            
            if (field.equals("user-agent")) {
                // 检查是否匹配我们的User-Agent
                isOurUserAgent = value.equals("*") || 
                                value.toLowerCase().contains("resourceplatformbot");
            } else if (isOurUserAgent) {
                if (field.equals("disallow")) {
                    if (!value.isEmpty()) {
                        rules.addDisallowRule(value);
                    }
                } else if (field.equals("allow")) {
                    if (!value.isEmpty()) {
                        rules.addAllowRule(value);
                    }
                } else if (field.equals("crawl-delay")) {
                    try {
                        int delay = Integer.parseInt(value);
                        rules.setCrawlDelay(delay);
                    } catch (NumberFormatException e) {
                        log.warn("无效的crawl-delay值: {}", value);
                    }
                }
            }
        }
        
        return rules;
    }

    /**
     * robots.txt规则
     */
    @Data
    public static class RobotsRules {
        private List<Pattern> disallowPatterns = new ArrayList<>();
        private List<Pattern> allowPatterns = new ArrayList<>();
        private int crawlDelay = 0;
        private long timestamp = System.currentTimeMillis();
        private boolean allowAll = false;

        public static RobotsRules allowAll() {
            RobotsRules rules = new RobotsRules();
            rules.allowAll = true;
            return rules;
        }

        public void addDisallowRule(String pattern) {
            disallowPatterns.add(convertToPattern(pattern));
        }

        public void addAllowRule(String pattern) {
            allowPatterns.add(convertToPattern(pattern));
        }

        /**
         * 检查路径是否允许
         */
        public boolean isAllowed(String path) {
            if (allowAll) {
                return true;
            }
            
            // 先检查allow规则
            for (Pattern pattern : allowPatterns) {
                if (pattern.matcher(path).matches()) {
                    return true;
                }
            }
            
            // 再检查disallow规则
            for (Pattern pattern : disallowPatterns) {
                if (pattern.matcher(path).matches()) {
                    return false;
                }
            }
            
            // 默认允许
            return true;
        }

        /**
         * 检查缓存是否过期
         */
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }

        /**
         * 将robots.txt模式转换为正则表达式
         */
        private Pattern convertToPattern(String robotsPattern) {
            try {
                // 先转义反斜杠，然后转义其他特殊字符
                String regex = robotsPattern
                        .replace("\\", "\\\\")  // 先转义反斜杠
                        .replace(".", "\\.")
                        .replace("?", "\\?")
                        .replace("+", "\\+")
                        .replace("|", "\\|")
                        .replace("(", "\\(")
                        .replace(")", "\\)")
                        .replace("[", "\\[")
                        .replace("]", "\\]")
                        .replace("{", "\\{")
                        .replace("}", "\\}")
                        .replace("^", "\\^")
                        .replace("$", "\\$");
                
                // 处理通配符（在转义之后）
                regex = regex.replace("*", ".*");
                
                // 如果以/结尾，匹配该路径及其子路径
                if (regex.endsWith("/")) {
                    regex = regex + ".*";
                }
                
                // 添加开始锚点
                if (!regex.startsWith(".*")) {
                    regex = "^" + regex;
                }
                
                return Pattern.compile(regex);
            } catch (Exception e) {
                // 如果正则表达式编译失败，返回一个匹配所有的模式
                log.warn("无法编译robots.txt规则: {}, 使用默认规则", robotsPattern);
                return Pattern.compile(".*");
            }
        }
    }
}
