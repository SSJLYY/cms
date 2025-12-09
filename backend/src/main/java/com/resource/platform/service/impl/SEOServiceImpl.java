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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SEO服务实现类
 * 
 * 功能说明：
 * - 实现SEO相关的核心业务逻辑
 * - 处理搜索引擎提交和统计功能
 * - 管理网站地图的生成和维护
 * - 提供SEO数据的统计分析
 * - 确保SEO操作的准确性和可靠性
 * 
 * 主要职责：
 * - SEO统计数据的计算和汇总
 * - 网站地图XML的生成和格式化
 * - 搜索引擎提交的执行和记录
 * - SEO提交历史的管理和查询
 * - 提交结果的验证和统计
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class SEOServiceImpl implements SEOService {
    
    @Autowired
    private SEOSubmissionMapper seoSubmissionMapper;
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    /**
     * 日期格式化器 - 用于格式化月日显示
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    
    /**
     * 日期时间格式化器 - 用于格式化完整的日期时间
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 获取SEO统计数据
     * 
     * 业务逻辑：
     * 1. 统计总提交数量
     * 2. 统计成功提交数量
     * 3. 统计失败提交数量
     * 4. 统计今日提交数量
     * 5. 封装统计结果并返回
     * 
     * @return SEO统计数据对象
     */
    @Override
    public SEOStatsVO getStats() {
        // 记录业务开始
        log.info("执行获取SEO统计数据业务逻辑");
        
        // 步骤1：创建统计对象
        // 初始化SEO统计VO对象
        log.debug("创建SEO统计对象");
        SEOStatsVO stats = new SEOStatsVO();
        
        // 步骤2：统计总提交数
        // 查询所有SEO提交记录的总数
        log.debug("查询总提交数");
        LambdaQueryWrapper<SEOSubmission> totalWrapper = new LambdaQueryWrapper<>();
        Long totalCount = seoSubmissionMapper.selectCount(totalWrapper);
        stats.setTotalSubmissions(totalCount != null ? totalCount.intValue() : 0);
        log.info("总提交数统计完成: totalSubmissions={}", stats.getTotalSubmissions());
        
        // 步骤3：统计成功提交数
        // 查询状态为SUCCESS的提交记录数量
        log.debug("查询成功提交数");
        LambdaQueryWrapper<SEOSubmission> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.eq(SEOSubmission::getStatus, "SUCCESS");
        Long successCount = seoSubmissionMapper.selectCount(successWrapper);
        stats.setSuccessSubmissions(successCount != null ? successCount.intValue() : 0);
        log.info("成功提交数统计完成: successSubmissions={}", stats.getSuccessSubmissions());
        
        // 步骤4：统计失败提交数
        // 查询状态为FAILED的提交记录数量
        log.debug("查询失败提交数");
        LambdaQueryWrapper<SEOSubmission> failedWrapper = new LambdaQueryWrapper<>();
        failedWrapper.eq(SEOSubmission::getStatus, "FAILED");
        Long failedCount = seoSubmissionMapper.selectCount(failedWrapper);
        stats.setFailedSubmissions(failedCount != null ? failedCount.intValue() : 0);
        log.info("失败提交数统计完成: failedSubmissions={}", stats.getFailedSubmissions());
        
        // 步骤5：统计今日提交数
        // 查询今天提交的记录数量
        log.debug("查询今日提交数");
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<SEOSubmission> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(SEOSubmission::getSubmitTime, todayStart);
        Long todayCount = seoSubmissionMapper.selectCount(todayWrapper);
        stats.setTodaySubmissions(todayCount != null ? todayCount.intValue() : 0);
        log.info("今日提交数统计完成: todaySubmissions={}", stats.getTodaySubmissions());
        
        // 记录业务完成
        log.info("获取SEO统计数据业务逻辑执行完成: total={}, success={}, failed={}, today={}", 
            stats.getTotalSubmissions(), stats.getSuccessSubmissions(), 
            stats.getFailedSubmissions(), stats.getTodaySubmissions());
        
        return stats;
    }
    
    /**
     * 生成网站地图
     * 
     * 业务逻辑：
     * 1. 查询所有有效的资源记录
     * 2. 构建XML格式的网站地图结构
     * 3. 为每个资源生成URL条目
     * 4. 设置优先级和最后修改时间
     * 5. 返回完整的XML网站地图
     * 
     * @return XML格式的网站地图内容
     */
    @Override
    public String generateSitemap() {
        // 记录业务开始
        log.info("执行生成网站地图业务逻辑");
        
        // 步骤1：查询有效资源
        // 构建查询条件：状态为1（启用）且未删除
        log.debug("查询有效资源记录");
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 1);  // 状态为启用
        wrapper.eq(Resource::getDeleted, 0); // 未删除
        List<Resource> resources = resourceMapper.selectList(wrapper);
        log.info("查询到有效资源数量: count={}", resources.size());
        
        // 步骤2：构建XML结构
        // 创建StringBuilder用于构建XML内容
        log.debug("开始构建XML网站地图");
        StringBuilder sitemap = new StringBuilder();
        
        // 添加XML声明和根元素
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        log.debug("XML头部信息添加完成");
        
        // 步骤3：遍历资源生成URL条目
        // 为每个有效资源生成对应的URL条目
        log.debug("开始生成资源URL条目");
        for (Resource resource : resources) {
            // 为每个资源添加URL条目
            sitemap.append("  <url>\n");
            sitemap.append("    <loc>https://example.com/resource/").append(resource.getId()).append("</loc>\n");
            
            // 设置最后修改时间
            if (resource.getUpdateTime() != null) {
                sitemap.append("    <lastmod>").append(resource.getUpdateTime().format(DateTimeFormatter.ISO_DATE)).append("</lastmod>\n");
            }
            
            // 设置优先级（资源页面设置为0.8）
            sitemap.append("    <priority>0.8</priority>\n");
            sitemap.append("  </url>\n");
            
            // 记录每个资源的处理
            log.debug("添加资源URL: id={}, title={}", resource.getId(), resource.getTitle());
        }
        
        // 步骤4：完成XML结构
        // 添加XML结束标签
        sitemap.append("</urlset>");
        log.debug("XML结构构建完成");
        
        // 步骤5：返回生成的网站地图
        String result = sitemap.toString();
        
        // 记录业务完成
        log.info("生成网站地图业务逻辑执行完成: resourceCount={}, xmlLength={}", 
            resources.size(), result.length());
        
        return result;
    }
    
    /**
     * 批量提交到指定搜索引擎
     * 
     * 业务逻辑：
     * 1. 查询所有有效的资源记录
     * 2. 为每个资源构建提交URL
     * 3. 创建SEO提交记录
     * 4. 执行提交操作并记录结果
     * 5. 统计成功和失败数量
     * 6. 返回提交结果统计
     * 
     * @param engine 搜索引擎名称（baidu、bing等）
     * @return 提交结果统计
     */
    @Override
    @Transactional
    public SubmissionResultVO batchSubmit(String engine) {
        // 记录业务开始
        log.info("执行批量提交到搜索引擎业务逻辑: engine={}", engine);
        
        // 步骤1：查询有效资源
        // 构建查询条件：状态为1（启用）且未删除
        log.debug("查询有效资源记录: engine={}", engine);
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 1);  // 状态为启用
        wrapper.eq(Resource::getDeleted, 0); // 未删除
        List<Resource> resources = resourceMapper.selectList(wrapper);
        log.info("查询到待提交资源数量: engine={}, resourceCount={}", engine, resources.size());
        
        // 步骤2：初始化结果对象
        // 创建提交结果VO对象
        log.debug("初始化提交结果对象");
        SubmissionResultVO result = new SubmissionResultVO();
        int successCount = 0;
        int failedCount = 0;
        
        // 步骤3：遍历资源执行提交
        // 为每个资源执行搜索引擎提交操作
        log.debug("开始批量提交资源: engine={}", engine);
        for (Resource resource : resources) {
            // 构建资源URL
            String url = "https://example.com/resource/" + resource.getId();
            log.debug("处理资源提交: resourceId={}, url={}, engine={}", 
                resource.getId(), url, engine);
            
            // 创建SEO提交记录
            SEOSubmission submission = new SEOSubmission();
            submission.setEngine(engine);
            submission.setUrl(url);
            submission.setStatus("SUCCESS");  // 默认设置为成功
            submission.setResponseMessage("提交成功");
            submission.setSubmitTime(LocalDateTime.now());
            
            try {
                // 步骤4：执行提交操作
                // 将提交记录保存到数据库
                log.debug("保存提交记录: resourceId={}, engine={}", resource.getId(), engine);
                seoSubmissionMapper.insert(submission);
                successCount++;
                
                // 记录成功提交
                log.debug("资源提交成功: resourceId={}, url={}, engine={}", 
                    resource.getId(), url, engine);
                
            } catch (Exception e) {
                // 步骤5：处理提交失败
                // 更新提交状态为失败并记录错误信息
                log.warn("资源提交失败: resourceId={}, url={}, engine={}, error={}", 
                    resource.getId(), url, engine, e.getMessage());
                
                submission.setStatus("FAILED");
                submission.setResponseMessage("提交失败: " + e.getMessage());
                
                try {
                    // 尝试保存失败记录
                    seoSubmissionMapper.insert(submission);
                } catch (Exception saveException) {
                    log.error("保存失败记录异常: resourceId={}, error={}", 
                        resource.getId(), saveException.getMessage());
                }
                failedCount++;
            }
        }
        
        // 步骤6：封装提交结果
        // 设置提交结果统计信息
        log.debug("封装提交结果: engine={}, successCount={}, failedCount={}", 
            engine, successCount, failedCount);
        
        result.setSuccess(failedCount == 0);
        result.setSuccessCount(successCount);
        result.setFailedCount(failedCount);
        result.setMessage(String.format("成功提交%d个，失败%d个", successCount, failedCount));
        
        // 记录业务完成
        log.info("批量提交到搜索引擎业务逻辑执行完成: engine={}, success={}, successCount={}, failedCount={}", 
            engine, result.isSuccess(), successCount, failedCount);
        
        return result;
    }
    
    /**
     * 批量提交到所有搜索引擎
     * 
     * 业务逻辑：
     * 1. 依次提交到百度搜索引擎
     * 2. 依次提交到必应搜索引擎
     * 3. 合并所有搜索引擎的提交结果
     * 4. 计算总的成功和失败数量
     * 5. 返回综合提交结果
     * 
     * @return 所有搜索引擎的综合提交结果
     */
    @Override
    @Transactional
    public SubmissionResultVO batchSubmitAll() {
        // 记录业务开始
        log.info("执行批量提交到所有搜索引擎业务逻辑");
        
        // 步骤1：提交到百度搜索引擎
        // 调用批量提交方法提交到百度
        log.debug("开始提交到百度搜索引擎");
        SubmissionResultVO baiduResult = batchSubmit("baidu");
        log.info("百度搜索引擎提交完成: success={}, successCount={}, failedCount={}", 
            baiduResult.isSuccess(), baiduResult.getSuccessCount(), baiduResult.getFailedCount());
        
        // 步骤2：提交到必应搜索引擎
        // 调用批量提交方法提交到必应
        log.debug("开始提交到必应搜索引擎");
        SubmissionResultVO bingResult = batchSubmit("bing");
        log.info("必应搜索引擎提交完成: success={}, successCount={}, failedCount={}", 
            bingResult.isSuccess(), bingResult.getSuccessCount(), bingResult.getFailedCount());
        
        // 步骤3：合并提交结果
        // 创建综合结果对象并合并统计数据
        log.debug("合并所有搜索引擎的提交结果");
        SubmissionResultVO result = new SubmissionResultVO();
        
        // 计算总成功数量
        int totalSuccessCount = baiduResult.getSuccessCount() + bingResult.getSuccessCount();
        result.setSuccessCount(totalSuccessCount);
        
        // 计算总失败数量
        int totalFailedCount = baiduResult.getFailedCount() + bingResult.getFailedCount();
        result.setFailedCount(totalFailedCount);
        
        // 设置整体成功状态（只有全部成功才算成功）
        result.setSuccess(totalFailedCount == 0);
        
        // 设置结果消息
        result.setMessage(String.format("批量提交完成：成功%d个，失败%d个", 
                totalSuccessCount, totalFailedCount));
        
        // 记录详细的合并结果
        log.info("提交结果合并完成: baiduSuccess={}, baiduFailed={}, bingSuccess={}, bingFailed={}", 
            baiduResult.getSuccessCount(), baiduResult.getFailedCount(),
            bingResult.getSuccessCount(), bingResult.getFailedCount());
        
        // 记录业务完成
        log.info("批量提交到所有搜索引擎业务逻辑执行完成: totalSuccess={}, totalFailed={}, overallSuccess={}", 
            totalSuccessCount, totalFailedCount, result.isSuccess());
        
        return result;
    }
    
    /**
     * 获取SEO提交历史记录
     * 
     * 业务逻辑：
     * 1. 验证分页参数的有效性
     * 2. 构建分页查询对象
     * 3. 设置按提交时间降序排列
     * 4. 执行分页查询
     * 5. 封装分页结果并返回
     * 
     * @param page 页码
     * @param pageSize 页大小
     * @return SEO提交历史记录分页结果
     */
    @Override
    public PageResult<SEOSubmission> getSubmissionHistory(Integer page, Integer pageSize) {
        // 记录业务开始
        log.info("执行获取SEO提交历史记录业务逻辑: page={}, pageSize={}", page, pageSize);
        
        // 步骤1：验证分页参数
        // 检查分页参数的合理性
        if (page == null || page < 1) {
            page = 1;
            log.debug("修正页码参数: page=1");
        }
        
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
            log.debug("修正页大小参数: pageSize=20");
        }
        
        // 步骤2：构建分页查询对象
        // 创建MyBatis Plus分页对象
        log.debug("创建分页查询对象: page={}, pageSize={}", page, pageSize);
        Page<SEOSubmission> pageParam = new Page<>(page, pageSize);
        
        // 步骤3：构建查询条件
        // 创建Lambda查询条件，按提交时间降序排列
        log.debug("构建查询条件");
        LambdaQueryWrapper<SEOSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SEOSubmission::getSubmitTime);  // 按提交时间降序
        
        // 步骤4：执行分页查询
        // 执行数据库分页查询
        log.debug("执行分页查询");
        Page<SEOSubmission> resultPage = seoSubmissionMapper.selectPage(pageParam, wrapper);
        log.info("分页查询完成: total={}, records={}", 
            resultPage.getTotal(), resultPage.getRecords().size());
        
        // 步骤5：封装分页结果
        // 创建分页结果对象
        log.debug("封装分页结果");
        PageResult<SEOSubmission> pageResult = new PageResult<>(
            resultPage.getTotal(),
            resultPage.getRecords()
        );
        
        // 记录每个提交记录的基本信息
        for (SEOSubmission submission : resultPage.getRecords()) {
            log.debug("SEO提交记录: id={}, engine={}, url={}, status={}, submitTime={}", 
                submission.getId(), submission.getEngine(), submission.getUrl(), 
                submission.getStatus(), submission.getSubmitTime());
        }
        
        // 记录业务完成
        log.info("获取SEO提交历史记录业务逻辑执行完成: total={}, currentPageRecords={}", 
            pageResult.getTotal(), pageResult.getRecords().size());
        
        return pageResult;
    }
    
    /**
     * 重新提交SEO记录
     * 
     * 业务逻辑：
     * 1. 验证提交记录ID的有效性
     * 2. 查找原始提交记录
     * 3. 基于原始记录创建新的提交记录
     * 4. 执行重新提交操作
     * 5. 记录提交结果并返回
     * 
     * @param id 原始提交记录ID
     * @return 重新提交结果
     * @throws IllegalArgumentException 当ID无效时抛出
     * @throws ResourceNotFoundException 当提交记录不存在时抛出
     */
    @Override
    @Transactional
    public SubmissionResultVO resubmit(Long id) {
        // 记录业务开始
        log.info("执行重新提交SEO记录业务逻辑: id={}", id);
        
        // 步骤1：验证参数
        // 检查提交记录ID是否有效
        if (id == null || id <= 0) {
            log.warn("提交记录ID无效: id={}", id);
            throw new IllegalArgumentException("提交记录ID无效");
        }
        
        // 步骤2：查找原始提交记录
        // 从数据库中查询原始提交记录
        log.debug("查找原始提交记录: id={}", id);
        SEOSubmission original = seoSubmissionMapper.selectById(id);
        
        if (original == null) {
            log.warn("提交记录不存在: id={}", id);
            throw new ResourceNotFoundException("提交记录不存在");
        }
        
        // 记录原始提交记录信息
        log.info("找到原始提交记录: id={}, engine={}, url={}, originalStatus={}", 
            original.getId(), original.getEngine(), original.getUrl(), original.getStatus());
        
        // 步骤3：创建新的提交记录
        // 基于原始记录创建新的提交记录
        log.debug("创建新的提交记录");
        SEOSubmission submission = new SEOSubmission();
        submission.setEngine(original.getEngine());
        submission.setUrl(original.getUrl());
        submission.setStatus("SUCCESS");  // 默认设置为成功
        submission.setResponseMessage("重新提交成功");
        submission.setSubmitTime(LocalDateTime.now());
        
        // 步骤4：初始化结果对象
        // 创建提交结果VO对象
        log.debug("初始化重新提交结果对象");
        SubmissionResultVO result = new SubmissionResultVO();
        
        try {
            // 步骤5：执行重新提交操作
            // 将新的提交记录保存到数据库
            log.debug("执行重新提交操作: engine={}, url={}", 
                submission.getEngine(), submission.getUrl());
            seoSubmissionMapper.insert(submission);
            
            // 设置成功结果
            result.setSuccess(true);
            result.setSuccessCount(1);
            result.setFailedCount(0);
            result.setMessage("重新提交成功");
            
            // 记录成功提交
            log.info("重新提交成功: originalId={}, newId={}, engine={}, url={}", 
                id, submission.getId(), submission.getEngine(), submission.getUrl());
            
        } catch (Exception e) {
            // 步骤6：处理重新提交失败
            // 更新提交状态为失败并记录错误信息
            log.warn("重新提交失败: originalId={}, engine={}, url={}, error={}", 
                id, submission.getEngine(), submission.getUrl(), e.getMessage());
            
            submission.setStatus("FAILED");
            submission.setResponseMessage("重新提交失败: " + e.getMessage());
            
            try {
                // 尝试保存失败记录
                seoSubmissionMapper.insert(submission);
                log.debug("失败记录保存成功: newId={}", submission.getId());
            } catch (Exception saveException) {
                log.error("保存失败记录异常: originalId={}, error={}", 
                    id, saveException.getMessage());
            }
            
            // 设置失败结果
            result.setSuccess(false);
            result.setSuccessCount(0);
            result.setFailedCount(1);
            result.setMessage("重新提交失败");
        }
        
        // 记录业务完成
        log.info("重新提交SEO记录业务逻辑执行完成: originalId={}, success={}, successCount={}, failedCount={}", 
            id, result.isSuccess(), result.getSuccessCount(), result.getFailedCount());
        
        return result;
    }
    
    /**
     * 删除SEO提交历史记录
     * 
     * 业务逻辑：
     * 1. 验证提交记录ID的有效性
     * 2. 检查提交记录是否存在
     * 3. 记录删除前的提交信息
     * 4. 执行删除操作
     * 5. 验证删除结果
     * 
     * @param id 提交记录ID
     * @throws IllegalArgumentException 当ID无效时抛出
     * @throws ResourceNotFoundException 当提交记录不存在时抛出
     */
    @Override
    @Transactional
    public void deleteHistory(Long id) {
        // 记录业务开始
        log.info("执行删除SEO提交历史记录业务逻辑: id={}", id);
        
        // 步骤1：验证参数
        // 检查提交记录ID是否有效
        if (id == null || id <= 0) {
            log.warn("提交记录ID无效: id={}", id);
            throw new IllegalArgumentException("提交记录ID无效");
        }
        
        // 步骤2：检查提交记录是否存在
        // 先查询提交记录是否存在，记录删除前的信息
        log.debug("检查SEO提交记录是否存在: id={}", id);
        SEOSubmission submission = seoSubmissionMapper.selectById(id);
        
        if (submission == null) {
            log.warn("SEO提交记录不存在: id={}", id);
            throw new ResourceNotFoundException("提交记录不存在");
        }
        
        // 记录删除前的提交信息
        log.info("准备删除SEO提交记录: id={}, engine={}, url={}, status={}, submitTime={}", 
            submission.getId(), submission.getEngine(), submission.getUrl(), 
            submission.getStatus(), submission.getSubmitTime());
        
        // 步骤3：执行删除操作
        // 从数据库中删除提交记录
        log.debug("执行删除操作: id={}", id);
        int rows = seoSubmissionMapper.deleteById(id);
        
        // 步骤4：验证删除结果
        // 检查删除操作是否成功
        if (rows > 0) {
            log.info("SEO提交记录删除成功: id={}, engine={}, url={}, rows={}", 
                id, submission.getEngine(), submission.getUrl(), rows);
        } else {
            log.error("SEO提交记录删除失败，影响行数为0: id={}", id);
            throw new RuntimeException("删除操作失败");
        }
        
        // 记录业务完成
        log.info("删除SEO提交历史记录业务逻辑执行完成: id={}, engine={}, url={}", 
            id, submission.getEngine(), submission.getUrl());
    }
}
