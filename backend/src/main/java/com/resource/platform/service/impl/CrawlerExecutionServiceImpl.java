package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.crawler.ResourceData;
import com.resource.platform.crawler.WebsiteStructure;
import com.resource.platform.crawler.RobotsTxtParser;
import com.resource.platform.dto.ResourceDTO;
import com.resource.platform.entity.CrawlerLog;
import com.resource.platform.entity.CrawlerTask;
import com.resource.platform.entity.Resource;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.mapper.CrawlerLogMapper;
import com.resource.platform.mapper.CrawlerTaskMapper;
import com.resource.platform.mapper.ResourceMapper;
import com.resource.platform.service.CrawlerExecutionService;
import com.resource.platform.service.IntelligentParserService;
import com.resource.platform.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import java.nio.charset.StandardCharsets;
import com.resource.platform.dto.DownloadLinkDTO;
import com.resource.platform.vo.ImageVO;

/**
 * 爬虫执行服务实现类
 * 
 * 功能说明：
 * - 实现爬虫任务的核心执行逻辑
 * - 管理爬虫任务的并发执行
 * - 处理网站结构分析和资源提取
 * - 管理爬虫任务的启动和停止
 * - 处理爬虫执行过程中的错误和异常
 * - 记录详细的爬虫执行日志
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class CrawlerExecutionServiceImpl implements CrawlerExecutionService {

    @Autowired
    private CrawlerTaskMapper crawlerTaskMapper;

    @Autowired
    private CrawlerLogMapper crawlerLogMapper;

    @Autowired
    private com.resource.platform.mapper.CategoryMapper categoryMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private IntelligentParserService intelligentParserService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private RobotsTxtParser robotsTxtParser;

    @Autowired
    private com.resource.platform.service.ImageDownloadService imageDownloadService;

    @Autowired
    private com.resource.platform.crawler.CrawlerErrorHandler errorHandler;

    // 线程池管理并发执行，最多5个任务同时执行
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    // 记录正在执行的任务ID集合
    private final Set<Long> runningTasks = ConcurrentHashMap.newKeySet();

    // 任务停止标记，用于优雅停止任务
    private final Map<Long, Boolean> stopFlags = new ConcurrentHashMap<>();

    /**
     * 执行爬虫任务
     * 
     * 业务逻辑：
     * 1. 检查任务是否正在执行
     * 2. 提交任务到线程池异步执行
     * 3. 记录任务执行状态
     * 
     * @param taskId 任务ID
     * @param executeType 执行类型（1=手动触发，2=定时触发）
     * @throws BusinessException 当任务正在执行时抛出
     */
    @Override
    public void executeCrawlerTask(Long taskId, Integer executeType) {
        // 记录执行请求开始
        log.info("开始执行爬虫任务: taskId={}, executeType={}", taskId, executeType);
        
        // 步骤1：检查任务是否正在执行
        if (isTaskRunning(taskId)) {
            log.warn("任务正在执行中，拒绝重复触发: taskId={}", taskId);
            throw new BusinessException("任务正在执行中，请勿重复触发");
        }

        // 步骤2：提交任务到线程池异步执行
        // 使用线程池避免阻塞主线程
        executorService.submit(() -> {
            try {
                // 调用核心执行逻辑
                executeTask(taskId, executeType);
            } catch (Exception e) {
                // 记录执行异常
                log.error("爬虫任务执行异常: taskId={}, executeType={}, error={}", 
                    taskId, executeType, e.getMessage(), e);
            }
        });
        
        // 记录任务提交成功
        log.info("爬虫任务已提交到执行队列: taskId={}, executeType={}", taskId, executeType);
    }

    /**
     * 检查任务是否正在执行
     * 
     * @param taskId 任务ID
     * @return true表示正在执行，false表示未执行
     */
    @Override
    public Boolean isTaskRunning(Long taskId) {
        boolean running = runningTasks.contains(taskId);
        log.debug("检查任务执行状态: taskId={}, running={}", taskId, running);
        return running;
    }

    /**
     * 停止任务执行
     * 
     * 业务逻辑：
     * 1. 检查任务是否正在执行
     * 2. 设置停止标记
     * 3. 等待任务自然停止
     * 
     * @param taskId 任务ID
     * @throws BusinessException 当任务未在执行时抛出
     */
    @Override
    public void stopTask(Long taskId) {
        // 记录停止请求开始
        log.info("开始停止爬虫任务: taskId={}", taskId);
        
        // 步骤1：检查任务是否正在执行
        if (!isTaskRunning(taskId)) {
            log.warn("任务未在执行中，无法停止: taskId={}", taskId);
            throw new BusinessException("任务未在执行中");
        }
        
        // 步骤2：设置停止标记
        // 通过标记位实现优雅停止，避免强制中断
        stopFlags.put(taskId, true);
        
        // 记录停止信号发送成功
        log.info("已发送停止信号: taskId={}", taskId);
    }

    /**
     * 执行爬虫任务的核心逻辑
     */
    private void executeTask(Long taskId, Integer executeType) {
        // 标记任务开始执行
        runningTasks.add(taskId);
        stopFlags.put(taskId, false);

        LocalDateTime startTime = LocalDateTime.now();
        CrawlerLog crawlerLog = new CrawlerLog();
        crawlerLog.setTaskId(taskId);
        crawlerLog.setExecuteType(executeType);
        crawlerLog.setStatus(1); // 执行中
        crawlerLog.setStartTime(startTime);
        crawlerLog.setCrawledCount(0);
        crawlerLog.setSuccessCount(0);
        crawlerLog.setFailedCount(0);

        try {
            // 获取任务配置
            CrawlerTask task = crawlerTaskMapper.selectById(taskId);
            if (task == null) {
                throw new BusinessException("任务不存在");
            }

            crawlerLog.setTaskName(task.getName());
            crawlerLogMapper.insert(crawlerLog);

            log.info("开始执行爬虫任务: taskId={}, taskName={}, executeType={}", 
                    taskId, task.getName(), executeType);

            // 步骤1: 分析网站结构
            log.info("步骤1: 分析网站结构 - {}", task.getTargetUrl());
            WebsiteStructure structure = intelligentParserService.analyzeWebsite(task.getTargetUrl());
            
            // 步骤2: 初始化爬取队列和去重过滤器
            log.info("步骤2: 初始化爬取队列");
            BloomFilter<String> visitedUrls = BloomFilter.create(
                    Funnels.stringFunnel(StandardCharsets.UTF_8), 
                    10000, 
                    0.01
            );
            
            // 使用队列存储URL及其深度
            Queue<UrlDepthPair> urlQueue = new LinkedList<>();
            urlQueue.offer(new UrlDepthPair(task.getTargetUrl(), 0));
            visitedUrls.put(task.getTargetUrl());
            
            int crawledCount = 0;
            int successCount = 0;
            int failedCount = 0;

            // 步骤3: 爬取资源列表页和详情页
            log.info("步骤3: 开始爬取流程，最大深度={}", task.getMaxDepth());
            while (!urlQueue.isEmpty()) {
                // 检查停止标记
                if (stopFlags.get(taskId)) {
                    log.info("任务被手动停止: taskId={}", taskId);
                    crawlerLog.setStatus(3); // 失败
                    crawlerLog.setErrorMessage("任务被手动停止");
                    break;
                }

                UrlDepthPair current = urlQueue.poll();
                String url = current.url;
                int depth = current.depth;
                
                // 深度限制控制
                if (depth >= task.getMaxDepth()) {
                    log.debug("达到最大深度限制，跳过: url={}, depth={}", url, depth);
                    continue;
                }

                try {
                    // 检查robots.txt
                    if (!robotsTxtParser.isAllowed(url)) {
                        log.info("robots.txt禁止访问: {}", url);
                        continue;
                    }
                    
                    // 获取crawl-delay
                    int crawlDelay = robotsTxtParser.getCrawlDelay(url);
                    if (crawlDelay > 0) {
                        log.debug("遵守crawl-delay: {}秒", crawlDelay);
                        Thread.sleep(crawlDelay * 1000L);
                    }
                    
                    log.debug("爬取列表页: url={}, depth={}", url, depth);
                    
                    // 下载页面
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(30000)
                            .get();

                    // 步骤3.1: 提取资源链接
                    List<String> resourceLinks = intelligentParserService.extractResourceLinks(doc, structure);
                    log.info("从 {} 提取到 {} 个资源链接", url, resourceLinks.size());
                    
                    // 步骤3.2: 爬取每个资源详情页
                    for (String resourceLink : resourceLinks) {
                        if (stopFlags.get(taskId)) {
                            break;
                        }

                        // 使用布隆过滤器去重
                        if (visitedUrls.mightContain(resourceLink)) {
                            log.debug("URL可能已访问，跳过: {}", resourceLink);
                            continue;
                        }
                        visitedUrls.put(resourceLink);

                        try {
                            // 检查robots.txt
                            if (!robotsTxtParser.isAllowed(resourceLink)) {
                                log.info("robots.txt禁止访问资源详情: {}", resourceLink);
                                continue;
                            }
                            
                            log.debug("爬取资源详情: {}", resourceLink);
                            
                            // 爬取资源详情
                            Document detailDoc = Jsoup.connect(resourceLink)
                                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                                    .timeout(30000)
                                    .get();

                            // 步骤3.3: 提取资源详情
                            ResourceData resourceData = intelligentParserService.extractResourceDetail(detailDoc, structure);
                            resourceData.setSourceUrl(resourceLink);

                            crawledCount++;

                            // 步骤3.4: 检查是否重复
                            if (isDuplicateResource(resourceLink)) {
                                log.info("跳过重复资源: url={}", resourceLink);
                                continue;
                            }

                            // 步骤3.5: 创建资源
                            createCrawledResource(task, resourceData);
                            successCount++;
                            log.info("成功创建资源: title={}, url={}", resourceData.getTitle(), resourceLink);

                        } catch (Exception e) {
                            log.error("爬取资源详情失败: url={}", resourceLink, e);
                            failedCount++;
                        }
                        
                        // 限速：每个请求间隔1秒
                        Thread.sleep(1000);
                    }

                    // 步骤3.6: 提取分页链接（只在当前深度未达到最大深度时）
                    if (depth + 1 < task.getMaxDepth()) {
                        List<String> paginationLinks = extractPaginationLinks(doc, structure);
                        log.debug("从 {} 提取到 {} 个分页链接", url, paginationLinks.size());
                        
                        for (String link : paginationLinks) {
                            if (!visitedUrls.mightContain(link)) {
                                urlQueue.offer(new UrlDepthPair(link, depth + 1));
                                visitedUrls.put(link);
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("爬取页面失败: url={}, depth={}", url, depth, e);
                }

                // 限速：每个页面间隔1秒
                Thread.sleep(1000);
            }

            // 更新任务统计
            task.setTotalCrawled(task.getTotalCrawled() + crawledCount);
            task.setTotalSuccess(task.getTotalSuccess() + successCount);
            task.setTotalFailed(task.getTotalFailed() + failedCount);
            task.setLastExecuteTime(startTime);
            
            // 计算下次执行时间
            if (task.getStatus() == 1 && task.getCrawlInterval() != null && task.getCrawlInterval() > 0) {
                task.setNextExecuteTime(LocalDateTime.now().plusHours(task.getCrawlInterval()));
            }
            
            crawlerTaskMapper.updateById(task);

            // 更新日志
            crawlerLog.setStatus(2); // 成功
            crawlerLog.setCrawledCount(crawledCount);
            crawlerLog.setSuccessCount(successCount);
            crawlerLog.setFailedCount(failedCount);

            log.info("爬虫任务执行完成: taskId={}, crawled={}, success={}, failed={}", 
                    taskId, crawledCount, successCount, failedCount);

        } catch (Exception e) {
            log.error("爬虫任务执行异常: taskId={}", taskId, e);
            crawlerLog.setStatus(3); // 失败
            crawlerLog.setErrorMessage(errorHandler.formatErrorMessage(e));
            crawlerLog.setErrorType(errorHandler.getErrorTypeString(e));
            errorHandler.logError(errorHandler.categorizeError(e), e, "taskId=" + taskId);
        } finally {
            // 计算执行时长
            LocalDateTime endTime = LocalDateTime.now();
            crawlerLog.setEndTime(endTime);
            long duration = Duration.between(startTime, endTime).getSeconds();
            crawlerLog.setDuration((int) duration);

            // 更新日志
            crawlerLogMapper.updateById(crawlerLog);

            // 清理标记
            runningTasks.remove(taskId);
            stopFlags.remove(taskId);
        }
    }

    /**
     * 检查资源是否重复
     */
    private boolean isDuplicateResource(String sourceUrl) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getSourceUrl, sourceUrl);
        return resourceMapper.selectCount(wrapper) > 0;
    }

    /**
     * 创建爬取的资源
     */
    private void createCrawledResource(CrawlerTask task, ResourceData resourceData) {
        try {
            // 1. 下载并上传图片
            List<ImageVO> uploadedImages = new ArrayList<>();
            if (resourceData.getImageUrls() != null && !resourceData.getImageUrls().isEmpty()) {
                uploadedImages = imageDownloadService.downloadAndUploadImages(
                        resourceData.getImageUrls(), 
                        1L // 系统用户ID
                );
            }
            
            // 2. 处理分类映射
            Long categoryId = mapCategory(task, resourceData.getCategory());
            
            // 3. 构建ResourceDTO
            ResourceDTO dto = new ResourceDTO();
            dto.setTitle(resourceData.getTitle());
            dto.setDescription(resourceData.getDescription());
            dto.setCategoryId(categoryId);
            
            // 4. 处理下载链接
            if (resourceData.getDownloadLinks() != null && !resourceData.getDownloadLinks().isEmpty()) {
                List<DownloadLinkDTO> downloadLinkDTOs = new ArrayList<>();
                for (String downloadUrl : resourceData.getDownloadLinks()) {
                    DownloadLinkDTO linkDTO = new DownloadLinkDTO();
                    linkDTO.setLinkUrl(downloadUrl);
                    
                    // 设置链接名称和类型，根据URL类型判断
                    String linkName = determineLinkName(downloadUrl);
                    String linkType = determineLinkType(downloadUrl);
                    
                    linkDTO.setLinkName(linkName);
                    linkDTO.setLinkType(linkType);
                    
                    log.debug("创建下载链接DTO: linkName={}, linkType={}, linkUrl={}", 
                             linkName, linkType, downloadUrl);
                    
                    downloadLinkDTOs.add(linkDTO);
                }
                dto.setDownloadLinks(downloadLinkDTOs);
            }
            
            // 5. 提取图片ID列表
            List<Long> imageIds = uploadedImages.stream()
                    .map(ImageVO::getId)
                    .collect(Collectors.toList());
            
            // 6. 创建爬取的资源
            resourceService.createCrawledResource(
                    dto, 
                    task.getId(), 
                    resourceData.getSourceUrl(), 
                    imageIds
            );
            
        } catch (Exception e) {
            log.error("创建爬取资源失败: title={}", resourceData.getTitle(), e);
            throw e;
        }
    }
    
    /**
     * 映射分类
     */
    private Long mapCategory(CrawlerTask task, String sourceCategory) {
        // 如果没有源分类，返回默认分类
        if (sourceCategory == null || sourceCategory.isEmpty()) {
            log.debug("源分类为空，使用默认分类");
            return getDefaultCategoryId();
        }
        
        // 解析分类映射配置
        if (task.getCategoryMapping() != null && !task.getCategoryMapping().isEmpty()) {
            try {
                // 使用Jackson解析JSON映射
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Long> mappingMap = objectMapper.readValue(
                        task.getCategoryMapping(), 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Long>>() {}
                );
                
                // 查找匹配的分类映射
                Long categoryId = mappingMap.get(sourceCategory);
                if (categoryId != null) {
                    log.debug("找到分类映射: {} -> {}", sourceCategory, categoryId);
                    return categoryId;
                }
                
                log.debug("未找到分类映射: {}, 使用默认分类", sourceCategory);
                
            } catch (Exception e) {
                log.error("解析分类映射失败: mapping={}", task.getCategoryMapping(), e);
            }
        }
        
        // 返回默认分类
        return getDefaultCategoryId();
    }
    
    /**
     * 获取默认分类ID
     */
    private Long getDefaultCategoryId() {
        // 查询第一个可用的分类作为默认分类
        LambdaQueryWrapper<com.resource.platform.entity.Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(com.resource.platform.entity.Category::getId);
        wrapper.last("LIMIT 1");
        com.resource.platform.entity.Category category = categoryMapper.selectOne(wrapper);
        return category != null ? category.getId() : 1L;
    }
    
    /**
     * 根据下载链接URL确定链接名称
     */
    private String determineLinkName(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            return "下载链接";
        }
        
        String lowerUrl = downloadUrl.toLowerCase();
        
        // 根据URL特征判断网盘类型
        if (lowerUrl.contains("pan.baidu.com")) {
            return "百度网盘";
        } else if (lowerUrl.contains("aliyundrive.com") || lowerUrl.contains("alipan.com")) {
            return "阿里云盘";
        } else if (lowerUrl.contains("pan.quark.cn") || lowerUrl.contains("quark.cn")) {
            return "夸克网盘";
        } else if (lowerUrl.contains("lanzou") || lowerUrl.contains("lanzoui") || lowerUrl.contains("lanzoux")) {
            return "蓝奏云";
        } else if (lowerUrl.contains("xunlei.com") || lowerUrl.contains("thunder")) {
            return "迅雷下载";
        } else if (lowerUrl.contains("115.com")) {
            return "115网盘";
        } else if (lowerUrl.contains("weiyun.com")) {
            return "微云";
        } else if (lowerUrl.contains("onedrive") || lowerUrl.contains("sharepoint")) {
            return "OneDrive";
        } else if (lowerUrl.contains("dropbox.com")) {
            return "Dropbox";
        } else if (lowerUrl.contains("github.com") || lowerUrl.contains("gitee.com")) {
            return "代码仓库";
        } else if (lowerUrl.contains("mega.nz")) {
            return "MEGA网盘";
        } else if (lowerUrl.endsWith(".zip") || lowerUrl.endsWith(".rar") || 
                   lowerUrl.endsWith(".7z") || lowerUrl.endsWith(".tar.gz")) {
            return "直链下载";
        } else {
            return "下载链接";
        }
    }
    
    /**
     * 根据下载链接URL确定链接类型（对应数据库中的link_type字段）
     */
    private String determineLinkType(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            return "direct";
        }
        
        String lowerUrl = downloadUrl.toLowerCase();
        
        // 根据URL特征判断网盘类型，返回对应的type_code
        if (lowerUrl.contains("pan.baidu.com")) {
            return "baidu";
        } else if (lowerUrl.contains("aliyundrive.com") || lowerUrl.contains("alipan.com")) {
            return "aliyun";
        } else if (lowerUrl.contains("pan.quark.cn") || lowerUrl.contains("quark.cn")) {
            return "quark";
        } else if (lowerUrl.contains("lanzou") || lowerUrl.contains("lanzoui") || lowerUrl.contains("lanzoux")) {
            return "lanzou";
        } else if (lowerUrl.contains("xunlei.com") || lowerUrl.contains("thunder")) {
            return "xunlei";
        } else if (lowerUrl.endsWith(".zip") || lowerUrl.endsWith(".rar") || 
                   lowerUrl.endsWith(".7z") || lowerUrl.endsWith(".tar.gz") ||
                   lowerUrl.endsWith(".exe") || lowerUrl.endsWith(".dmg") ||
                   lowerUrl.endsWith(".apk") || lowerUrl.endsWith(".deb")) {
            return "direct";
        } else {
            return "guanfang"; // 官方链接作为默认
        }
    }

    /**
     * 提取分页链接
     */
    private List<String> extractPaginationLinks(Document doc, WebsiteStructure structure) {
        List<String> links = new ArrayList<>();
        // TODO: 实现分页链接提取逻辑
        return links;
    }


    
    /**
     * URL和深度的配对类
     */
    private static class UrlDepthPair {
        String url;
        int depth;
        
        UrlDepthPair(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }
}
