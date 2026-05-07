package com.resource.platform.module.revenue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.BizErrorCode;
import com.resource.platform.common.PageResult;
import com.resource.platform.module.revenue.entity.Revenue;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.module.revenue.mapper.RevenueMapper;
import com.resource.platform.module.revenue.service.RevenueService;
import com.resource.platform.module.revenue.vo.RevenueOverviewVO;
import com.resource.platform.module.revenue.vo.RevenueTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收益服务实现类
 * 
 * 功能说明：
 * - 实现收益数据的核心业务逻辑
 * - 处理收益统计和分析计算
 * - 管理收益记录的增删改查操作
 * - 提供收益数据的分类统计功能
 * - 确保收益数据的准确性和一致性
 * 
 * 主要职责：
 * - 收益数据的统计计算
 * - 收益记录的持久化管理
 * - 收益类型的分类统计
 * - 时间周期的数据过滤
 * - 收益概览的数据聚合
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class RevenueServiceImpl implements RevenueService {

    private static final int MAX_PAGE_SIZE = 100;
    
    @Autowired
    private RevenueMapper revenueMapper;
    
    /**
     * 收益类型名称映射表（不可变，防止意外修改）
     * 用于将英文类型代码转换为中文显示名称
     */
    private static final Map<String, String> TYPE_NAME_MAP;
    
    static {
        Map<String, String> mutableMap = new HashMap<>();
        mutableMap.put("cloud_storage", "云存储");
        mutableMap.put("download_revenue", "下载收益");
        mutableMap.put("mobile_cloud", "移动云盘");
        mutableMap.put("mobile_cloud_backup", "移动云盘（备用）");
        mutableMap.put("uc_cloud", "UC网盘");
        mutableMap.put("12_cloud", "12云盘");
        mutableMap.put("lanzou_cloud", "蓝奏云");
        mutableMap.put("chengtong_cloud", "城通网盘");
        TYPE_NAME_MAP = java.util.Collections.unmodifiableMap(mutableMap);
    }
    
    /**
     * 获取收益概览
     * 
     * 业务逻辑：
     * 1. 验证时间周期参数的有效性
     * 2. 计算指定周期的开始时间
     * 3. 统计总收益、下载次数等关键指标
     * 4. 封装概览数据并返回
     * 
     * @param period 时间周期
     * @return 收益概览统计数据
     * @throws BusinessException 当时间周期参数无效时抛出
     */
    @Override
    public RevenueOverviewVO getOverview(String period) {
        // 记录业务开始
        log.info("执行获取收益概览业务逻辑: period={}", period);
        
        // 步骤1：验证参数
        // 检查时间周期参数是否有效
        if (period == null || period.trim().isEmpty()) {
            period = "today";
            log.debug("修正时间周期参数: period=today");
        }
        
        // 步骤2：创建概览对象
        // 初始化收益概览VO对象
        log.debug("创建收益概览对象");
        RevenueOverviewVO overview = new RevenueOverviewVO();
        overview.setPeriod(period);
        
        // 步骤3：计算时间范围
        // 根据周期参数计算开始时间
        log.debug("计算时间范围: period={}", period);
        String startTime = getStartTime(period);
        log.debug("计算得到开始时间: startTime={}", startTime);
        
        // 步骤4：统计总收益
        // 查询指定时间范围内的总收益
        log.debug("查询总收益: startTime={}", startTime);
        BigDecimal totalRevenue = revenueMapper.getTotalRevenue(startTime);
        overview.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        log.info("总收益统计完成: totalRevenue={}", overview.getTotalRevenue());
        
        // 步骤5：统计总下载次数
        // 查询指定时间范围内的总下载次数
        log.debug("查询总下载次数: startTime={}", startTime);
        Integer totalDownloads = revenueMapper.getTotalDownloads(startTime);
        overview.setTotalDownloads(totalDownloads != null ? totalDownloads : 0);
        log.info("总下载次数统计完成: totalDownloads={}", overview.getTotalDownloads());
        
        // 步骤6：统计收益项数量
        // 查询指定时间范围内的收益记录数量
        log.debug("查询收益项数量: startTime={}", startTime);
        LambdaQueryWrapper<Revenue> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Revenue::getCreateTime, 
            LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Long count = revenueMapper.selectCount(wrapper);
        overview.setRevenueItemCount(count == null ? 0 : count.intValue());
        log.info("收益项数量统计完成: itemCount={}", overview.getRevenueItemCount());
        
        // 记录业务完成
        log.info("获取收益概览业务逻辑执行完成: period={}, totalRevenue={}, totalDownloads={}, itemCount={}", 
            period, overview.getTotalRevenue(), overview.getTotalDownloads(), overview.getRevenueItemCount());
        
        return overview;
    }
    
    /**
     * 按类型获取收益统计
     * 
     * 业务逻辑：
     * 1. 验证时间周期参数的有效性
     * 2. 计算指定周期的开始时间
     * 3. 查询各类型的收益统计数据
     * 4. 确保所有类型都有数据（即使为0）
     * 5. 封装类型统计结果并返回
     * 
     * @param period 时间周期
     * @return 按类型分组的收益统计列表
     * @throws BusinessException 当时间周期参数无效时抛出
     */
    @Override
    public List<RevenueTypeVO> getRevenueByType(String period) {
        // 记录业务开始
        log.info("执行按类型获取收益统计业务逻辑: period={}", period);
        
        // 步骤1：验证参数
        // 检查时间周期参数是否有效
        if (period == null || period.trim().isEmpty()) {
            period = "today";
            log.debug("修正时间周期参数: period=today");
        }
        
        // 步骤2：计算时间范围
        // 根据周期参数计算开始时间
        log.debug("计算时间范围: period={}", period);
        String startTime = getStartTime(period);
        log.debug("计算得到开始时间: startTime={}", startTime);
        
        // 步骤3：查询收益类型统计数据
        // 从数据库查询各类型的收益统计
        log.debug("查询收益类型统计数据: startTime={}", startTime);
        List<Map<String, Object>> dataList = revenueMapper.getRevenueByType(startTime);
        log.info("查询到收益类型数据: count={}", dataList.size());
        
        // 步骤4：构建结果列表
        // 确保所有类型都有数据，即使金额为0
        log.debug("构建收益类型统计结果");
        List<RevenueTypeVO> result = new ArrayList<>();
        
        // 遍历所有预定义的收益类型
        for (Map.Entry<String, String> entry : TYPE_NAME_MAP.entrySet()) {
            // 创建类型统计VO对象
            RevenueTypeVO vo = new RevenueTypeVO();
            vo.setRevenueType(entry.getKey());
            vo.setTypeName(entry.getValue());
            
            // 设置默认值
            vo.setTotalAmount(BigDecimal.ZERO);
            vo.setDownloadCount(0);
            vo.setAccumulatedRevenue(BigDecimal.ZERO);
            
            log.debug("处理收益类型: type={}, typeName={}", entry.getKey(), entry.getValue());
            
            // 步骤5：查找对应的统计数据
            // 在查询结果中查找当前类型的数据
            for (Map<String, Object> data : dataList) {
                if (entry.getKey().equals(data.get("revenue_type"))) {
                    // 找到对应数据，更新统计值
                    BigDecimal totalAmount = (BigDecimal) data.get("total_amount");
                    Number downloadCountValue = (Number) data.get("total_downloads");
                    Integer downloadCount = downloadCountValue == null ? 0 : downloadCountValue.intValue();
                    
                    vo.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
                    vo.setDownloadCount(downloadCount);
                    vo.setAccumulatedRevenue(totalAmount != null ? totalAmount : BigDecimal.ZERO);
                    
                    log.debug("找到收益类型数据: type={}, totalAmount={}, downloadCount={}", 
                        entry.getKey(), vo.getTotalAmount(), vo.getDownloadCount());
                    break;
                }
            }
            
            result.add(vo);
        }
        
        // 记录业务完成
        log.info("按类型获取收益统计业务逻辑执行完成: period={}, typeCount={}", 
            period, result.size());
        
        // 记录各类型的统计结果
        for (RevenueTypeVO vo : result) {
            log.debug("收益类型统计结果: type={}, typeName={}, totalAmount={}, downloadCount={}", 
                vo.getRevenueType(), vo.getTypeName(), vo.getTotalAmount(), vo.getDownloadCount());
        }
        
        return result;
    }
    
    /**
     * 获取收益明细列表
     * 
     * 业务逻辑：
     * 1. 验证分页参数的有效性
     * 2. 构建动态查询条件（时间、类型、状态过滤）
     * 3. 执行分页查询并排序
     * 4. 封装分页结果并返回
     * 
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param period 时间周期，可选
     * @param revenueType 收益类型，可选
     * @param status 状态，可选
     * @return 收益明细分页结果
     */
    @Override
    public PageResult<Revenue> getRevenueList(Integer pageNum, Integer pageSize, String period, String revenueType, String status) {
        // 记录业务开始
        log.info("执行获取收益明细列表业务逻辑: pageNum={}, pageSize={}, period={}, revenueType={}, status={}", 
            pageNum, pageSize, period, revenueType, status);
        
        // 步骤1：验证参数
        // 检查分页参数的合理性
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
            log.debug("修正页码参数: pageNum=1");
        }
        
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
            log.debug("修正页大小参数: pageSize=10");
        } else if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
            log.debug("页大小超过上限，修正为: pageSize={}", MAX_PAGE_SIZE);
        }
        
        // 步骤2：构建查询条件
        // 创建分页对象
        log.debug("创建分页对象: pageNum={}, pageSize={}", pageNum, pageSize);
        Page<Revenue> page = new Page<>(pageNum, pageSize);
        
        // 创建Lambda查询条件，支持动态查询
        log.debug("构建查询条件");
        LambdaQueryWrapper<Revenue> wrapper = new LambdaQueryWrapper<>();
        
        // 时间筛选
        if (period != null && !period.isEmpty()) {
            String startTime = getStartTime(period);
            wrapper.ge(Revenue::getCreateTime, 
                LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.debug("添加时间查询条件: period={}, startTime={}", period, startTime);
        }
        
        // 类型筛选
        if (revenueType != null && !revenueType.isEmpty()) {
            wrapper.eq(Revenue::getRevenueType, revenueType);
            log.debug("添加类型查询条件: revenueType={}", revenueType);
        }
        
        // 状态筛选
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Revenue::getStatus, status);
            log.debug("添加状态查询条件: status={}", status);
        }
        
        // 排序条件：按创建时间降序
        wrapper.orderByDesc(Revenue::getCreateTime);
        
        // 步骤3：执行分页查询
        // 执行查询获取收益明细列表
        log.debug("执行分页查询");
        IPage<Revenue> pageResult = revenueMapper.selectPage(page, wrapper);
        log.info("分页查询完成: total={}, records={}", 
            pageResult.getTotal(), pageResult.getRecords().size());
        
        // 步骤4：封装分页结果
        // 创建分页结果对象
        log.debug("封装分页结果");
        PageResult<Revenue> result = new PageResult<>(pageResult.getTotal(), pageResult.getRecords());
        
        // 记录每个收益记录的基本信息
        for (Revenue revenue : pageResult.getRecords()) {
            log.debug("收益记录: id={}, type={}, amount={}, status={}", 
                revenue.getId(), revenue.getRevenueType(), revenue.getAmount(), revenue.getStatus());
        }
        
        // 记录业务完成
        log.info("获取收益明细列表业务逻辑执行完成: total={}, currentPageRecords={}", 
            result.getTotal(), result.getRecords().size());
        
        return result;
    }
    
    /**
     * 删除收益记录
     * 
     * 业务逻辑：
     * 1. 验证收益记录ID的有效性
     * 2. 检查收益记录是否存在
     * 3. 记录删除前的收益信息
     * 4. 执行删除操作
     * 5. 验证删除结果
     * 
     * @param id 收益记录ID
     * @return 删除是否成功
     * @throws BusinessException 当ID无效时抛出
     * @throws ResourceNotFoundException 当收益记录不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRevenue(Long id) {
        log.info("执行删除收益记录业务逻辑: id={}", id);

        if (id == null || id <= 0) {
            log.warn("收益记录ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "收益记录ID无效");
        }

        log.debug("检查收益记录是否存在: id={}", id);
        Revenue existingRevenue = revenueMapper.selectById(id);
        if (existingRevenue == null) {
            log.warn("收益记录不存在: id={}", id);
            throw new ResourceNotFoundException("收益记录不存在");
        }

        log.info("准备删除收益记录: id={}, type={}, amount={}, status={}",
            existingRevenue.getId(), existingRevenue.getRevenueType(),
            existingRevenue.getAmount(), existingRevenue.getStatus());

        log.debug("执行删除操作: id={}", id);
        int rows = revenueMapper.deleteById(id);
        boolean success = rows > 0;
        if (success) {
            log.info("收益记录删除成功: id={}, type={}, amount={}, rows={}",
                id, existingRevenue.getRevenueType(), existingRevenue.getAmount(), rows);
        } else {
            log.error("收益记录删除失败，影响行数为0: id={}", id);
            throw new BusinessException("删除收益记录失败");
        }

        log.info("删除收益记录业务逻辑执行完成: id={}, success={}", id, success);
        return true;
    }

    /**
     * 批量删除收益记录
     * 
     * 业务逻辑：
     * 1. 验证ID列表的有效性
     * 2. 检查ID列表是否为空
     * 3. 验证每个ID的有效性
     * 4. 记录删除前的收益信息
     * 5. 执行批量删除操作
     * 6. 验证删除结果
     * 
     * @param ids 收益记录ID列表
     * @return 删除是否成功
     * @throws BusinessException 当ID列表无效时抛出
     * @throws BusinessException 当ID列表为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteRevenue(List<Long> ids) {
        log.info("执行批量删除收益记录业务逻辑: ids={}, count={}", ids, ids != null ? ids.size() : 0);

        if (ids == null || ids.isEmpty()) {
            log.warn("批量删除收益记录ID列表为空");
            throw new BusinessException("删除ID列表不能为空");
        }

        log.debug("验证ID列表中的每个ID");
        for (Long id : ids) {
            if (id == null || id <= 0) {
                log.warn("批量删除包含无效ID: id={}", id);
                throw new BusinessException(BizErrorCode.PARAM_ERROR, "包含无效的收益记录ID: " + id);
            }
        }

        log.debug("查询要删除的收益记录信息");
        List<Revenue> toDeleteRevenues = revenueMapper.selectBatchIds(ids);
        if (toDeleteRevenues.size() != ids.size()) {
            throw new ResourceNotFoundException("部分收益记录不存在或已被删除");
        }
        log.info("准备批量删除收益记录: existingCount={}, requestCount={}", toDeleteRevenues.size(), ids.size());

        BigDecimal totalDeleteAmount = BigDecimal.ZERO;
        for (Revenue revenue : toDeleteRevenues) {
            log.debug("准备删除收益记录: id={}, type={}, amount={}, status={}",
                revenue.getId(), revenue.getRevenueType(), revenue.getAmount(), revenue.getStatus());

            if (revenue.getAmount() != null) {
                totalDeleteAmount = totalDeleteAmount.add(revenue.getAmount());
            }
        }
        log.info("批量删除涉及总金额: totalAmount={}", totalDeleteAmount);

        log.debug("执行批量删除操作: count={}", ids.size());
        int rows = revenueMapper.deleteBatchIds(ids);
        boolean success = rows == ids.size();
        if (success) {
            log.info("收益记录批量删除成功: requestCount={}, deletedCount={}, totalAmount={}",
                ids.size(), rows, totalDeleteAmount);
        } else {
            throw new BusinessException("批量删除收益记录失败");
        }

        log.info("批量删除收益记录业务逻辑执行完成: requestCount={}, deletedCount={}, success={}",
            ids.size(), rows, success);
        return true;
    }

    private String getStartTime(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        
        switch (period) {
            case "yesterday":
                startTime = now.minusDays(1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "week":
                startTime = now.minusDays(7);
                break;
            case "month":
                startTime = now.minusDays(30);
                break;
            case "today":
            default:
                startTime = now.withHour(0).withMinute(0).withSecond(0);
                break;
        }
        
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
