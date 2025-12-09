package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.dto.DownloadLinkDTO;
import com.resource.platform.entity.DownloadLink;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.mapper.DownloadLinkMapper;
import com.resource.platform.service.DownloadLinkService;
import com.resource.platform.vo.DownloadLinkVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 下载链接服务实现类
 * 
 * 功能说明：
 * - 实现下载链接的核心业务逻辑
 * - 处理下载链接的增删改查操作
 * - 管理下载链接的排序和状态
 * - 提供下载链接的数据转换功能
 * - 确保下载链接数据的一致性和有效性
 * 
 * 主要职责：
 * - 下载链接数据的持久化管理
 * - 下载链接的业务规则验证
 * - 下载链接的格式化和转换
 * - 下载链接的排序和过滤
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class DownloadLinkServiceImpl implements DownloadLinkService {

    @Autowired
    private DownloadLinkMapper downloadLinkMapper;

    /**
     * 获取指定资源的下载链接列表
     * 
     * 业务逻辑：
     * 1. 验证资源ID的有效性
     * 2. 构建查询条件，按资源ID过滤
     * 3. 按排序字段升序排列
     * 4. 将实体对象转换为VO对象
     * 5. 返回排序后的下载链接列表
     * 
     * @param resourceId 资源ID
     * @return 下载链接VO列表
     */
    @Override
    public List<DownloadLinkVO> getDownloadLinks(Long resourceId) {
        // 记录业务开始
        log.info("执行获取下载链接业务逻辑: resourceId={}", resourceId);
        
        // 步骤1：验证参数
        // 检查资源ID是否有效
        if (resourceId == null || resourceId <= 0) {
            log.warn("资源ID无效: resourceId={}", resourceId);
            throw new IllegalArgumentException("资源ID无效");
        }
        
        // 步骤2：构建查询条件
        // 创建Lambda查询条件，按资源ID过滤并排序
        log.debug("构建查询条件: resourceId={}", resourceId);
        LambdaQueryWrapper<DownloadLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLink::getResourceId, resourceId);    // 按资源ID过滤
        wrapper.orderByAsc(DownloadLink::getSortOrder);         // 按排序字段升序
        
        // 步骤3：查询数据库
        // 执行查询获取下载链接列表
        log.debug("查询下载链接: resourceId={}", resourceId);
        List<DownloadLink> links = downloadLinkMapper.selectList(wrapper);
        log.info("查询到下载链接: resourceId={}, count={}", resourceId, links.size());
        
        // 步骤4：转换为VO对象
        // 使用Stream API将实体对象转换为VO对象
        log.debug("转换下载链接为VO对象");
        List<DownloadLinkVO> linkVOs = links.stream().map(link -> {
            // 创建VO对象并复制属性
            DownloadLinkVO vo = new DownloadLinkVO();
            BeanUtils.copyProperties(link, vo);
            
            // 记录转换的链接信息
            log.debug("转换下载链接: id={}, title={}, type={}", 
                link.getId(), link.getTitle(), link.getLinkType());
            
            return vo;
        }).collect(Collectors.toList());
        
        // 记录业务完成
        log.info("获取下载链接业务逻辑执行完成: resourceId={}, resultCount={}", 
            resourceId, linkVOs.size());
        
        return linkVOs;
    }

    /**
     * 创建下载链接
     * 
     * 业务逻辑：
     * 1. 验证下载链接数据的完整性
     * 2. 检查下载链接URL的有效性
     * 3. 设置默认的排序和状态值
     * 4. 保存下载链接到数据库
     * 5. 转换为VO对象并返回
     * 
     * @param dto 下载链接数据传输对象
     * @return 创建成功的下载链接VO对象
     * @throws IllegalArgumentException 当参数验证失败时抛出
     */
    @Override
    @Transactional
    public DownloadLinkVO createDownloadLink(DownloadLinkDTO dto) {
        // 记录业务开始
        log.info("执行创建下载链接业务逻辑: resourceId={}, title={}, type={}", 
            dto.getResourceId(), dto.getTitle(), dto.getLinkType());
        
        // 步骤1：验证参数
        // 检查DTO对象是否为空
        if (dto == null) {
            log.warn("下载链接DTO为空");
            throw new IllegalArgumentException("下载链接数据不能为空");
        }
        
        // 检查必填字段
        if (dto.getResourceId() == null || dto.getResourceId() <= 0) {
            log.warn("资源ID无效: resourceId={}", dto.getResourceId());
            throw new IllegalArgumentException("资源ID无效");
        }
        
        if (dto.getDownloadUrl() == null || dto.getDownloadUrl().trim().isEmpty()) {
            log.warn("下载链接URL为空: resourceId={}", dto.getResourceId());
            throw new IllegalArgumentException("下载链接URL不能为空");
        }
        
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            log.warn("下载链接标题为空: resourceId={}", dto.getResourceId());
            throw new IllegalArgumentException("下载链接标题不能为空");
        }
        
        // 步骤2：创建实体对象
        // 将DTO转换为实体对象
        log.debug("创建下载链接实体对象");
        DownloadLink link = new DownloadLink();
        BeanUtils.copyProperties(dto, link);
        
        // 步骤3：设置默认值
        // 设置默认的排序值
        if (link.getSortOrder() == null) {
            link.setSortOrder(0);
            log.debug("设置默认排序值: sortOrder=0");
        }
        
        // 设置默认状态（如果需要）
        // 这里可以根据业务需求设置默认状态
        log.debug("下载链接数据准备完成: resourceId={}, title={}, url={}", 
            link.getResourceId(), link.getTitle(), link.getDownloadUrl());
        
        // 步骤4：保存到数据库
        // 执行插入操作
        log.debug("保存下载链接到数据库");
        int rows = downloadLinkMapper.insert(link);
        
        // 验证插入结果
        if (rows > 0) {
            log.info("下载链接保存成功: id={}, resourceId={}, title={}, rows={}", 
                link.getId(), link.getResourceId(), link.getTitle(), rows);
        } else {
            log.error("下载链接保存失败，影响行数为0: resourceId={}, title={}", 
                link.getResourceId(), link.getTitle());
            throw new RuntimeException("下载链接保存失败");
        }
        
        // 步骤5：转换为VO对象
        // 将保存后的实体对象转换为VO对象
        log.debug("转换下载链接为VO对象");
        DownloadLinkVO vo = new DownloadLinkVO();
        BeanUtils.copyProperties(link, vo);
        
        // 记录业务完成
        log.info("创建下载链接业务逻辑执行完成: id={}, resourceId={}, title={}", 
            vo.getId(), vo.getResourceId(), vo.getTitle());
        
        return vo;
    }

    /**
     * 删除下载链接
     * 
     * 业务逻辑：
     * 1. 验证下载链接ID的有效性
     * 2. 检查下载链接是否存在
     * 3. 记录删除前的链接信息
     * 4. 执行删除操作
     * 5. 验证删除结果
     * 
     * @param id 下载链接ID
     * @throws IllegalArgumentException 当ID无效时抛出
     * @throws ResourceNotFoundException 当下载链接不存在时抛出
     */
    @Override
    @Transactional
    public void deleteDownloadLink(Long id) {
        // 记录业务开始
        log.info("执行删除下载链接业务逻辑: id={}", id);
        
        // 步骤1：验证参数
        // 检查下载链接ID是否有效
        if (id == null || id <= 0) {
            log.warn("下载链接ID无效: id={}", id);
            throw new IllegalArgumentException("下载链接ID无效");
        }
        
        // 步骤2：检查下载链接是否存在
        // 先查询下载链接是否存在，记录删除前的信息
        log.debug("检查下载链接是否存在: id={}", id);
        DownloadLink existingLink = downloadLinkMapper.selectById(id);
        
        if (existingLink == null) {
            log.warn("下载链接不存在: id={}", id);
            throw new ResourceNotFoundException("下载链接不存在: " + id);
        }
        
        // 记录删除前的链接信息
        log.info("准备删除下载链接: id={}, resourceId={}, title={}, url={}", 
            existingLink.getId(), existingLink.getResourceId(), 
            existingLink.getTitle(), existingLink.getDownloadUrl());
        
        // 步骤3：执行删除操作
        // 从数据库中删除下载链接
        log.debug("执行删除操作: id={}", id);
        int rows = downloadLinkMapper.deleteById(id);
        
        // 步骤4：验证删除结果
        // 检查删除操作是否成功
        if (rows > 0) {
            log.info("下载链接删除成功: id={}, resourceId={}, title={}, rows={}", 
                id, existingLink.getResourceId(), existingLink.getTitle(), rows);
        } else {
            log.error("下载链接删除失败，影响行数为0: id={}", id);
            throw new RuntimeException("下载链接删除失败");
        }
        
        // 记录业务完成
        log.info("删除下载链接业务逻辑执行完成: id={}", id);
    }
}
