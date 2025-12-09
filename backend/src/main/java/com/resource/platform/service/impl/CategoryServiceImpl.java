package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.dto.CategoryQueryDTO;
import com.resource.platform.entity.Category;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.exception.ValidationException;
import com.resource.platform.mapper.CategoryMapper;
import com.resource.platform.service.CategoryService;
import com.resource.platform.vo.CategoryStatisticsVO;
import com.resource.platform.vo.CategoryTreeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 * 
 * 功能说明：
 * - 实现分类的核心业务逻辑
 * - 处理分类的CRUD操作
 * - 管理分类的树形结构
 * - 处理分类的层级关系
 * - 提供分类统计和导出功能
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private com.resource.platform.mapper.ResourceMapper resourceMapper;

    /**
     * 获取分类统计信息
     * 
     * 业务逻辑：
     * 1. 统计总分类数量
     * 2. 统计一级分类数量
     * 3. 统计二级分类数量
     * 4. 统计启用的分类数量
     * 5. 计算禁用的分类数量
     * 6. 返回统计结果
     * 
     * @return 分类统计信息对象
     */
    @Override
    public CategoryStatisticsVO getStatistics() {
        // 记录统计开始
        log.info("开始获取分类统计信息");
        
        // 创建统计结果对象
        CategoryStatisticsVO statistics = new CategoryStatisticsVO();
        
        // 步骤1：统计总分类数
        // 查询所有分类的数量
        Long totalCategories = categoryMapper.selectCount(null);
        statistics.setTotalCategories(totalCategories);
        log.debug("总分类数: {}", totalCategories);
        
        // 步骤2：统计一级分类数
        // 查询层级为1的分类数量
        Long level1Categories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getLevel, 1)
        );
        statistics.setLevel1Categories(level1Categories);
        log.debug("一级分类数: {}", level1Categories);
        
        // 步骤3：统计二级分类数
        // 查询层级为2的分类数量
        Long level2Categories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getLevel, 2)
        );
        statistics.setLevel2Categories(level2Categories);
        log.debug("二级分类数: {}", level2Categories);
        
        // 步骤4：统计启用分类数
        // 查询状态为1（启用）的分类数量
        Long activeCategories = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1)
        );
        statistics.setActiveCategories(activeCategories);
        log.debug("启用分类数: {}", activeCategories);
        
        // 步骤5：计算禁用分类数
        // 总数减去启用数即为禁用数
        statistics.setInactiveCategories(totalCategories - activeCategories);
        log.debug("禁用分类数: {}", totalCategories - activeCategories);
        
        // 记录统计成功
        log.info("获取分类统计信息成功: total={}, level1={}, level2={}, active={}, inactive={}", 
            totalCategories, level1Categories, level2Categories, 
            activeCategories, totalCategories - activeCategories);
        
        return statistics;
    }

    /**
     * 获取分类树形结构
     * 
     * 业务逻辑：
     * 1. 查询所有分类数据
     * 2. 按排序字段和ID排序
     * 3. 递归构建树形结构
     * 4. 统计每个分类的资源数量
     * 5. 返回树形结构
     * 
     * @return 分类树形结构列表
     */
    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        // 记录构建开始
        log.info("开始构建分类树");
        
        // 步骤1：查询所有分类
        // 按排序字段和ID升序排列，确保树形结构的顺序正确
        List<Category> allCategories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId)
        );
        
        log.debug("查询到分类数量: {}", allCategories.size());
        
        // 步骤2：构建树形结构
        // 从根节点（parentId=0）开始递归构建
        List<CategoryTreeVO> tree = buildTree(allCategories, 0L);
        
        // 记录构建成功
        log.info("构建分类树成功: rootCount={}, totalCount={}", tree.size(), allCategories.size());
        
        return tree;
    }

    /**
     * 递归构建分类树
     * 
     * 业务逻辑：
     * 1. 遍历所有分类
     * 2. 找出指定父ID的子分类
     * 3. 递归查找每个子分类的子节点
     * 4. 统计每个分类的资源数量
     * 5. 返回树形节点列表
     * 
     * @param categories 所有分类列表
     * @param parentId 父分类ID
     * @return 树形节点列表
     */
    private List<CategoryTreeVO> buildTree(List<Category> categories, Long parentId) {
        // 创建树形节点列表
        List<CategoryTreeVO> tree = new ArrayList<>();
        
        // 步骤1：遍历所有分类
        for (Category category : categories) {
            // 步骤2：找出当前父ID的子分类
            if (category.getParentId().equals(parentId)) {
                // 创建树形节点
                CategoryTreeVO node = new CategoryTreeVO();
                BeanUtils.copyProperties(category, node);
                
                // 步骤3：递归查找子分类
                // 以当前分类的ID作为父ID，继续查找下一层子分类
                List<CategoryTreeVO> children = buildTree(categories, category.getId());
                node.setChildren(children);
                
                // 步骤4：查询该分类下的资源数量
                // 统计有多少资源属于这个分类
                Long resourceCount = resourceMapper.selectCount(
                    new LambdaQueryWrapper<com.resource.platform.entity.Resource>()
                        .eq(com.resource.platform.entity.Resource::getCategoryId, category.getId())
                );
                node.setResourceCount(resourceCount);
                
                // 添加到树形列表
                tree.add(node);
                
                log.debug("构建分类节点: id={}, name={}, parentId={}, childrenCount={}, resourceCount={}", 
                    category.getId(), category.getName(), parentId, children.size(), resourceCount);
            }
        }
        
        return tree;
    }

    /**
     * 分页查询分类列表
     * 
     * 业务逻辑：
     * 1. 根据关键词模糊搜索分类名称
     * 2. 根据父分类ID筛选
     * 3. 根据层级筛选
     * 4. 根据状态筛选
     * 5. 按排序字段和ID排序
     * 6. 执行分页查询
     * 
     * @param queryDTO 查询条件对象
     * @return 分类分页结果
     */
    @Override
    public Page<Category> queryCategories(CategoryQueryDTO queryDTO) {
        // 记录查询开始
        log.info("开始分页查询分类: page={}, pageSize={}, keyword={}, parentId={}, level={}, status={}", 
            queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getKeyword(),
            queryDTO.getParentId(), queryDTO.getLevel(), queryDTO.getStatus());
        
        // 创建分页对象
        Page<Category> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        
        // 步骤1：关键词搜索
        // 在分类名称中模糊匹配关键词
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            log.debug("添加关键词搜索条件: keyword={}", queryDTO.getKeyword());
            wrapper.like(Category::getName, queryDTO.getKeyword());
        }
        
        // 步骤2：父分类筛选
        // 根据父分类ID精确匹配
        if (queryDTO.getParentId() != null) {
            log.debug("添加父分类筛选条件: parentId={}", queryDTO.getParentId());
            wrapper.eq(Category::getParentId, queryDTO.getParentId());
        }
        
        // 步骤3：层级筛选
        // 根据层级精确匹配（1=一级分类，2=二级分类）
        if (queryDTO.getLevel() != null) {
            log.debug("添加层级筛选条件: level={}", queryDTO.getLevel());
            wrapper.eq(Category::getLevel, queryDTO.getLevel());
        }
        
        // 步骤4：状态筛选
        // 根据状态精确匹配（0=禁用，1=启用）
        if (queryDTO.getStatus() != null) {
            log.debug("添加状态筛选条件: status={}", queryDTO.getStatus());
            wrapper.eq(Category::getStatus, queryDTO.getStatus());
        }
        
        // 步骤5：排序设置
        // 先按排序字段升序，再按ID升序
        wrapper.orderByAsc(Category::getSortOrder)
               .orderByAsc(Category::getId);
        
        // 步骤6：执行分页查询
        Page<Category> result = categoryMapper.selectPage(page, wrapper);
        
        // 记录查询成功
        log.info("分页查询分类成功: total={}, records={}", result.getTotal(), result.getRecords().size());
        
        return result;
    }

    /**
     * 根据ID获取分类详情
     * 
     * 业务逻辑：
     * 1. 根据ID查询分类
     * 2. 验证分类是否存在
     * 3. 返回分类信息
     * 
     * @param id 分类ID
     * @return 分类对象
     * @throws ResourceNotFoundException 当分类不存在时抛出
     */
    @Override
    public Category getCategoryById(Long id) {
        // 记录查询开始
        log.info("开始查询分类详情: categoryId={}", id);
        
        // 步骤1：根据ID查询分类
        Category category = categoryMapper.selectById(id);
        
        // 步骤2：验证分类是否存在
        if (category == null) {
            log.warn("分类不存在: categoryId={}", id);
            throw new ResourceNotFoundException("分类", id);
        }
        
        // 记录查询成功
        log.info("查询分类详情成功: categoryId={}, name={}, level={}", 
            id, category.getName(), category.getLevel());
        
        return category;
    }

    /**
     * 创建分类
     * 
     * 业务逻辑：
     * 1. 验证同级分类名称唯一性
     * 2. 验证父分类是否存在
     * 3. 设置分类层级
     * 4. 设置默认值（排序、状态）
     * 5. 保存分类到数据库
     * 6. 返回创建成功的分类
     * 
     * @param category 分类对象
     * @return 创建成功的分类对象
     * @throws ValidationException 当数据验证失败时抛出
     */
    @Override
    @Transactional
    public Category createCategory(Category category) {
        // 记录创建开始
        log.info("开始创建分类: name={}, parentId={}", category.getName(), category.getParentId());
        
        // 步骤1：验证分类名称唯一性
        // 在同一父分类下，分类名称不能重复
        log.debug("验证分类名称唯一性: name={}, parentId={}", category.getName(), category.getParentId());
        Long count = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getName, category.getName())
                .eq(Category::getParentId, category.getParentId())
        );
        
        if (count > 0) {
            log.warn("同级分类名称已存在: name={}, parentId={}, count={}", 
                category.getName(), category.getParentId(), count);
            throw new ValidationException("同级分类名称已存在");
        }
        
        // 步骤2：设置层级
        // 如果没有父分类或父分类ID为0，则为一级分类
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
            log.debug("设置为一级分类: level=1");
        } else {
            // 如果有父分类，验证父分类是否存在
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                log.warn("父分类不存在: parentId={}", category.getParentId());
                throw new ValidationException("父分类不存在");
            }
            // 子分类的层级 = 父分类层级 + 1
            category.setLevel(parent.getLevel() + 1);
            log.debug("设置为子分类: parentId={}, parentLevel={}, level={}", 
                category.getParentId(), parent.getLevel(), category.getLevel());
        }
        
        // 步骤3：设置默认排序
        // 如果未指定排序值，默认为0
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
            log.debug("设置默认排序: sortOrder=0");
        }
        
        // 步骤4：设置默认状态
        // 如果未指定状态，默认为1（启用）
        if (category.getStatus() == null) {
            category.setStatus(1);
            log.debug("设置默认状态: status=1");
        }
        
        // 步骤5：保存到数据库
        log.debug("保存分类到数据库: name={}, level={}", category.getName(), category.getLevel());
        categoryMapper.insert(category);
        
        // 记录创建成功
        log.info("创建分类成功: categoryId={}, name={}, level={}", 
            category.getId(), category.getName(), category.getLevel());
        
        return category;
    }

    /**
     * 更新分类
     * 
     * 业务逻辑：
     * 1. 验证分类是否存在
     * 2. 验证父分类是否存在
     * 3. 重新计算分类层级
     * 4. 验证同级分类名称唯一性（排除自身）
     * 5. 更新分类信息到数据库
     * 6. 返回更新后的分类
     * 
     * @param category 分类对象
     * @return 更新后的分类对象
     * @throws ResourceNotFoundException 当分类不存在时抛出
     * @throws ValidationException 当数据验证失败时抛出
     */
    @Override
    @Transactional
    public Category updateCategory(Category category) {
        // 记录更新开始
        log.info("开始更新分类: categoryId={}, name={}", category.getId(), category.getName());
        
        // 步骤1：验证分类是否存在
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            log.warn("分类不存在: categoryId={}", category.getId());
            throw new ResourceNotFoundException("分类", category.getId());
        }
        
        log.debug("原分类信息: name={}, parentId={}, level={}", 
            existing.getName(), existing.getParentId(), existing.getLevel());
        
        // 步骤2：设置层级
        // 如果没有父分类或父分类ID为0，则为一级分类
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
            log.debug("更新为一级分类: level=1");
        } else {
            // 如果有父分类，验证父分类是否存在
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                log.warn("父分类不存在: parentId={}", category.getParentId());
                throw new ValidationException("父分类不存在");
            }
            // 子分类的层级 = 父分类层级 + 1
            category.setLevel(parent.getLevel() + 1);
            log.debug("更新为子分类: parentId={}, parentLevel={}, level={}", 
                category.getParentId(), parent.getLevel(), category.getLevel());
        }
        
        // 步骤3：验证分类名称唯一性
        // 在同一父分类下，分类名称不能重复（排除自身）
        log.debug("验证分类名称唯一性: name={}, parentId={}, excludeId={}", 
            category.getName(), category.getParentId(), category.getId());
        Long count = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getName, category.getName())
                .eq(Category::getParentId, category.getParentId())
                .ne(Category::getId, category.getId())
        );
        
        if (count > 0) {
            log.warn("同级分类名称已存在: name={}, parentId={}, count={}", 
                category.getName(), category.getParentId(), count);
            throw new ValidationException("同级分类名称已存在");
        }
        
        // 步骤4：更新到数据库
        log.debug("更新分类到数据库: categoryId={}, name={}, level={}", 
            category.getId(), category.getName(), category.getLevel());
        int rows = categoryMapper.updateById(category);
        
        // 记录更新成功
        log.info("更新分类成功: categoryId={}, name={}, level={}, affectedRows={}", 
            category.getId(), category.getName(), category.getLevel(), rows);
        
        return category;
    }

    /**
     * 删除分类
     * 
     * 业务逻辑：
     * 1. 验证分类是否存在
     * 2. 检查是否有子分类
     * 3. 检查是否有资源使用该分类
     * 4. 删除分类
     * 
     * @param id 分类ID
     * @throws ResourceNotFoundException 当分类不存在时抛出
     * @throws ValidationException 当分类下有子分类时抛出
     */
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // 记录删除开始
        log.info("开始删除分类: categoryId={}", id);
        
        // 步骤1：验证分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            log.warn("分类不存在: categoryId={}", id);
            throw new ResourceNotFoundException("分类", id);
        }
        
        log.debug("分类信息: name={}, level={}", category.getName(), category.getLevel());
        
        // 步骤2：检查是否有子分类
        // 如果有子分类，不允许删除父分类
        log.debug("检查子分类: categoryId={}", id);
        Long childCount = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getParentId, id)
        );
        
        if (childCount > 0) {
            log.warn("分类下有子分类，无法删除: categoryId={}, childCount={}", id, childCount);
            throw new ValidationException("该分类下有子分类，无法删除");
        }
        
        // 步骤3：检查是否有资源使用该分类
        // TODO: 可以在这里添加资源检查逻辑
        
        // 步骤4：删除分类
        log.debug("删除分类: categoryId={}", id);
        int rows = categoryMapper.deleteById(id);
        
        // 记录删除成功
        log.info("删除分类成功: categoryId={}, name={}, affectedRows={}", 
            id, category.getName(), rows);
    }

    /**
     * 批量删除分类
     * 
     * 业务逻辑：
     * 1. 遍历分类ID列表
     * 2. 逐个调用删除方法
     * 3. 捕获并记录删除失败的分类
     * 4. 继续处理剩余分类
     * 
     * @param ids 分类ID列表
     */
    @Override
    @Transactional
    public void deleteCategories(List<Long> ids) {
        // 记录批量删除开始
        log.info("开始批量删除分类: count={}, ids={}", ids.size(), ids);
        
        // 统计删除结果
        int successCount = 0;
        int failCount = 0;
        
        // 步骤1：遍历分类ID列表
        for (Long id : ids) {
            try {
                // 步骤2：调用单个删除方法
                deleteCategory(id);
                successCount++;
            } catch (Exception e) {
                // 步骤3：记录删除失败
                // 不抛出异常，继续处理剩余分类
                log.error("批量删除分类失败: categoryId={}, error={}", id, e.getMessage(), e);
                failCount++;
            }
        }
        
        // 记录批量删除结果
        log.info("批量删除分类完成: total={}, success={}, fail={}", 
            ids.size(), successCount, failCount);
    }

    /**
     * 更新分类排序
     * 
     * 业务逻辑：
     * 1. 验证分类是否存在
     * 2. 更新排序值
     * 3. 保存到数据库
     * 
     * @param id 分类ID
     * @param sortOrder 新的排序值
     * @throws ResourceNotFoundException 当分类不存在时抛出
     */
    @Override
    @Transactional
    public void updateSortOrder(Long id, Integer sortOrder) {
        // 记录更新开始
        log.info("开始更新分类排序: categoryId={}, sortOrder={}", id, sortOrder);
        
        // 步骤1：验证分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            log.warn("分类不存在: categoryId={}", id);
            throw new ResourceNotFoundException("分类", id);
        }
        
        // 记录原排序值
        Integer oldSortOrder = category.getSortOrder();
        log.debug("原排序值: oldSortOrder={}, newSortOrder={}", oldSortOrder, sortOrder);
        
        // 步骤2：更新排序值
        category.setSortOrder(sortOrder);
        
        // 步骤3：保存到数据库
        int rows = categoryMapper.updateById(category);
        
        // 记录更新成功
        log.info("更新分类排序成功: categoryId={}, oldSortOrder={}, newSortOrder={}, affectedRows={}", 
            id, oldSortOrder, sortOrder, rows);
    }

    /**
     * 导出分类数据
     * 
     * 业务逻辑：
     * 1. 查询所有分类数据
     * 2. 生成CSV格式内容
     * 3. 保存到临时文件
     * 4. 返回文件路径
     * 
     * @return 导出文件的路径
     * @throws RuntimeException 当导出失败时抛出
     */
    @Override
    public String exportCategories() {
        // 记录导出开始
        log.info("开始导出分类数据");
        
        // 步骤1：查询所有分类数据
        // 按排序字段和ID升序排列
        log.debug("查询所有分类数据");
        List<Category> categories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId)
        );
        
        log.debug("查询到分类数量: {}", categories.size());
        
        // 步骤2：生成CSV内容
        // 构建CSV格式的字符串
        StringBuilder csv = new StringBuilder();
        
        // 添加CSV表头
        csv.append("ID,名称,父分类ID,层级,图标,描述,排序,状态,创建时间\n");
        
        // 遍历所有分类，生成CSV行
        for (Category category : categories) {
            // 拼接每个字段，空值用空字符串代替
            csv.append(category.getId()).append(",")
               .append(category.getName()).append(",")
               .append(category.getParentId()).append(",")
               .append(category.getLevel()).append(",")
               .append(category.getIcon() != null ? category.getIcon() : "").append(",")
               .append(category.getDescription() != null ? category.getDescription() : "").append(",")
               .append(category.getSortOrder()).append(",")
               .append(category.getStatus()).append(",")
               .append(category.getCreateTime()).append("\n");
        }
        
        log.debug("CSV内容生成完成: size={} bytes", csv.length());
        
        // 步骤3：保存到临时文件
        try {
            // 生成唯一的文件名，使用时间戳避免冲突
            String fileName = "categories_export_" + System.currentTimeMillis() + ".csv";
            String filePath = "/tmp/" + fileName;
            
            // 创建文件对象
            File file = new File(filePath);
            
            // 写入文件，使用UTF-8编码
            log.debug("保存CSV文件: filePath={}", filePath);
            FileUtils.writeStringToFile(file, csv.toString(), StandardCharsets.UTF_8);
            
            // 记录导出成功
            log.info("导出分类成功: filePath={}, count={}, size={} bytes", 
                filePath, categories.size(), csv.length());
            
            return filePath;
            
        } catch (IOException e) {
            // 记录导出失败
            log.error("导出分类失败: error={}", e.getMessage(), e);
            throw new RuntimeException("导出分类失败: " + e.getMessage());
        }
    }
}
