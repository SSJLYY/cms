package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.common.PageResult;
import com.resource.platform.dto.FriendLinkDTO;
import com.resource.platform.dto.FriendLinkQueryDTO;
import com.resource.platform.entity.FriendLink;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.mapper.FriendLinkMapper;
import com.resource.platform.service.FriendLinkService;
import com.resource.platform.vo.FriendLinkVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 友情链接服务实现类
 * 
 * 功能说明：
 * - 实现友情链接的核心业务逻辑
 * - 处理友情链接的增删改查操作
 * - 管理友情链接的状态和排序
 * - 提供友情链接的数据验证功能
 * - 确保友情链接数据的一致性和有效性
 * 
 * 主要职责：
 * - 友情链接数据的持久化管理
 * - 友情链接的业务规则验证
 * - 友情链接的格式化和转换
 * - 友情链接的排序和过滤
 * - URL格式的验证和处理
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendLinkServiceImpl implements FriendLinkService {
    
    private final FriendLinkMapper friendLinkMapper;
    
    /**
     * 分页查询友情链接
     * 
     * 业务逻辑：
     * 1. 验证分页查询参数的有效性
     * 2. 构建动态查询条件（名称模糊查询、状态过滤）
     * 3. 执行分页查询并排序
     * 4. 将实体对象转换为VO对象
     * 5. 封装分页结果并返回
     * 
     * @param queryDTO 查询条件DTO
     * @return 分页查询结果
     */
    @Override
    public PageResult<FriendLinkVO> queryPage(FriendLinkQueryDTO queryDTO) {
        // 记录业务开始
        log.info("执行友情链接分页查询业务逻辑: page={}, pageSize={}, name={}, status={}", 
            queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getName(), queryDTO.getStatus());
        
        // 步骤1：验证参数
        // 检查分页参数的合理性
        if (queryDTO.getPage() == null || queryDTO.getPage() < 1) {
            queryDTO.setPage(1);
            log.debug("修正页码参数: page=1");
        }
        
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
            log.debug("修正页大小参数: pageSize=10");
        }
        
        // 步骤2：构建查询条件
        // 创建Lambda查询条件，支持动态查询
        log.debug("构建查询条件");
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        
        // 名称模糊查询
        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(FriendLink::getName, queryDTO.getName());
            log.debug("添加名称模糊查询条件: name={}", queryDTO.getName());
        }
        
        // 状态精确查询
        if (queryDTO.getStatus() != null) {
            wrapper.eq(FriendLink::getStatus, queryDTO.getStatus());
            log.debug("添加状态查询条件: status={}", queryDTO.getStatus());
        }
        
        // 排序条件：先按排序字段升序，再按创建时间降序
        wrapper.orderByAsc(FriendLink::getSortOrder)
               .orderByDesc(FriendLink::getCreateTime);
        
        // 步骤3：执行分页查询
        // 创建分页对象并执行查询
        log.debug("执行分页查询: page={}, pageSize={}", queryDTO.getPage(), queryDTO.getPageSize());
        Page<FriendLink> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<FriendLink> result = friendLinkMapper.selectPage(page, wrapper);
        
        log.info("分页查询完成: total={}, records={}", result.getTotal(), result.getRecords().size());
        
        // 步骤4：转换为VO对象
        // 使用Stream API将实体对象转换为VO对象
        log.debug("转换实体对象为VO对象");
        List<FriendLinkVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 步骤5：封装分页结果
        PageResult<FriendLinkVO> pageResult = new PageResult<>(result.getTotal(), voList);
        
        // 记录业务完成
        log.info("友情链接分页查询业务逻辑执行完成: total={}, currentPageRecords={}", 
            pageResult.getTotal(), voList.size());
        
        return pageResult;
    }
    
    /**
     * 获取所有启用的友情链接
     * 
     * 业务逻辑：
     * 1. 构建查询条件，只查询启用状态的友情链接
     * 2. 按排序字段和创建时间排序
     * 3. 将实体对象转换为VO对象
     * 4. 返回启用的友情链接列表
     * 
     * @return 启用的友情链接VO列表
     */
    @Override
    public List<FriendLinkVO> listEnabled() {
        // 记录业务开始
        log.info("执行获取启用友情链接业务逻辑");
        
        // 步骤1：构建查询条件
        // 只查询状态为启用（1）的友情链接
        log.debug("构建启用友情链接查询条件");
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendLink::getStatus, 1)                    // 只查询启用状态
               .orderByAsc(FriendLink::getSortOrder)            // 按排序字段升序
               .orderByDesc(FriendLink::getCreateTime);         // 按创建时间降序
        
        // 步骤2：执行查询
        // 查询所有启用的友情链接
        log.debug("查询启用的友情链接");
        List<FriendLink> list = friendLinkMapper.selectList(wrapper);
        log.info("查询到启用友情链接: count={}", list.size());
        
        // 步骤3：转换为VO对象
        // 使用Stream API将实体对象转换为VO对象
        log.debug("转换启用友情链接为VO对象");
        List<FriendLinkVO> voList = list.stream()
                .map(friendLink -> {
                    // 转换单个友情链接
                    FriendLinkVO vo = convertToVO(friendLink);
                    log.debug("转换友情链接: id={}, name={}, url={}", 
                        vo.getId(), vo.getName(), vo.getUrl());
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 记录业务完成
        log.info("获取启用友情链接业务逻辑执行完成: count={}", voList.size());
        
        return voList;
    }
    
    /**
     * 根据ID获取友情链接
     * 
     * 业务逻辑：
     * 1. 验证友情链接ID的有效性
     * 2. 从数据库查询指定ID的友情链接
     * 3. 检查友情链接是否存在
     * 4. 转换为VO对象并返回
     * 
     * @param id 友情链接ID
     * @return 友情链接VO对象
     * @throws IllegalArgumentException 当ID无效时抛出
     * @throws ResourceNotFoundException 当友情链接不存在时抛出
     */
    @Override
    public FriendLinkVO getById(Long id) {
        // 记录业务开始
        log.info("执行根据ID获取友情链接业务逻辑: id={}", id);
        
        // 步骤1：验证参数
        // 检查友情链接ID是否有效
        if (id == null || id <= 0) {
            log.warn("友情链接ID无效: id={}", id);
            throw new IllegalArgumentException("友情链接ID无效");
        }
        
        // 步骤2：查询数据库
        // 根据ID查询友情链接
        log.debug("查询友情链接: id={}", id);
        FriendLink friendLink = friendLinkMapper.selectById(id);
        
        // 步骤3：检查查询结果
        // 如果友情链接不存在，抛出异常
        if (friendLink == null) {
            log.warn("友情链接不存在: id={}", id);
            throw new ResourceNotFoundException("友情链接不存在");
        }
        
        // 记录查询成功
        log.info("友情链接查询成功: id={}, name={}, url={}, status={}", 
            friendLink.getId(), friendLink.getName(), friendLink.getUrl(), friendLink.getStatus());
        
        // 步骤4：转换为VO对象
        // 将实体对象转换为VO对象
        log.debug("转换友情链接为VO对象");
        FriendLinkVO vo = convertToVO(friendLink);
        
        // 记录业务完成
        log.info("根据ID获取友情链接业务逻辑执行完成: id={}, name={}", 
            vo.getId(), vo.getName());
        
        return vo;
    }
    
    /**
     * 创建友情链接
     * 
     * 业务逻辑：
     * 1. 验证友情链接数据的完整性
     * 2. 验证URL格式的正确性
     * 3. 检查网站名称是否重复
     * 4. 设置默认值（排序、状态等）
     * 5. 保存友情链接到数据库
     * 
     * @param dto 友情链接数据传输对象
     * @return 创建成功的友情链接ID
     * @throws IllegalArgumentException 当参数验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(FriendLinkDTO dto) {
        // 记录业务开始
        log.info("执行创建友情链接业务逻辑: name={}, url={}, description={}", 
            dto.getName(), dto.getUrl(), dto.getDescription());
        
        // 步骤1：验证参数
        // 检查DTO对象是否为空
        if (dto == null) {
            log.warn("友情链接DTO为空");
            throw new IllegalArgumentException("友情链接数据不能为空");
        }
        
        // 检查必填字段
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            log.warn("友情链接名称为空");
            throw new IllegalArgumentException("友情链接名称不能为空");
        }
        
        if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
            log.warn("友情链接URL为空");
            throw new IllegalArgumentException("友情链接URL不能为空");
        }
        
        // 步骤2：验证URL格式
        // 检查URL格式是否正确
        log.debug("验证URL格式: url={}", dto.getUrl());
        if (!isValidUrl(dto.getUrl())) {
            log.warn("URL格式不正确: url={}", dto.getUrl());
            throw new BusinessException("URL格式不正确");
        }
        
        // 步骤3：检查名称重复
        // 查询数据库中是否存在同名的友情链接
        log.debug("检查友情链接名称是否重复: name={}", dto.getName());
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendLink::getName, dto.getName());
        long count = friendLinkMapper.selectCount(wrapper);
        
        if (count > 0) {
            log.warn("友情链接名称已存在: name={}, count={}", dto.getName(), count);
            throw new BusinessException("网站名称已存在");
        }
        
        // 步骤4：创建实体对象
        // 将DTO转换为实体对象
        log.debug("创建友情链接实体对象");
        FriendLink friendLink = new FriendLink();
        BeanUtils.copyProperties(dto, friendLink);
        
        // 步骤5：设置默认值
        // 设置默认排序值
        if (friendLink.getSortOrder() == null) {
            friendLink.setSortOrder(0);
            log.debug("设置默认排序值: sortOrder=0");
        }
        
        // 设置默认状态（如果需要）
        if (friendLink.getStatus() == null) {
            friendLink.setStatus(1); // 默认启用
            log.debug("设置默认状态: status=1");
        }
        
        // 步骤6：保存到数据库
        // 执行插入操作
        log.debug("保存友情链接到数据库");
        int rows = friendLinkMapper.insert(friendLink);
        
        // 验证插入结果
        if (rows > 0) {
            log.info("友情链接保存成功: id={}, name={}, url={}, rows={}", 
                friendLink.getId(), friendLink.getName(), friendLink.getUrl(), rows);
        } else {
            log.error("友情链接保存失败，影响行数为0: name={}, url={}", 
                friendLink.getName(), friendLink.getUrl());
            throw new RuntimeException("友情链接保存失败");
        }
        
        // 记录业务完成
        log.info("创建友情链接业务逻辑执行完成: id={}, name={}", 
            friendLink.getId(), friendLink.getName());
        
        return friendLink.getId();
    }
    
    /**
     * 更新友情链接
     * 
     * 业务逻辑：
     * 1. 验证友情链接ID和数据的有效性
     * 2. 检查友情链接是否存在
     * 3. 验证URL格式的正确性
     * 4. 检查网站名称是否重复（排除自己）
     * 5. 更新友情链接信息
     * 
     * @param dto 友情链接数据传输对象
     * @throws IllegalArgumentException 当参数验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     * @throws ResourceNotFoundException 当友情链接不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FriendLinkDTO dto) {
        // 记录业务开始
        log.info("执行更新友情链接业务逻辑: id={}, name={}, url={}", 
            dto.getId(), dto.getName(), dto.getUrl());
        
        // 步骤1：验证参数
        // 检查DTO对象是否为空
        if (dto == null) {
            log.warn("友情链接DTO为空");
            throw new IllegalArgumentException("友情链接数据不能为空");
        }
        
        // 检查友情链接ID
        if (dto.getId() == null || dto.getId() <= 0) {
            log.warn("友情链接ID无效: id={}", dto.getId());
            throw new BusinessException("友情链接ID不能为空");
        }
        
        // 检查必填字段
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            log.warn("友情链接名称为空: id={}", dto.getId());
            throw new IllegalArgumentException("友情链接名称不能为空");
        }
        
        if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
            log.warn("友情链接URL为空: id={}", dto.getId());
            throw new IllegalArgumentException("友情链接URL不能为空");
        }
        
        // 步骤2：验证URL格式
        // 检查URL格式是否正确
        log.debug("验证URL格式: id={}, url={}", dto.getId(), dto.getUrl());
        if (!isValidUrl(dto.getUrl())) {
            log.warn("URL格式不正确: id={}, url={}", dto.getId(), dto.getUrl());
            throw new BusinessException("URL格式不正确");
        }
        
        // 步骤3：检查友情链接是否存在
        // 查询现有的友情链接
        log.debug("检查友情链接是否存在: id={}", dto.getId());
        FriendLink existingLink = friendLinkMapper.selectById(dto.getId());
        if (existingLink == null) {
            log.warn("友情链接不存在: id={}", dto.getId());
            throw new ResourceNotFoundException("友情链接不存在");
        }
        
        // 记录更新前的信息
        log.info("更新前友情链接信息: id={}, oldName={}, oldUrl={}, oldStatus={}", 
            existingLink.getId(), existingLink.getName(), existingLink.getUrl(), existingLink.getStatus());
        
        // 步骤4：检查名称重复（排除自己）
        // 查询是否有其他友情链接使用相同名称
        log.debug("检查友情链接名称是否重复: id={}, name={}", dto.getId(), dto.getName());
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendLink::getName, dto.getName())
               .ne(FriendLink::getId, dto.getId());
        long count = friendLinkMapper.selectCount(wrapper);
        
        if (count > 0) {
            log.warn("友情链接名称已存在: id={}, name={}, count={}", 
                dto.getId(), dto.getName(), count);
            throw new BusinessException("网站名称已存在");
        }
        
        // 步骤5：更新友情链接
        // 将DTO转换为实体对象
        log.debug("准备更新友情链接实体对象");
        FriendLink friendLink = new FriendLink();
        BeanUtils.copyProperties(dto, friendLink);
        
        // 执行更新操作
        log.debug("更新友情链接到数据库: id={}", friendLink.getId());
        int rows = friendLinkMapper.updateById(friendLink);
        
        // 验证更新结果
        if (rows > 0) {
            log.info("友情链接更新成功: id={}, name={}, url={}, rows={}", 
                friendLink.getId(), friendLink.getName(), friendLink.getUrl(), rows);
        } else {
            log.error("友情链接更新失败，影响行数为0: id={}, name={}", 
                friendLink.getId(), friendLink.getName());
            throw new RuntimeException("友情链接更新失败");
        }
        
        // 记录业务完成
        log.info("更新友情链接业务逻辑执行完成: id={}, name={}", 
            friendLink.getId(), friendLink.getName());
    }
    
    /**
     * 删除友情链接
     * 
     * 业务逻辑：
     * 1. 验证友情链接ID的有效性
     * 2. 检查友情链接是否存在
     * 3. 记录删除前的友情链接信息
     * 4. 执行删除操作
     * 5. 验证删除结果
     * 
     * @param id 友情链接ID
     * @throws IllegalArgumentException 当ID无效时抛出
     * @throws ResourceNotFoundException 当友情链接不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 记录业务开始
        log.info("执行删除友情链接业务逻辑: id={}", id);
        
        // 步骤1：验证参数
        // 检查友情链接ID是否有效
        if (id == null || id <= 0) {
            log.warn("友情链接ID无效: id={}", id);
            throw new IllegalArgumentException("友情链接ID无效");
        }
        
        // 步骤2：检查友情链接是否存在
        // 先查询友情链接是否存在，记录删除前的信息
        log.debug("检查友情链接是否存在: id={}", id);
        FriendLink friendLink = friendLinkMapper.selectById(id);
        
        if (friendLink == null) {
            log.warn("友情链接不存在: id={}", id);
            throw new ResourceNotFoundException("友情链接不存在");
        }
        
        // 记录删除前的友情链接信息
        log.info("准备删除友情链接: id={}, name={}, url={}, status={}", 
            friendLink.getId(), friendLink.getName(), friendLink.getUrl(), friendLink.getStatus());
        
        // 步骤3：执行删除操作
        // 从数据库中删除友情链接
        log.debug("执行删除操作: id={}", id);
        int rows = friendLinkMapper.deleteById(id);
        
        // 步骤4：验证删除结果
        // 检查删除操作是否成功
        if (rows > 0) {
            log.info("友情链接删除成功: id={}, name={}, rows={}", 
                id, friendLink.getName(), rows);
        } else {
            log.error("友情链接删除失败，影响行数为0: id={}, name={}", 
                id, friendLink.getName());
            throw new RuntimeException("友情链接删除失败");
        }
        
        // 记录业务完成
        log.info("删除友情链接业务逻辑执行完成: id={}", id);
    }
    
    /**
     * 批量删除友情链接
     * 
     * 业务逻辑：
     * 1. 验证ID列表的有效性
     * 2. 检查ID列表是否为空
     * 3. 验证每个ID的有效性
     * 4. 执行批量删除操作
     * 5. 验证删除结果
     * 
     * @param ids 友情链接ID列表
     * @throws IllegalArgumentException 当ID列表无效时抛出
     * @throws BusinessException 当ID列表为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        // 记录业务开始
        log.info("执行批量删除友情链接业务逻辑: ids={}, count={}", 
            ids, ids != null ? ids.size() : 0);
        
        // 步骤1：验证参数
        // 检查ID列表是否为空
        if (ids == null || ids.isEmpty()) {
            log.warn("批量删除友情链接ID列表为空");
            throw new BusinessException("删除ID列表不能为空");
        }
        
        // 步骤2：验证每个ID的有效性
        // 检查ID列表中是否有无效ID
        log.debug("验证ID列表中的每个ID");
        for (Long id : ids) {
            if (id == null || id <= 0) {
                log.warn("批量删除包含无效ID: id={}", id);
                throw new IllegalArgumentException("包含无效的友情链接ID: " + id);
            }
        }
        
        // 步骤3：记录删除前的信息（可选）
        // 查询要删除的友情链接信息用于日志记录
        log.debug("查询要删除的友情链接信息");
        List<FriendLink> toDeleteLinks = friendLinkMapper.selectBatchIds(ids);
        log.info("准备批量删除友情链接: existingCount={}, requestCount={}", 
            toDeleteLinks.size(), ids.size());
        
        // 记录每个要删除的友情链接
        for (FriendLink link : toDeleteLinks) {
            log.debug("准备删除友情链接: id={}, name={}, url={}", 
                link.getId(), link.getName(), link.getUrl());
        }
        
        // 步骤4：执行批量删除操作
        // 使用MyBatis Plus的批量删除功能
        log.debug("执行批量删除操作: count={}", ids.size());
        int rows = friendLinkMapper.deleteBatchIds(ids);
        
        // 步骤5：验证删除结果
        // 检查删除操作是否成功
        if (rows > 0) {
            log.info("友情链接批量删除成功: requestCount={}, deletedCount={}", 
                ids.size(), rows);
        } else {
            log.warn("友情链接批量删除未删除任何记录: requestCount={}, deletedCount={}", 
                ids.size(), rows);
        }
        
        // 记录业务完成
        log.info("批量删除友情链接业务逻辑执行完成: requestCount={}, deletedCount={}", 
            ids.size(), rows);
    }
    
    /**
     * 更新友情链接状态
     * 
     * 业务逻辑：
     * 1. 验证友情链接ID和状态的有效性
     * 2. 检查友情链接是否存在
     * 3. 记录状态变更前的信息
     * 4. 更新友情链接状态
     * 5. 验证更新结果
     * 
     * @param id 友情链接ID
     * @param status 新状态（0-禁用，1-启用）
     * @throws IllegalArgumentException 当参数无效时抛出
     * @throws ResourceNotFoundException 当友情链接不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        // 记录业务开始
        log.info("执行更新友情链接状态业务逻辑: id={}, status={}", id, status);
        
        // 步骤1：验证参数
        // 检查友情链接ID是否有效
        if (id == null || id <= 0) {
            log.warn("友情链接ID无效: id={}", id);
            throw new IllegalArgumentException("友情链接ID无效");
        }
        
        // 检查状态值是否有效
        if (status == null || (status != 0 && status != 1)) {
            log.warn("友情链接状态值无效: id={}, status={}", id, status);
            throw new IllegalArgumentException("状态值无效，只能是0（禁用）或1（启用）");
        }
        
        // 步骤2：检查友情链接是否存在
        // 查询现有的友情链接
        log.debug("检查友情链接是否存在: id={}", id);
        FriendLink friendLink = friendLinkMapper.selectById(id);
        if (friendLink == null) {
            log.warn("友情链接不存在: id={}", id);
            throw new ResourceNotFoundException("友情链接不存在");
        }
        
        // 记录状态变更前的信息
        Integer oldStatus = friendLink.getStatus();
        String oldStatusText = oldStatus == 1 ? "启用" : "禁用";
        String newStatusText = status == 1 ? "启用" : "禁用";
        
        log.info("友情链接状态变更: id={}, name={}, oldStatus={}({}), newStatus={}({})", 
            id, friendLink.getName(), oldStatus, oldStatusText, status, newStatusText);
        
        // 检查状态是否需要变更
        if (Objects.equals(oldStatus, status)) {
            log.info("友情链接状态无需变更: id={}, status={}({})", 
                id, status, newStatusText);
            return;
        }
        
        // 步骤3：更新友情链接状态
        // 设置新状态
        friendLink.setStatus(status);
        
        // 执行更新操作
        log.debug("更新友情链接状态到数据库: id={}, status={}", id, status);
        int rows = friendLinkMapper.updateById(friendLink);
        
        // 步骤4：验证更新结果
        // 检查更新操作是否成功
        if (rows > 0) {
            log.info("友情链接状态更新成功: id={}, name={}, status={}({}), rows={}", 
                id, friendLink.getName(), status, newStatusText, rows);
        } else {
            log.error("友情链接状态更新失败，影响行数为0: id={}, status={}", id, status);
            throw new RuntimeException("友情链接状态更新失败");
        }
        
        // 记录业务完成
        log.info("更新友情链接状态业务逻辑执行完成: id={}, status={}({})", 
            id, status, newStatusText);
    }
    
    /**
     * 转换为VO对象
     * 
     * 将友情链接实体对象转换为VO对象，用于前端展示
     * 
     * @param friendLink 友情链接实体对象
     * @return 友情链接VO对象
     */
    private FriendLinkVO convertToVO(FriendLink friendLink) {
        // 检查输入参数
        if (friendLink == null) {
            log.warn("友情链接实体对象为空，无法转换为VO");
            return null;
        }
        
        // 创建VO对象并复制属性
        FriendLinkVO vo = new FriendLinkVO();
        BeanUtils.copyProperties(friendLink, vo);
        
        // 记录转换过程（调试级别）
        log.debug("友情链接实体转换为VO: id={}, name={}, url={}", 
            vo.getId(), vo.getName(), vo.getUrl());
        
        return vo;
    }
    
    /**
     * 验证URL格式
     * 
     * 检查URL是否符合标准格式，必须以http://或https://开头
     * 
     * @param url 待验证的URL字符串
     * @return true表示格式正确，false表示格式错误
     */
    private boolean isValidUrl(String url) {
        // 检查URL是否为空
        if (!StringUtils.hasText(url)) {
            log.debug("URL为空或空白字符串");
            return false;
        }
        
        // 去除首尾空白字符
        String trimmedUrl = url.trim();
        
        // 检查URL是否以http://或https://开头
        boolean isValid = trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://");
        
        // 记录验证结果
        log.debug("URL格式验证: url={}, isValid={}", url, isValid);
        
        return isValid;
    }
}
