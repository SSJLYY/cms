package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.resource.platform.entity.LinkType;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.mapper.LinkTypeMapper;
import com.resource.platform.service.LinkTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 链接类型服务实现类
 * 
 * 功能说明：
 * - 实现链接类型的核心业务逻辑
 * - 处理链接类型的增删改查操作
 * - 管理链接类型的状态和排序
 * - 提供链接类型的数据验证功能
 * - 确保链接类型数据的一致性和有效性
 * 
 * 主要职责：
 * - 链接类型数据的持久化管理
 * - 链接类型的业务规则验证
 * - 链接类型的格式化和转换
 * - 链接类型的排序和过滤
 * - 关联数据的完整性检查
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class LinkTypeServiceImpl extends ServiceImpl<LinkTypeMapper, LinkType> implements LinkTypeService {
    
    /**
     * 获取所有启用的链接类型
     * 
     * 业务逻辑：
     * 1. 构建查询条件，只查询启用状态的链接类型
     * 2. 按排序字段升序排列
     * 3. 返回启用的链接类型列表
     * 4. 供前台页面展示使用
     * 
     * @return 启用的链接类型列表
     */
    @Override
    public List<LinkType> listEnabled() {
        // 记录业务开始
        log.info("执行获取启用链接类型业务逻辑");
        
        // 步骤1：构建查询条件
        // 只查询状态为启用（1）的链接类型
        log.debug("构建启用链接类型查询条件");
        LambdaQueryWrapper<LinkType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LinkType::getStatus, 1)                    // 只查询启用状态
               .orderByAsc(LinkType::getSortOrder);           // 按排序字段升序
        
        // 步骤2：执行查询
        // 查询所有启用的链接类型
        log.debug("查询启用的链接类型");
        List<LinkType> list = list(wrapper);
        log.info("查询到启用链接类型: count={}", list.size());
        
        // 步骤3：记录查询结果
        // 记录每个启用的链接类型信息
        for (LinkType linkType : list) {
            log.debug("启用链接类型: id={}, name={}, icon={}", 
                linkType.getId(), linkType.getName(), linkType.getIcon());
        }
        
        // 记录业务完成
        log.info("获取启用链接类型业务逻辑执行完成: count={}", list.size());
        
        return list;
    }
    
    /**
     * 获取所有链接类型
     * 
     * 业务逻辑：
     * 1. 构建查询条件，查询所有链接类型（包括启用和禁用）
     * 2. 按排序字段升序排列
     * 3. 返回所有链接类型列表
     * 4. 供管理后台使用
     * 
     * @return 所有链接类型列表
     */
    @Override
    public List<LinkType> listAll() {
        // 记录业务开始
        log.info("执行获取所有链接类型业务逻辑");
        
        // 步骤1：构建查询条件
        // 查询所有链接类型，按排序字段升序
        log.debug("构建所有链接类型查询条件");
        LambdaQueryWrapper<LinkType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(LinkType::getSortOrder);           // 按排序字段升序
        
        // 步骤2：执行查询
        // 查询所有链接类型
        log.debug("查询所有链接类型");
        List<LinkType> list = list(wrapper);
        log.info("查询到所有链接类型: count={}", list.size());
        
        // 步骤3：记录查询结果
        // 统计启用和禁用的数量
        long enabledCount = list.stream().filter(linkType -> linkType.getStatus() == 1).count();
        long disabledCount = list.size() - enabledCount;
        log.info("链接类型统计: total={}, enabled={}, disabled={}", 
            list.size(), enabledCount, disabledCount);
        
        // 记录每个链接类型信息
        for (LinkType linkType : list) {
            log.debug("链接类型: id={}, name={}, status={}, sortOrder={}", 
                linkType.getId(), linkType.getName(), linkType.getStatus(), linkType.getSortOrder());
        }
        
        // 记录业务完成
        log.info("获取所有链接类型业务逻辑执行完成: count={}", list.size());
        
        return list;
    }
    
    /**
     * 保存链接类型
     * 
     * 业务逻辑：
     * 1. 验证链接类型数据的完整性
     * 2. 检查类型名称是否重复
     * 3. 设置默认值（排序、状态等）
     * 4. 保存链接类型到数据库
     * 5. 记录创建操作日志
     * 
     * @param entity 链接类型实体对象
     * @return 保存是否成功
     * @throws IllegalArgumentException 当参数验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     */
    @Override
    @Transactional
    public boolean save(LinkType entity) {
        // 记录业务开始
        log.info("执行保存链接类型业务逻辑: name={}, icon={}", 
            entity.getName(), entity.getIcon());
        
        // 步骤1：验证参数
        // 检查链接类型数据的完整性
        if (entity == null) {
            log.warn("链接类型实体对象为空");
            throw new IllegalArgumentException("链接类型数据不能为空");
        }
        
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            log.warn("链接类型名称为空");
            throw new IllegalArgumentException("链接类型名称不能为空");
        }
        
        // 步骤2：检查名称重复
        // 查询数据库中是否存在同名的链接类型
        log.debug("检查链接类型名称是否重复: name={}", entity.getName());
        LambdaQueryWrapper<LinkType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LinkType::getName, entity.getName());
        long count = count(wrapper);
        
        if (count > 0) {
            log.warn("链接类型名称已存在: name={}, count={}", entity.getName(), count);
            throw new BusinessException("链接类型名称已存在");
        }
        
        // 步骤3：设置默认值
        // 设置默认排序值
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
            log.debug("设置默认排序值: sortOrder=0");
        }
        
        // 设置默认状态
        if (entity.getStatus() == null) {
            entity.setStatus(1); // 默认启用
            log.debug("设置默认状态: status=1");
        }
        
        // 步骤4：保存到数据库
        // 调用父类的save方法
        log.debug("保存链接类型到数据库");
        boolean result = super.save(entity);
        
        // 验证保存结果
        if (result) {
            log.info("链接类型保存成功: id={}, name={}, icon={}", 
                entity.getId(), entity.getName(), entity.getIcon());
        } else {
            log.error("链接类型保存失败: name={}", entity.getName());
            throw new RuntimeException("链接类型保存失败");
        }
        
        // 记录业务完成
        log.info("保存链接类型业务逻辑执行完成: id={}, name={}", 
            entity.getId(), entity.getName());
        
        return result;
    }
    
    /**
     * 根据ID更新链接类型
     * 
     * 业务逻辑：
     * 1. 验证链接类型ID和数据的有效性
     * 2. 检查链接类型是否存在
     * 3. 验证类型名称是否重复（排除自己）
     * 4. 更新链接类型信息
     * 5. 记录更新操作日志
     * 
     * @param entity 链接类型实体对象
     * @return 更新是否成功
     * @throws IllegalArgumentException 当参数验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     * @throws ResourceNotFoundException 当链接类型不存在时抛出
     */
    @Override
    @Transactional
    public boolean updateById(LinkType entity) {
        // 记录业务开始
        log.info("执行更新链接类型业务逻辑: id={}, name={}", 
            entity.getId(), entity.getName());
        
        // 步骤1：验证参数
        // 检查链接类型数据的完整性
        if (entity == null) {
            log.warn("链接类型实体对象为空");
            throw new IllegalArgumentException("链接类型数据不能为空");
        }
        
        if (entity.getId() == null || entity.getId() <= 0) {
            log.warn("链接类型ID无效: id={}", entity.getId());
            throw new IllegalArgumentException("链接类型ID无效");
        }
        
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            log.warn("链接类型名称为空: id={}", entity.getId());
            throw new IllegalArgumentException("链接类型名称不能为空");
        }
        
        // 步骤2：检查链接类型是否存在
        // 查询现有的链接类型
        log.debug("检查链接类型是否存在: id={}", entity.getId());
        LinkType existingType = getById(entity.getId());
        if (existingType == null) {
            log.warn("链接类型不存在: id={}", entity.getId());
            throw new ResourceNotFoundException("链接类型不存在");
        }
        
        // 记录更新前的信息
        log.info("更新前链接类型信息: id={}, oldName={}, oldStatus={}", 
            existingType.getId(), existingType.getName(), existingType.getStatus());
        
        // 步骤3：检查名称重复（排除自己）
        // 查询是否有其他链接类型使用相同名称
        log.debug("检查链接类型名称是否重复: id={}, name={}", entity.getId(), entity.getName());
        LambdaQueryWrapper<LinkType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LinkType::getName, entity.getName())
               .ne(LinkType::getId, entity.getId());
        long count = count(wrapper);
        
        if (count > 0) {
            log.warn("链接类型名称已存在: id={}, name={}, count={}", 
                entity.getId(), entity.getName(), count);
            throw new BusinessException("链接类型名称已存在");
        }
        
        // 步骤4：更新链接类型
        // 调用父类的updateById方法
        log.debug("更新链接类型到数据库: id={}", entity.getId());
        boolean result = super.updateById(entity);
        
        // 验证更新结果
        if (result) {
            log.info("链接类型更新成功: id={}, name={}, icon={}", 
                entity.getId(), entity.getName(), entity.getIcon());
        } else {
            log.error("链接类型更新失败: id={}, name={}", 
                entity.getId(), entity.getName());
            throw new RuntimeException("链接类型更新失败");
        }
        
        // 记录业务完成
        log.info("更新链接类型业务逻辑执行完成: id={}, name={}", 
            entity.getId(), entity.getName());
        
        return result;
    }
    
    /**
     * 根据ID删除链接类型
     * 
     * 业务逻辑：
     * 1. 验证链接类型ID的有效性
     * 2. 检查链接类型是否存在
     * 3. 检查是否有关联的下载链接
     * 4. 执行删除操作
     * 5. 记录删除操作日志
     * 
     * @param id 链接类型ID
     * @return 删除是否成功
     * @throws IllegalArgumentException 当ID无效时抛出
     * @throws ResourceNotFoundException 当链接类型不存在时抛出
     * @throws BusinessException 当存在关联数据时抛出
     */
    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        // 记录业务开始
        log.info("执行删除链接类型业务逻辑: id={}", id);
        
        // 步骤1：验证参数
        // 检查链接类型ID是否有效
        if (id == null) {
            log.warn("链接类型ID为空");
            throw new IllegalArgumentException("链接类型ID不能为空");
        }
        
        Long linkTypeId = Long.valueOf(id.toString());
        if (linkTypeId <= 0) {
            log.warn("链接类型ID无效: id={}", linkTypeId);
            throw new IllegalArgumentException("链接类型ID无效");
        }
        
        // 步骤2：检查链接类型是否存在
        // 先查询链接类型是否存在，记录删除前的信息
        log.debug("检查链接类型是否存在: id={}", linkTypeId);
        LinkType existingType = getById(linkTypeId);
        
        if (existingType == null) {
            log.warn("链接类型不存在: id={}", linkTypeId);
            throw new ResourceNotFoundException("链接类型不存在");
        }
        
        // 记录删除前的链接类型信息
        log.info("准备删除链接类型: id={}, name={}, status={}", 
            existingType.getId(), existingType.getName(), existingType.getStatus());
        
        // 步骤3：检查关联数据
        // TODO: 检查是否有下载链接使用此类型
        // 这里可以添加关联数据检查逻辑
        log.debug("检查链接类型关联数据: id={}", linkTypeId);
        
        // 步骤4：执行删除操作
        // 调用父类的removeById方法
        log.debug("执行删除操作: id={}", linkTypeId);
        boolean result = super.removeById(linkTypeId);
        
        // 步骤5：验证删除结果
        // 检查删除操作是否成功
        if (result) {
            log.info("链接类型删除成功: id={}, name={}", 
                linkTypeId, existingType.getName());
        } else {
            log.error("链接类型删除失败: id={}, name={}", 
                linkTypeId, existingType.getName());
            throw new RuntimeException("链接类型删除失败");
        }
        
        // 记录业务完成
        log.info("删除链接类型业务逻辑执行完成: id={}", linkTypeId);
        
        return result;
    }
    
    /**
     * 批量删除链接类型
     * 
     * 业务逻辑：
     * 1. 验证ID列表的有效性
     * 2. 检查ID列表是否为空
     * 3. 验证每个ID的有效性
     * 4. 检查每个链接类型是否存在关联数据
     * 5. 执行批量删除操作
     * 6. 记录批量删除结果
     * 
     * @param idList 链接类型ID列表
     * @return 删除是否成功
     * @throws IllegalArgumentException 当ID列表无效时抛出
     * @throws BusinessException 当ID列表为空或存在关联数据时抛出
     */
    @Transactional
    public boolean batchRemoveByIds(Collection<? extends Serializable> idList) {
        // 记录业务开始
        log.info("执行批量删除链接类型业务逻辑: ids={}, count={}", 
            idList, idList != null ? idList.size() : 0);
        
        // 步骤1：验证参数
        // 检查ID列表是否为空
        if (idList == null || idList.isEmpty()) {
            log.warn("批量删除链接类型ID列表为空");
            throw new BusinessException("删除ID列表不能为空");
        }
        
        // 步骤2：验证每个ID的有效性
        // 检查ID列表中是否有无效ID
        log.debug("验证ID列表中的每个ID");
        for (Serializable id : idList) {
            if (id == null) {
                log.warn("批量删除包含空ID");
                throw new IllegalArgumentException("包含无效的链接类型ID: null");
            }
            
            Long linkTypeId = Long.valueOf(id.toString());
            if (linkTypeId <= 0) {
                log.warn("批量删除包含无效ID: id={}", linkTypeId);
                throw new IllegalArgumentException("包含无效的链接类型ID: " + linkTypeId);
            }
        }
        
        // 步骤3：记录删除前的信息（可选）
        // 查询要删除的链接类型信息用于日志记录
        log.debug("查询要删除的链接类型信息");
        List<LinkType> toDeleteTypes = listByIds(idList);
        log.info("准备批量删除链接类型: existingCount={}, requestCount={}", 
            toDeleteTypes.size(), idList.size());
        
        // 记录每个要删除的链接类型
        for (LinkType linkType : toDeleteTypes) {
            log.debug("准备删除链接类型: id={}, name={}, status={}", 
                linkType.getId(), linkType.getName(), linkType.getStatus());
        }
        
        // 步骤4：检查关联数据
        // TODO: 检查每个链接类型是否有关联的下载链接
        log.debug("检查链接类型关联数据");
        
        // 步骤5：执行批量删除操作
        // 调用父类的removeByIds方法
        log.debug("执行批量删除操作: count={}", idList.size());
        boolean result = super.removeByIds(idList);
        
        // 步骤6：验证删除结果
        // 检查删除操作是否成功
        if (result) {
            log.info("链接类型批量删除成功: requestCount={}", idList.size());
        } else {
            log.warn("链接类型批量删除失败: requestCount={}", idList.size());
        }
        
        // 记录业务完成
        log.info("批量删除链接类型业务逻辑执行完成: requestCount={}", idList.size());
        
        return result;
    }
}
