package com.resource.platform.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.FriendLinkDTO;
import com.resource.platform.dto.FriendLinkQueryDTO;
import com.resource.platform.service.FriendLinkService;
import com.resource.platform.vo.FriendLinkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 友情链接管理控制器
 * 
 * 功能说明：
 * - 提供友情链接的分页查询接口
 * - 提供友情链接的增删改查管理功能
 * - 支持友情链接状态的启用和禁用
 * - 提供批量删除友情链接功能
 * - 区分前台展示和后台管理接口
 * - 记录友情链接的管理操作日志
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/friendlinks")
@RequiredArgsConstructor
@Tag(name = "友情链接管理", description = "友情链接管理相关接口")
public class FriendLinkController {
    
    private final FriendLinkService friendLinkService;
    
    /**
     * 分页查询友情链接
     * 
     * 业务逻辑：
     * 1. 验证分页查询参数的有效性
     * 2. 构建查询条件（名称模糊查询、状态过滤）
     * 3. 执行分页查询并排序
     * 4. 转换为VO对象并返回
     * 
     * @param queryDTO 查询条件DTO
     * @return 分页查询结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询友情链接")
    public Result<PageResult<FriendLinkVO>> queryPage(FriendLinkQueryDTO queryDTO) {
        // 记录请求开始
        log.info("开始分页查询友情链接: page={}, pageSize={}, name={}, status={}", 
            queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getName(), queryDTO.getStatus());
        
        try {
            // 步骤1：验证分页参数
            // 检查分页参数的合理性
            if (queryDTO.getPage() == null || queryDTO.getPage() < 1) {
                queryDTO.setPage(1);
                log.debug("修正页码参数: page=1");
            }
            
            if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
                queryDTO.setPageSize(10);
                log.debug("修正页大小参数: pageSize=10");
            }
            
            // 步骤2：调用Service层执行分页查询
            PageResult<FriendLinkVO> result = friendLinkService.queryPage(queryDTO);
            
            // 记录成功结果
            log.info("友情链接分页查询成功: total={}, records={}, page={}, pageSize={}", 
                result.getTotal(), result.getRecords().size(), 
                queryDTO.getPage(), queryDTO.getPageSize());
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常
            log.error("友情链接分页查询失败: queryDTO={}, error={}", 
                queryDTO, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取所有启用的友情链接
     * 
     * 业务逻辑：
     * 1. 查询状态为启用的友情链接
     * 2. 按排序字段和创建时间排序
     * 3. 转换为VO对象列表
     * 4. 供前台页面展示使用
     * 
     * @return 启用的友情链接列表
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的友情链接")
    public Result<List<FriendLinkVO>> listEnabled() {
        // 记录请求开始
        log.info("开始获取启用的友情链接列表");
        
        try {
            // 调用Service层查询启用的友情链接
            List<FriendLinkVO> list = friendLinkService.listEnabled();
            
            // 记录成功结果
            log.info("启用友情链接列表获取成功: count={}", list.size());
            
            return Result.success(list);
            
        } catch (Exception e) {
            // 记录异常
            log.error("启用友情链接列表获取失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 根据ID获取友情链接
     * 
     * 业务逻辑：
     * 1. 验证友情链接ID的有效性
     * 2. 查询指定ID的友情链接
     * 3. 检查友情链接是否存在
     * 4. 转换为VO对象并返回
     * 
     * @param id 友情链接ID
     * @return 友情链接详细信息
     * @throws ResourceNotFoundException 当友情链接不存在时抛出
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取友情链接")
    public Result<FriendLinkVO> getById(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始根据ID获取友情链接: id={}", id);
        
        try {
            // 步骤1：验证参数
            // 检查友情链接ID是否有效
            if (id == null || id <= 0) {
                log.warn("友情链接ID无效: id={}", id);
                throw new IllegalArgumentException("友情链接ID无效");
            }
            
            // 步骤2：调用Service层查询友情链接
            FriendLinkVO vo = friendLinkService.getById(id);
            
            // 记录成功结果
            log.info("友情链接获取成功: id={}, name={}, url={}", 
                vo.getId(), vo.getName(), vo.getUrl());
            
            return Result.success(vo);
            
        } catch (Exception e) {
            // 记录异常
            log.error("友情链接获取失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 创建友情链接
     * 
     * 业务逻辑：
     * 1. 验证友情链接数据的完整性
     * 2. 检查网站名称是否重复
     * 3. 验证URL格式的正确性
     * 4. 设置默认排序和状态
     * 5. 保存友情链接到数据库
     * 
     * @param dto 友情链接数据传输对象
     * @return 创建成功的友情链接ID
     * @throws ValidationException 当数据验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     */
    @PostMapping
    @OperationLog(module = "友情链接管理", type = "创建")
    @Operation(summary = "创建友情链接")
    public Result<Long> create(@Validated @RequestBody FriendLinkDTO dto) {
        // 记录请求开始
        log.info("开始创建友情链接: name={}, url={}, description={}", 
            dto.getName(), dto.getUrl(), dto.getDescription());
        
        try {
            // 步骤1：验证参数
            // @Validated注解已经进行了基本验证，这里进行业务验证
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                log.warn("友情链接名称为空");
                throw new IllegalArgumentException("友情链接名称不能为空");
            }
            
            if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
                log.warn("友情链接URL为空");
                throw new IllegalArgumentException("友情链接URL不能为空");
            }
            
            // 步骤2：调用Service层创建友情链接
            Long id = friendLinkService.create(dto);
            
            // 记录成功结果
            log.info("友情链接创建成功: id={}, name={}, url={}", 
                id, dto.getName(), dto.getUrl());
            
            return Result.success(id);
            
        } catch (Exception e) {
            // 记录异常
            log.error("友情链接创建失败: name={}, url={}, error={}", 
                dto.getName(), dto.getUrl(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 更新友情链接
     * 
     * 业务逻辑：
     * 1. 验证友情链接ID和数据的有效性
     * 2. 检查友情链接是否存在
     * 3. 验证网站名称是否重复（排除自己）
     * 4. 验证URL格式的正确性
     * 5. 更新友情链接信息
     * 
     * @param dto 友情链接数据传输对象
     * @return 操作结果
     * @throws ValidationException 当数据验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     * @throws ResourceNotFoundException 当友情链接不存在时抛出
     */
    @PutMapping
    @OperationLog(module = "友情链接管理", type = "更新")
    @Operation(summary = "更新友情链接")
    public Result<Void> update(@Validated @RequestBody FriendLinkDTO dto) {
        // 记录请求开始
        log.info("开始更新友情链接: id={}, name={}, url={}", 
            dto.getId(), dto.getName(), dto.getUrl());
        
        try {
            // 步骤1：验证参数
            // 检查友情链接ID是否有效
            if (dto.getId() == null || dto.getId() <= 0) {
                log.warn("友情链接ID无效: id={}", dto.getId());
                throw new IllegalArgumentException("友情链接ID无效");
            }
            
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                log.warn("友情链接名称为空: id={}", dto.getId());
                throw new IllegalArgumentException("友情链接名称不能为空");
            }
            
            if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
                log.warn("友情链接URL为空: id={}", dto.getId());
                throw new IllegalArgumentException("友情链接URL不能为空");
            }
            
            // 步骤2：调用Service层更新友情链接
            friendLinkService.update(dto);
            
            // 记录成功结果
            log.info("友情链接更新成功: id={}, name={}, url={}", 
                dto.getId(), dto.getName(), dto.getUrl());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("友情链接更新失败: id={}, name={}, error={}", 
                dto.getId(), dto.getName(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 删除友情链接
     * 
     * 业务逻辑：
     * 1. 验证友情链接ID的有效性
     * 2. 检查友情链接是否存在
     * 3. 执行删除操作
     * 4. 记录删除操作日志
     * 
     * @param id 友情链接ID
     * @return 操作结果
     * @throws ResourceNotFoundException 当友情链接不存在时抛出
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "友情链接管理", type = "删除")
    @Operation(summary = "删除友情链接")
    public Result<Void> delete(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始删除友情链接: id={}", id);
        
        try {
            // 步骤1：验证参数
            // 检查友情链接ID是否有效
            if (id == null || id <= 0) {
                log.warn("友情链接ID无效: id={}", id);
                throw new IllegalArgumentException("友情链接ID无效");
            }
            
            // 步骤2：调用Service层删除友情链接
            friendLinkService.delete(id);
            
            // 记录成功结果
            log.info("友情链接删除成功: id={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("友情链接删除失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 批量删除友情链接
     * 
     * 业务逻辑：
     * 1. 验证ID列表的有效性
     * 2. 检查ID列表是否为空
     * 3. 执行批量删除操作
     * 4. 记录批量删除结果
     * 
     * @param ids 友情链接ID列表
     * @return 操作结果
     * @throws BusinessException 当ID列表为空时抛出
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "友情链接管理", type = "批量删除")
    @Operation(summary = "批量删除友情链接")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        // 记录请求开始
        log.info("开始批量删除友情链接: ids={}, count={}", ids, ids != null ? ids.size() : 0);
        
        try {
            // 步骤1：验证参数
            // 检查ID列表是否为空
            if (ids == null || ids.isEmpty()) {
                log.warn("批量删除友情链接ID列表为空");
                throw new IllegalArgumentException("删除ID列表不能为空");
            }
            
            // 检查ID列表中是否有无效ID
            for (Long id : ids) {
                if (id == null || id <= 0) {
                    log.warn("批量删除包含无效ID: id={}", id);
                    throw new IllegalArgumentException("包含无效的友情链接ID: " + id);
                }
            }
            
            // 步骤2：调用Service层批量删除友情链接
            friendLinkService.batchDelete(ids);
            
            // 记录成功结果
            log.info("友情链接批量删除成功: count={}", ids.size());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("友情链接批量删除失败: ids={}, error={}", 
                ids, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 更新友情链接状态
     * 
     * 业务逻辑：
     * 1. 验证友情链接ID和状态的有效性
     * 2. 检查友情链接是否存在
     * 3. 更新友情链接状态
     * 4. 记录状态变更日志
     * 
     * @param id 友情链接ID
     * @param status 新状态（0-禁用，1-启用）
     * @return 操作结果
     * @throws ResourceNotFoundException 当友情链接不存在时抛出
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "友情链接管理", type = "更新状态")
    @Operation(summary = "更新友情链接状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        // 记录请求开始
        log.info("开始更新友情链接状态: id={}, status={}", id, status);
        
        try {
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
            
            // 步骤2：调用Service层更新友情链接状态
            friendLinkService.updateStatus(id, status);
            
            // 记录成功结果
            String statusText = status == 1 ? "启用" : "禁用";
            log.info("友情链接状态更新成功: id={}, status={}({})", id, status, statusText);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("友情链接状态更新失败: id={}, status={}, error={}", 
                id, status, e.getMessage(), e);
            throw e;
        }
    }
}
