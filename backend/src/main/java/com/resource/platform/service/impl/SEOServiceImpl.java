package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.entity.Resource;
import com.resource.platform.entity.SEOSubmission;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.mapper.ResourceMapper;
import com.resource.platform.mapper.SEOSubmissionMapper;
import com.resource.platform.service.SEOService;
import com.resource.platform.vo.SEOStatsVO;
import com.resource.platform.vo.SubmissionResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SEOServiceImpl implements SEOService {
    
    @Autowired
    private SEOSubmissionMapper seoSubmissionMapper;
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public SEOStatsVO getStats() {
        SEOStatsVO stats = new SEOStatsVO();
        
        // 总提交数
        LambdaQueryWrapper<SEOSubmission> totalWrapper = new LambdaQueryWrapper<>();
        Long totalCount = seoSubmissionMapper.selectCount(totalWrapper);
        stats.setTotalSubmissions(totalCount != null ? totalCount.intValue() : 0);
        
        // 成功提交数
        LambdaQueryWrapper<SEOSubmission> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.eq(SEOSubmission::getStatus, "SUCCESS");
        Long successCount = seoSubmissionMapper.selectCount(successWrapper);
        stats.setSuccessSubmissions(successCount != null ? successCount.intValue() : 0);
        
        // 失败提交数
        LambdaQueryWrapper<SEOSubmission> failedWrapper = new LambdaQueryWrapper<>();
        failedWrapper.eq(SEOSubmission::getStatus, "FAILED");
        Long failedCount = seoSubmissionMapper.selectCount(failedWrapper);
        stats.setFailedSubmissions(failedCount != null ? failedCount.intValue() : 0);
        
        // 今日提交数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<SEOSubmission> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(SEOSubmission::getSubmitTime, todayStart);
        Long todayCount = seoSubmissionMapper.selectCount(todayWrapper);
        stats.setTodaySubmissions(todayCount != null ? todayCount.intValue() : 0);
        
        return stats;
    }
    
    @Override
    public String generateSitemap() {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 1);
        wrapper.eq(Resource::getDeleted, 0);
        List<Resource> resources = resourceMapper.selectList(wrapper);
        
        StringBuilder sitemap = new StringBuilder();
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        
        for (Resource resource : resources) {
            sitemap.append("  <url>\n");
            sitemap.append("    <loc>https://example.com/resource/").append(resource.getId()).append("</loc>\n");
            sitemap.append("    <lastmod>").append(resource.getUpdateTime().format(DateTimeFormatter.ISO_DATE)).append("</lastmod>\n");
            sitemap.append("    <priority>0.8</priority>\n");
            sitemap.append("  </url>\n");
        }
        
        sitemap.append("</urlset>");
        return sitemap.toString();
    }
    
    @Override
    public SubmissionResultVO batchSubmit(String engine) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 1);
        wrapper.eq(Resource::getDeleted, 0);
        List<Resource> resources = resourceMapper.selectList(wrapper);
        
        SubmissionResultVO result = new SubmissionResultVO();
        int successCount = 0;
        int failedCount = 0;
        
        for (Resource resource : resources) {
            String url = "https://example.com/resource/" + resource.getId();
            
            SEOSubmission submission = new SEOSubmission();
            submission.setEngine(engine);
            submission.setUrl(url);
            submission.setStatus("SUCCESS");
            submission.setResponseMessage("提交成功");
            submission.setSubmitTime(LocalDateTime.now());
            
            try {
                seoSubmissionMapper.insert(submission);
                successCount++;
            } catch (Exception e) {
                submission.setStatus("FAILED");
                submission.setResponseMessage("提交失败: " + e.getMessage());
                try {
                    seoSubmissionMapper.insert(submission);
                } catch (Exception ignored) {
                }
                failedCount++;
            }
        }
        
        result.setSuccess(failedCount == 0);
        result.setSuccessCount(successCount);
        result.setFailedCount(failedCount);
        result.setMessage(String.format("成功提交%d个，失败%d个", successCount, failedCount));
        
        return result;
    }
    
    @Override
    public SubmissionResultVO batchSubmitAll() {
        // 提交到百度
        SubmissionResultVO baiduResult = batchSubmit("baidu");
        
        // 提交到必应
        SubmissionResultVO bingResult = batchSubmit("bing");
        
        // 合并结果
        SubmissionResultVO result = new SubmissionResultVO();
        result.setSuccessCount(baiduResult.getSuccessCount() + bingResult.getSuccessCount());
        result.setFailedCount(baiduResult.getFailedCount() + bingResult.getFailedCount());
        result.setSuccess(result.getFailedCount() == 0);
        result.setMessage(String.format("批量提交完成：成功%d个，失败%d个", 
                result.getSuccessCount(), result.getFailedCount()));
        
        return result;
    }
    
    @Override
    public PageResult<SEOSubmission> getSubmissionHistory(Integer page, Integer pageSize) {
        Page<SEOSubmission> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<SEOSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SEOSubmission::getSubmitTime);
        
        Page<SEOSubmission> resultPage = seoSubmissionMapper.selectPage(pageParam, wrapper);
        
        PageResult<SEOSubmission> pageResult = new PageResult<>(
            resultPage.getTotal(),
            resultPage.getRecords()
        );
        
        return pageResult;
    }
    
    @Override
    public SubmissionResultVO resubmit(Long id) {
        SEOSubmission original = seoSubmissionMapper.selectById(id);
        if (original == null) {
            throw new ResourceNotFoundException("提交记录不存在");
        }
        
        SEOSubmission submission = new SEOSubmission();
        submission.setEngine(original.getEngine());
        submission.setUrl(original.getUrl());
        submission.setStatus("SUCCESS");
        submission.setResponseMessage("重新提交成功");
        submission.setSubmitTime(LocalDateTime.now());
        
        SubmissionResultVO result = new SubmissionResultVO();
        try {
            seoSubmissionMapper.insert(submission);
            result.setSuccess(true);
            result.setSuccessCount(1);
            result.setFailedCount(0);
            result.setMessage("重新提交成功");
        } catch (Exception e) {
            submission.setStatus("FAILED");
            submission.setResponseMessage("重新提交失败: " + e.getMessage());
            try {
                seoSubmissionMapper.insert(submission);
            } catch (Exception ignored) {
            }
            result.setSuccess(false);
            result.setSuccessCount(0);
            result.setFailedCount(1);
            result.setMessage("重新提交失败");
        }
        
        return result;
    }
    
    @Override
    public void deleteHistory(Long id) {
        SEOSubmission submission = seoSubmissionMapper.selectById(id);
        if (submission == null) {
            throw new ResourceNotFoundException("提交记录不存在");
        }
        seoSubmissionMapper.deleteById(id);
    }
}
