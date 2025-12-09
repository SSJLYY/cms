package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.dto.AdvertisementDTO;
import com.resource.platform.entity.Advertisement;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.mapper.AdvertisementMapper;
import com.resource.platform.service.PromotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推广服务实现类
 * 
 * 功能说明：
 * - 实现推广广告的核心业务逻辑
 * - 处理广告的增删改查操作
 * - 管理广告的状态、排序和展示规则
 * - 提供广告点击统计功能
 * - 确保广告数据的一致性和有效性
 * 
 * 主要职责：
 * - 广告数据的持久化管理
 * - 广告的业务规则验证
 * - 广告的时间范围控制
 * - 广告的位置和排序管理
 * - 广告点击统计和分析
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class PromotionServiceImpl implements PromotionService {
    
    @Autowired
    private AdvertisementMapper advertisementMapper;
    
    /**
     * 获取广告列表
     * 
     * 业务逻辑：
     * 1. 验证分页参数的有效性
     * 2. 构建动态查询条件（位置过滤）
     * 3. 执行分页查询并排序
     * 4. 封装分页结果并返回
     * 
     * @param page 页码
     * @param pageSize 页大小
     * @param position 广告位置，可选
     * @return 广告列表分页结果
     */
    @Override
    public PageResult<Advertisement> getAdvertisementList(Integer page, Integer pageSize, String position) {
        // 记录业务开始
        log.info("执行获取广告列表业务逻辑: page={}, pageSize={}, position={}", 
            page, pageSize, position);
        
        // 步骤1：验证参数
        // 检查分页参数的合理性
        if (page == null || page < 1) {
            page = 1;
            log.debug("修正页码参数: page=1");
        }
        
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
            log.debug("修正页大小参数: pageSize=20");
        }
        
        // 步骤2：构建查询条件
        // 创建分页对象
        log.debug("创建分页对象: page={}, pageSize={}", page, pageSize);
        Page<Advertisement> pageParam = new Page<>(page, pageSize);
        
        // 创建Lambda查询条件，支持动态查询
        log.debug("构建查询条件");
        LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();
        
        // 位置过滤
        if (position != null && !position.isEmpty() && !"all".equals(position)) {
            wrapper.eq(Advertisement::getPosition, position);
            log.debug("添加位置查询条件: position={}", position);
        }
        
        // 排序条件：先按排序字段升序，再按创建时间降序
        wrapper.orderByAsc(Advertisement::getSortOrder)
               .orderByDesc(Advertisement::getCreateTime);
        
        // 步骤3：执行分页查询
        // 执行查询获取广告列表
        log.debug("执行分页查询");
        Page<Advertisement> resultPage = advertisementMapper.selectPage(pageParam, wrapper);
        log.info("分页查询完成: total={}, records={}", 
            resultPage.getTotal(), resultPage.getRecords().size());
        
        // 步骤4：封装分页结果
        // 创建分页结果对象
        log.debug("封装分页结果");
        PageResult<Advertisement> pageResult = new PageResult<>(
            resultPage.getTotal(),
            resultPage.getRecords()
        );
        
        // 记录每个广告的基本信息
        for (Advertisement ad : resultPage.getRecords()) {
            log.debug("广告信息: id={}, title={}, position={}, status={}", 
                ad.getId(), ad.getTitle(), ad.getPosition(), ad.getStatus());
        }
        
        // 记录业务完成
        log.info("获取广告列表业务逻辑执行完成: total={}, currentPageRecords={}", 
            pageResult.getTotal(), pageResult.getRecords().size());
        
        return pageResult;
    }
    
    /**
     * 根据ID获取广告详情
     * 
     * 业务逻辑：
     * 1. 验证广告ID的有效性
     * 2. 从数据库查询指定ID的广告
     * 3. 检查广告是否存在
     * 4. 返回广告详细信息
     * 
     * @param id 广告ID
     * @return 广告详细信息
     * @throws IllegalArgumentException 当ID无效时抛出
     * @throws ResourceNotFoundException 当广告不存在时抛出
     */
    @Override
    public Advertisement getAdvertisementById(Long id) {
        // 记录业务开始
        log.info("执行根据ID获取广告业务逻辑: id={}", id);
        
        // 步骤1：验证参数
        // 检查广告ID是否有效
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new IllegalArgumentException("广告ID无效");
        }
        
        // 步骤2：查询数据库
        // 根据ID查询广告
        log.debug("查询广告: id={}", id);
        Advertisement advertisement = advertisementMapper.selectById(id);
        
        // 步骤3：检查查询结果
        // 如果广告不存在，抛出异常
        if (advertisement == null) {
            log.warn("广告不存在: id={}", id);
            throw new ResourceNotFoundException("广告不存在");
        }
        
        // 记录查询成功
        log.info("广告查询成功: id={}, title={}, position={}, status={}", 
            advertisement.getId(), advertisement.getTitle(), 
            advertisement.getPosition(), advertisement.getStatus());
        
        // 记录业务完成
        log.info("根据ID获取广告业务逻辑执行完成: id={}, title={}", 
            advertisement.getId(), advertisement.getTitle());
        
        return advertisement;
    }
    
    /**
     * 创建广告
     * 
     * 业务逻辑：
     * 1. 验证广告数据的完整性
     * 2. 检查广告标题是否重复
     * 3. 验证广告位置和时间范围
     * 4. 设置默认值
     * 5. 保存广告到数据库
     * 
     * @param dto 广告数据传输对象
     * @throws IllegalArgumentException 当参数验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     */
    @Override
    @Transactional
    public void createAdvertisement(AdvertisementDTO dto) {
        // 记录业务开始
        log.info("执行创建广告业务逻辑: title={}, position={}, type={}", 
            dto.getTitle(), dto.getPosition(), dto.getType());
        
        // 步骤1：验证参数
        // 检查DTO对象是否为空
        if (dto == null) {
            log.warn("广告DTO为空");
            throw new IllegalArgumentException("广告数据不能为空");
        }
        
        // 检查必填字段
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            log.warn("广告标题为空");
            throw new IllegalArgumentException("广告标题不能为空");
        }
        
        if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
            log.warn("广告位置为空");
            throw new IllegalArgumentException("广告位置不能为空");
        }
        
        // 步骤2：检查标题重复
        // 查询数据库中是否存在同标题的广告
        log.debug("检查广告标题是否重复: title={}", dto.getTitle());
        LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Advertisement::getTitle, dto.getTitle());
        long count = advertisementMapper.selectCount(wrapper);
        
        if (count > 0) {
            log.warn("广告标题已存在: title={}, count={}", dto.getTitle(), count);
            throw new BusinessException("广告标题已存在");
        }
        
        // 步骤3：创建实体对象
        // 将DTO转换为实体对象
        log.debug("创建广告实体对象");
        Advertisement advertisement = new Advertisement();
        BeanUtils.copyProperties(dto, advertisement);
        
        // 步骤4：设置默认值
        // 设置默认排序值
        if (advertisement.getSortOrder() == null) {
            advertisement.setSortOrder(0);
            log.debug("设置默认排序值: sortOrder=0");
        }
        
        // 设置默认状态
        if (advertisement.getStatus() == null) {
            advertisement.setStatus(1); // 默认启用
            log.debug("设置默认状态: status=1");
        }
        
        // 设置默认点击数
        if (advertisement.getClickCount() == null) {
            advertisement.setClickCount(0);
            log.debug("设置默认点击数: clickCount=0");
        }
        
        // 步骤5：保存到数据库
        // 执行插入操作
        log.debug("保存广告到数据库");
        int rows = advertisementMapper.insert(advertisement);
        
        // 验证插入结果
        if (rows > 0) {
            log.info("广告保存成功: id={}, title={}, position={}, rows={}", 
                advertisement.getId(), advertisement.getTitle(), 
                advertisement.getPosition(), rows);
        } else {
            log.error("广告保存失败，影响行数为0: title={}, position={}", 
                advertisement.getTitle(), advertisement.getPosition());
            throw new RuntimeException("广告保存失败");
        }
        
        // 记录业务完成
        log.info("创建广告业务逻辑执行完成: id={}, title={}", 
            advertisement.getId(), advertisement.getTitle());
    }
    
    /**
     * 更新广告
     * 
     * 业务逻辑：
     * 1. 验证广告ID和数据的有效性
     * 2. 检查广告是否存在
     * 3. 验证广告标题是否重复（排除自己）
     * 4. 更新广告信息
     * 5. 记录更新操作日志
     * 
     * @param id 广告ID
     * @param dto 广告数据传输对象
     * @throws IllegalArgumentException 当参数验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     * @throws ResourceNotFoundException 当广告不存在时抛出
     */
    @Override
    @Transactional
    public void updateAdvertisement(Long id, AdvertisementDTO dto) {
        // 记录业务开始
        log.info("执行更新广告业务逻辑: id={}, title={}, position={}", 
            id, dto.getTitle(), dto.getPosition());
        
        // 步骤1：验证参数
        // 检查广告ID是否有效
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new IllegalArgumentException("广告ID无效");
        }
        
        // 检查DTO对象是否为空
        if (dto == null) {
            log.warn("广告DTO为空: id={}", id);
            throw new IllegalArgumentException("广告数据不能为空");
        }
        
        // 检查必填字段
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            log.warn("广告标题为空: id={}", id);
            throw new IllegalArgumentException("广告标题不能为空");
        }
        
        if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
            log.warn("广告位置为空: id={}", id);
            throw new IllegalArgumentException("广告位置不能为空");
        }
        
        // 步骤2：检查广告是否存在
        // 查询现有的广告
        log.debug("检查广告是否存在: id={}", id);
        Advertisement advertisement = advertisementMapper.selectById(id);
        if (advertisement == null) {
            log.warn("广告不存在: id={}", id);
            throw new ResourceNotFoundException("广告不存在");
        }
        
        // 记录更新前的信息
        log.info("更新前广告信息: id={}, oldTitle={}, oldPosition={}, oldStatus={}", 
            advertisement.getId(), advertisement.getTitle(), 
            advertisement.getPosition(), advertisement.getStatus());
        
        // 步骤3：检查标题重复（排除自己）
        // 查询是否有其他广告使用相同标题
        log.debug("检查广告标题是否重复: id={}, title={}", id, dto.getTitle());
        LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Advertisement::getTitle, dto.getTitle())
               .ne(Advertisement::getId, id);
        long count = advertisementMapper.selectCount(wrapper);
        
        if (count > 0) {
            log.warn("广告标题已存在: id={}, title={}, count={}", 
                id, dto.getTitle(), count);
            throw new BusinessException("广告标题已存在");
        }
        
        // 步骤4：更新广告信息
        // 将DTO数据复制到实体对象
        log.debug("准备更新广告实体对象");
        BeanUtils.copyProperties(dto, advertisement);
        advertisement.setId(id);
        
        // 执行更新操作
        log.debug("更新广告到数据库: id={}", id);
        int rows = advertisementMapper.updateById(advertisement);
        
        // 验证更新结果
        if (rows > 0) {
            log.info("广告更新成功: id={}, title={}, position={}, rows={}", 
                id, advertisement.getTitle(), advertisement.getPosition(), rows);
        } else {
            log.error("广告更新失败，影响行数为0: id={}, title={}", 
                id, advertisement.getTitle());
            throw new RuntimeException("广告更新失败");
        }
        
        // 记录业务完成
        log.info("更新广告业务逻辑执行完成: id={}, title={}", 
            id, advertisement.getTitle());
    }
    
    /**
     * 删除广告
     * 
     * 业务逻辑：
     * 1. 验证广告ID的有效性
     * 2. 检查广告是否存在
     * 3. 记录删除前的广告信息
     * 4. 执行删除操作
     * 5. 验证删除结果
     * 
     * @param id 广告ID
     * @throws IllegalArgumentException 当ID无效时抛出
     * @throws ResourceNotFoundException 当广告不存在时抛出
     */
    @Override
    @Transactional
    public void deleteAdvertisement(Long id) {
        // 记录业务开始
        log.info("执行删除广告业务逻辑: id={}", id);
        
        // 步骤1：验证参数
        // 检查广告ID是否有效
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new IllegalArgumentException("广告ID无效");
        }
        
        // 步骤2：检查广告是否存在
        // 先查询广告是否存在，记录删除前的信息
        log.debug("检查广告是否存在: id={}", id);
        Advertisement advertisement = advertisementMapper.selectById(id);
        
        if (advertisement == null) {
            log.warn("广告不存在: id={}", id);
            throw new ResourceNotFoundException("广告不存在");
        }
        
        // 记录删除前的广告信息
        log.info("准备删除广告: id={}, title={}, position={}, status={}, clickCount={}", 
            advertisement.getId(), advertisement.getTitle(), advertisement.getPosition(), 
            advertisement.getStatus(), advertisement.getClickCount());
        
        // 步骤3：执行删除操作
        // 从数据库中删除广告
        log.debug("执行删除操作: id={}", id);
        int rows = advertisementMapper.deleteById(id);
        
        // 步骤4：验证删除结果
        // 检查删除操作是否成功
        if (rows > 0) {
            log.info("广告删除成功: id={}, title={}, rows={}", 
                id, advertisement.getTitle(), rows);
        } else {
            log.error("广告删除失败，影响行数为0: id={}, title={}", 
                id, advertisement.getTitle());
            throw new RuntimeException("广告删除失败");
        }
        
        // 记录业务完成
        log.info("删除广告业务逻辑执行完成: id={}", id);
    }
    
    /**
     * 更新广告状态
     * 
     * 业务逻辑：
     * 1. 验证广告ID和状态的有效性
     * 2. 检查广告是否存在
     * 3. 记录状态变更前的信息
     * 4. 更新广告状态
     * 5. 验证更新结果
     * 
     * @param id 广告ID
     * @param status 新状态（0-禁用，1-启用）
     * @throws IllegalArgumentException 当参数无效时抛出
     * @throws ResourceNotFoundException 当广告不存在时抛出
     */
    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        // 记录业务开始
        log.info("执行更新广告状态业务逻辑: id={}, status={}", id, status);
        
        // 步骤1：验证参数
        // 检查广告ID是否有效
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new IllegalArgumentException("广告ID无效");
        }
        
        // 检查状态值是否有效
        if (status == null || (status != 0 && status != 1)) {
            log.warn("广告状态值无效: id={}, status={}", id, status);
            throw new IllegalArgumentException("状态值无效，只能是0（禁用）或1（启用）");
        }
        
        // 步骤2：检查广告是否存在
        // 查询现有的广告
        log.debug("检查广告是否存在: id={}", id);
        Advertisement advertisement = advertisementMapper.selectById(id);
        if (advertisement == null) {
            log.warn("广告不存在: id={}", id);
            throw new ResourceNotFoundException("广告不存在");
        }
        
        // 记录状态变更前的信息
        Integer oldStatus = advertisement.getStatus();
        String oldStatusText = oldStatus == 1 ? "启用" : "禁用";
        String newStatusText = status == 1 ? "启用" : "禁用";
        
        log.info("广告状态变更: id={}, title={}, oldStatus={}({}), newStatus={}({})", 
            id, advertisement.getTitle(), oldStatus, oldStatusText, status, newStatusText);
        
        // 检查状态是否需要变更
        if (oldStatus.equals(status)) {
            log.info("广告状态无需变更: id={}, status={}({})", 
                id, status, newStatusText);
            return;
        }
        
        // 步骤3：更新广告状态
        // 设置新状态
        advertisement.setStatus(status);
        
        // 执行更新操作
        log.debug("更新广告状态到数据库: id={}, status={}", id, status);
        int rows = advertisementMapper.updateById(advertisement);
        
        // 步骤4：验证更新结果
        // 检查更新操作是否成功
        if (rows > 0) {
            log.info("广告状态更新成功: id={}, title={}, status={}({}), rows={}", 
                id, advertisement.getTitle(), status, newStatusText, rows);
        } else {
            log.error("广告状态更新失败，影响行数为0: id={}, status={}", id, status);
            throw new RuntimeException("广告状态更新失败");
        }
        
        // 记录业务完成
        log.info("更新广告状态业务逻辑执行完成: id={}, status={}({})", 
            id, status, newStatusText);
    }
    
    /**
     * 更新广告排序
     * 
     * 业务逻辑：
     * 1. 验证广告ID和排序值的有效性
     * 2. 检查广告是否存在
     * 3. 记录排序变更前的信息
     * 4. 更新广告排序值
     * 5. 验证更新结果
     * 
     * @param id 广告ID
     * @param sortOrder 新排序值
     * @throws IllegalArgumentException 当参数无效时抛出
     * @throws ResourceNotFoundException 当广告不存在时抛出
     */
    @Override
    @Transactional
    public void updateSortOrder(Long id, Integer sortOrder) {
        // 记录业务开始
        log.info("执行更新广告排序业务逻辑: id={}, sortOrder={}", id, sortOrder);
        
        // 步骤1：验证参数
        // 检查广告ID是否有效
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new IllegalArgumentException("广告ID无效");
        }
        
        // 检查排序值是否有效
        if (sortOrder == null || sortOrder < 0) {
            log.warn("广告排序值无效: id={}, sortOrder={}", id, sortOrder);
            throw new IllegalArgumentException("排序值无效，必须大于等于0");
        }
        
        // 步骤2：检查广告是否存在
        // 查询现有的广告
        log.debug("检查广告是否存在: id={}", id);
        Advertisement advertisement = advertisementMapper.selectById(id);
        if (advertisement == null) {
            log.warn("广告不存在: id={}", id);
            throw new ResourceNotFoundException("广告不存在");
        }
        
        // 记录排序变更前的信息
        Integer oldSortOrder = advertisement.getSortOrder();
        log.info("广告排序变更: id={}, title={}, oldSortOrder={}, newSortOrder={}", 
            id, advertisement.getTitle(), oldSortOrder, sortOrder);
        
        // 检查排序是否需要变更
        if (oldSortOrder != null && oldSortOrder.equals(sortOrder)) {
            log.info("广告排序无需变更: id={}, sortOrder={}", id, sortOrder);
            return;
        }
        
        // 步骤3：更新广告排序
        // 设置新排序值
        advertisement.setSortOrder(sortOrder);
        
        // 执行更新操作
        log.debug("更新广告排序到数据库: id={}, sortOrder={}", id, sortOrder);
        int rows = advertisementMapper.updateById(advertisement);
        
        // 步骤4：验证更新结果
        // 检查更新操作是否成功
        if (rows > 0) {
            log.info("广告排序更新成功: id={}, title={}, sortOrder={}, rows={}", 
                id, advertisement.getTitle(), sortOrder, rows);
        } else {
            log.error("广告排序更新失败，影响行数为0: id={}, sortOrder={}", id, sortOrder);
            throw new RuntimeException("广告排序更新失败");
        }
        
        // 记录业务完成
        log.info("更新广告排序业务逻辑执行完成: id={}, sortOrder={}", id, sortOrder);
    }
    
    /**
     * 记录广告点击
     * 
     * 业务逻辑：
     * 1. 验证广告ID的有效性
     * 2. 检查广告是否存在且启用
     * 3. 增加广告点击计数
     * 4. 更新点击统计到数据库
     * 5. 记录点击统计日志
     * 
     * @param id 广告ID
     * @throws IllegalArgumentException 当ID无效时抛出
     * @throws ResourceNotFoundException 当广告不存在时抛出
     * @throws BusinessException 当广告未启用时抛出
     */
    @Override
    @Transactional
    public void recordClick(Long id) {
        // 记录业务开始
        log.info("执行记录广告点击业务逻辑: id={}", id);
        
        // 步骤1：验证参数
        // 检查广告ID是否有效
        if (id == null || id <= 0) {
            log.warn("广告ID无效: id={}", id);
            throw new IllegalArgumentException("广告ID无效");
        }
        
        // 步骤2：检查广告是否存在
        // 查询现有的广告
        log.debug("检查广告是否存在: id={}", id);
        Advertisement advertisement = advertisementMapper.selectById(id);
        if (advertisement == null) {
            log.warn("广告不存在: id={}", id);
            throw new ResourceNotFoundException("广告不存在");
        }
        
        // 检查广告是否启用
        if (advertisement.getStatus() != 1) {
            log.warn("广告未启用，无法记录点击: id={}, status={}", id, advertisement.getStatus());
            throw new BusinessException("广告未启用");
        }
        
        // 记录点击前的信息
        Integer oldClickCount = advertisement.getClickCount();
        if (oldClickCount == null) {
            oldClickCount = 0;
        }
        Integer newClickCount = oldClickCount + 1;
        
        log.info("广告点击统计: id={}, title={}, oldClickCount={}, newClickCount={}", 
            id, advertisement.getTitle(), oldClickCount, newClickCount);
        
        // 步骤3：更新点击计数
        // 增加点击计数
        advertisement.setClickCount(newClickCount);
        
        // 执行更新操作
        log.debug("更新广告点击计数到数据库: id={}, clickCount={}", id, newClickCount);
        int rows = advertisementMapper.updateById(advertisement);
        
        // 步骤4：验证更新结果
        // 检查更新操作是否成功
        if (rows > 0) {
            log.info("广告点击记录成功: id={}, title={}, clickCount={}, rows={}", 
                id, advertisement.getTitle(), newClickCount, rows);
        } else {
            log.error("广告点击记录失败，影响行数为0: id={}, clickCount={}", id, newClickCount);
            throw new RuntimeException("广告点击记录失败");
        }
        
        // 记录业务完成
        log.info("记录广告点击业务逻辑执行完成: id={}, clickCount={}", id, newClickCount);
    }
    
    /**
     * 获取广告位置选项
     * 
     * 业务逻辑：
     * 1. 定义系统支持的所有广告位置
     * 2. 构建位置选项列表
     * 3. 返回位置选项供前端使用
     * 
     * @return 广告位置选项列表
     */
    @Override
    public List<Map<String, String>> getPositionOptions() {
        // 记录业务开始
        log.info("执行获取广告位置选项业务逻辑");
        
        // 步骤1：创建位置选项列表
        // 定义系统支持的广告位置
        log.debug("构建广告位置选项列表");
        List<Map<String, String>> options = new ArrayList<>();
        
        // 首页位置
        Map<String, String> option1 = new HashMap<>();
        option1.put("value", "homepage");
        option1.put("label", "首页");
        options.add(option1);
        log.debug("添加位置选项: value=homepage, label=首页");
        
        // 下载页位置
        Map<String, String> option2 = new HashMap<>();
        option2.put("value", "download");
        option2.put("label", "下载页");
        options.add(option2);
        log.debug("添加位置选项: value=download, label=下载页");
        
        // 分类页位置
        Map<String, String> option3 = new HashMap<>();
        option3.put("value", "category");
        option3.put("label", "分类页");
        options.add(option3);
        log.debug("添加位置选项: value=category, label=分类页");
        
        // 自定义页位置
        Map<String, String> option4 = new HashMap<>();
        option4.put("value", "custom");
        option4.put("label", "自定义页");
        options.add(option4);
        log.debug("添加位置选项: value=custom, label=自定义页");
        
        // 记录业务完成
        log.info("获取广告位置选项业务逻辑执行完成: count={}", options.size());
        
        return options;
    }
    
    /**
     * 获取用户端有效广告
     * 
     * 业务逻辑：
     * 1. 验证广告位置参数的有效性
     * 2. 构建查询条件（启用状态、指定位置）
     * 3. 过滤时间范围内的有效广告
     * 4. 按排序字段排序返回
     * 
     * @param position 广告位置
     * @return 有效的广告列表
     * @throws IllegalArgumentException 当位置参数无效时抛出
     */
    @Override
    public List<Advertisement> getActiveAdvertisements(String position) {
        // 记录业务开始
        log.info("执行获取用户端有效广告业务逻辑: position={}", position);
        
        // 步骤1：验证参数
        // 检查广告位置是否有效
        if (position == null || position.trim().isEmpty()) {
            log.warn("广告位置为空");
            throw new IllegalArgumentException("广告位置不能为空");
        }
        
        // 步骤2：构建查询条件
        // 创建Lambda查询条件
        log.debug("构建有效广告查询条件: position={}", position);
        LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询启用状态的广告
        wrapper.eq(Advertisement::getStatus, 1);
        log.debug("添加状态查询条件: status=1");
        
        // 指定位置的广告
        wrapper.eq(Advertisement::getPosition, position);
        log.debug("添加位置查询条件: position={}", position);
        
        // 步骤3：检查时间范围
        // 获取当前时间用于时间范围过滤
        LocalDateTime now = LocalDateTime.now();
        log.debug("当前时间: {}", now);
        
        // 开始时间条件：为空或小于等于当前时间
        wrapper.and(w -> w.isNull(Advertisement::getStartTime)
                .or()
                .le(Advertisement::getStartTime, now));
        log.debug("添加开始时间查询条件");
        
        // 结束时间条件：为空或大于等于当前时间
        wrapper.and(w -> w.isNull(Advertisement::getEndTime)
                .or()
                .ge(Advertisement::getEndTime, now));
        log.debug("添加结束时间查询条件");
        
        // 排序条件：按排序字段升序
        wrapper.orderByAsc(Advertisement::getSortOrder);
        
        // 步骤4：执行查询
        // 查询有效的广告列表
        log.debug("查询有效广告");
        List<Advertisement> advertisements = advertisementMapper.selectList(wrapper);
        log.info("查询到有效广告: position={}, count={}", position, advertisements.size());
        
        // 记录每个有效广告的信息
        for (Advertisement ad : advertisements) {
            log.debug("有效广告: id={}, title={}, sortOrder={}, startTime={}, endTime={}", 
                ad.getId(), ad.getTitle(), ad.getSortOrder(), 
                ad.getStartTime(), ad.getEndTime());
        }
        
        // 记录业务完成
        log.info("获取用户端有效广告业务逻辑执行完成: position={}, count={}", 
            position, advertisements.size());
        
        return advertisements;
    }
}
