package com.resource.platform.module.crawler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.BizErrorCode;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.module.category.dto.CategoryMappingItem;
import com.resource.platform.module.crawler.dto.CrawlerTaskDTO;
import com.resource.platform.module.crawler.dto.CrawlerTaskQueryDTO;
import com.resource.platform.module.crawler.dto.CustomRules;
import com.resource.platform.module.crawler.entity.CrawlerTask;
import com.resource.platform.module.resource.entity.Resource;
import com.resource.platform.module.crawler.mapper.CrawlerTaskMapper;
import com.resource.platform.module.resource.mapper.ResourceMapper;
import com.resource.platform.module.crawler.service.CrawlerTaskService;
import com.resource.platform.module.crawler.vo.CrawlerStatistics;
import com.resource.platform.module.crawler.vo.CrawlerTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CrawlerTaskServiceImpl implements CrawlerTaskService {

    /** 分页查询每页最大条数 */
    private static final int MAX_PAGE_SIZE = 100;
    
    @Autowired
    private CrawlerTaskMapper crawlerTaskMapper;
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrawlerTaskVO createTask(CrawlerTaskDTO dto) {
        // 验证URL
        if (!validateTargetUrl(dto.getTargetUrl())) {
            throw new BusinessException(BizErrorCode.CRAWLER_URL_INVALID);
        }
        
        // 创建任务实体
        CrawlerTask task = new CrawlerTask();
        task.setName(dto.getName());
        task.setTargetUrl(dto.getTargetUrl());
        task.setStatus(dto.getStatus() ? 1 : 0);
        task.setCrawlInterval(dto.getCrawlInterval());
        task.setMaxDepth(dto.getMaxDepth());
        task.setIntelligentMode(dto.getIntelligentMode() ? 1 : 0);
        task.setTotalCrawled(0);
        task.setTotalSuccess(0);
        task.setTotalFailed(0);
        
        // 转换分类映射为JSON
        if (dto.getCategoryMapping() != null && dto.getCategoryMapping().length > 0) {
            try {
                // 将数组转换为Map
                Map<String, Long> mappingMap = new HashMap<>();
                for (CategoryMappingItem item : dto.getCategoryMapping()) {
                    if (item.getKey() != null && item.getValue() != null) {
                        mappingMap.put(item.getKey(), item.getValue());
                    }
                }
                task.setCategoryMapping(objectMapper.writeValueAsString(mappingMap));
            } catch (JsonProcessingException e) {
                log.error("分类映射JSON转换失败", e);
                throw new BusinessException(BizErrorCode.CRAWLER_CATEGORY_MAPPING_ERROR, e);
            }
        }
        
        // 转换自定义规则为JSON
        if (dto.getCustomRules() != null) {
            try {
                task.setCustomRules(objectMapper.writeValueAsString(dto.getCustomRules()));
            } catch (JsonProcessingException e) {
                log.error("自定义规则JSON转换失败", e);
                throw new BusinessException(BizErrorCode.CRAWLER_CUSTOM_RULES_ERROR, e);
            }
        }
        
        // 计算下次执行时间
        if (dto.getStatus()) {
            task.setNextExecuteTime(LocalDateTime.now().plusHours(dto.getCrawlInterval()));
        }
        
        crawlerTaskMapper.insert(task);
        
        return convertToVO(task);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrawlerTaskVO updateTask(Long id, CrawlerTaskDTO dto) {
        CrawlerTask task = crawlerTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(BizErrorCode.CRAWLER_TASK_NOT_FOUND);
        }
        Integer previousCrawlInterval = task.getCrawlInterval();
        
        // 验证URL（如果URL有变化）
        if (!java.util.Objects.equals(task.getTargetUrl(), dto.getTargetUrl())) {
            if (!validateTargetUrl(dto.getTargetUrl())) {
                throw new BusinessException(BizErrorCode.CRAWLER_URL_INVALID);
            }
        }
        
        // 更新任务信息
        task.setName(dto.getName());
        task.setTargetUrl(dto.getTargetUrl());
        task.setStatus(dto.getStatus() ? 1 : 0);
        task.setCrawlInterval(dto.getCrawlInterval());
        task.setMaxDepth(dto.getMaxDepth());
        task.setIntelligentMode(dto.getIntelligentMode() ? 1 : 0);
        
        // 转换分类映射为JSON
        if (dto.getCategoryMapping() != null && dto.getCategoryMapping().length > 0) {
            try {
                // 将数组转换为Map
                Map<String, Long> mappingMap = new HashMap<>();
                for (CategoryMappingItem item : dto.getCategoryMapping()) {
                    if (item.getKey() != null && item.getValue() != null) {
                        mappingMap.put(item.getKey(), item.getValue());
                    }
                }
                task.setCategoryMapping(objectMapper.writeValueAsString(mappingMap));
            } catch (JsonProcessingException e) {
                log.error("分类映射JSON转换失败", e);
                throw new BusinessException(BizErrorCode.CRAWLER_CATEGORY_MAPPING_ERROR, e);
            }
        }
        
        // 转换自定义规则为JSON
        if (dto.getCustomRules() != null) {
            try {
                task.setCustomRules(objectMapper.writeValueAsString(dto.getCustomRules()));
            } catch (JsonProcessingException e) {
                log.error("自定义规则JSON转换失败", e);
                throw new BusinessException(BizErrorCode.CRAWLER_CUSTOM_RULES_ERROR, e);
            }
        }
        
        // 更新下次执行时间（如果间隔有变化且任务已启用）
        if (dto.getStatus() && !java.util.Objects.equals(dto.getCrawlInterval(), previousCrawlInterval)) {
            task.setNextExecuteTime(LocalDateTime.now().plusHours(dto.getCrawlInterval()));
        }
        
        crawlerTaskMapper.updateById(task);
        
        return convertToVO(task);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id, Boolean deleteResources) {
        CrawlerTask task = crawlerTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(BizErrorCode.CRAWLER_TASK_NOT_FOUND);
        }
        
        // 如果需要删除关联资源
        if (deleteResources != null && deleteResources) {
            LambdaUpdateWrapper<Resource> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Resource::getCrawlerTaskId, id);
            wrapper.set(Resource::getDeleted, 1);
            resourceMapper.update(null, wrapper);
        }
        
        // 逻辑删除任务
        crawlerTaskMapper.deleteById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrawlerTaskVO toggleTaskStatus(Long id) {
        CrawlerTask task = crawlerTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(BizErrorCode.CRAWLER_TASK_NOT_FOUND);
        }
        
        // 切换状态
        Integer newStatus = Integer.valueOf(1).equals(task.getStatus()) ? 0 : 1;
        task.setStatus(newStatus);
        
        // 如果启用任务，设置下次执行时间
        if (Integer.valueOf(1).equals(newStatus)) {
            task.setNextExecuteTime(LocalDateTime.now().plusHours(task.getCrawlInterval()));
        } else {
            task.setNextExecuteTime(null);
        }
        
        crawlerTaskMapper.updateById(task);
        
        return convertToVO(task);
    }
    
    @Override
    public PageResult<CrawlerTaskVO> queryTasks(CrawlerTaskQueryDTO query) {
        long safePageNum = query.getPage() == null || query.getPage() < 1 ? 1L : query.getPage();
        int safePageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : Math.min(query.getPageSize(), MAX_PAGE_SIZE);
        Page<CrawlerTask> page = new Page<>(safePageNum, safePageSize);
        
        LambdaQueryWrapper<CrawlerTask> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(CrawlerTask::getName, query.getName());
        }
        
        if (query.getStatus() != null) {
            wrapper.eq(CrawlerTask::getStatus, query.getStatus());
        }
        
        wrapper.orderByDesc(CrawlerTask::getCreateTime);
        
        Page<CrawlerTask> result = crawlerTaskMapper.selectPage(page, wrapper);
        
        List<CrawlerTaskVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(result.getTotal(), voList);
    }
    
    @Override
    public CrawlerTaskVO getTaskDetail(Long id) {
        CrawlerTask task = crawlerTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(BizErrorCode.CRAWLER_TASK_NOT_FOUND);
        }
        return convertToVO(task);
    }
    
    @Override
    public void triggerTask(Long id) {
        CrawlerTask task = crawlerTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(BizErrorCode.CRAWLER_TASK_NOT_FOUND);
        }
        
        // 这里将在后续实现CrawlerExecutionService时调用
        // crawlerExecutionService.executeCrawlerTask(id, 2);
        log.info("手动触发爬虫任务: {}", task.getName());
    }
    
    @Override
    public Boolean validateTargetUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        
        // 验证URL格式
        if (!url.matches("^https?://.*")) {
            return false;
        }
        
        // 验证URL可达性
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHead request = new HttpHead(new URI(url));
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                return statusCode == HttpStatus.SC_OK || 
                       statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                       statusCode == HttpStatus.SC_MOVED_TEMPORARILY;
            }
        } catch (Exception e) {
            log.error("URL验证失败: {}", url, e);
            return false;
        }
    }
    
    /**
     * 转换实体为VO
     */
    private CrawlerTaskVO convertToVO(CrawlerTask task) {
        CrawlerTaskVO vo = new CrawlerTaskVO();
        BeanUtils.copyProperties(task, vo);
        
        // 转换智能模式：整数转布尔值
        vo.setIntelligentMode(Integer.valueOf(1).equals(task.getIntelligentMode()));
        
        // 转换状态：整数转布尔值
        vo.setStatus(Integer.valueOf(1).equals(task.getStatus()));
        
        // 转换分类映射
        if (StringUtils.hasText(task.getCategoryMapping())) {
            try {
                Map<String, Long> mapping = objectMapper.readValue(
                    task.getCategoryMapping(), 
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Long.class)
                );
                // 将Map转换为数组
                CategoryMappingItem[] mappingArray = mapping.entrySet().stream()
                    .map(entry -> new CategoryMappingItem(entry.getKey(), entry.getValue()))
                    .toArray(CategoryMappingItem[]::new);
                vo.setCategoryMapping(mappingArray);
            } catch (JsonProcessingException e) {
                log.error("分类映射JSON解析失败", e);
            }
        }
        
        // 转换自定义规则
        if (StringUtils.hasText(task.getCustomRules())) {
            try {
                CustomRules rules = objectMapper.readValue(task.getCustomRules(), CustomRules.class);
                vo.setCustomRules(rules);
            } catch (JsonProcessingException e) {
                log.error("自定义规则JSON解析失败", e);
            }
        }
        
        // 设置统计信息
        CrawlerStatistics statistics = new CrawlerStatistics(
            task.getTotalCrawled(),
            task.getTotalSuccess(),
            task.getTotalFailed()
        );
        vo.setStatistics(statistics);
        
        return vo;
    }
}
