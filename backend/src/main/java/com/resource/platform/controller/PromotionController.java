package com.resource.platform.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.PageResult;
import com.resource.platform.common.Result;
import com.resource.platform.dto.AdvertisementDTO;
import com.resource.platform.entity.Advertisement;
import com.resource.platform.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 推广管理控制器
 * 
 * 功能说明：
 * - 提供推广广告的分页查询接口
 * - 提供推广广告的增删改查管理功能
 * - 支持广告状态和排序的管理
 * - 提供广告点击统计功能
 * - 管理广告位置和展示规则
 * - 记录推广活动的管理操作日志
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "推广管理")
@RestController
@RequestMapping("/api/promotion")
public class PromotionController {
    
    @Autowired
    private PromotionService promotionService;
    
    /**
     * 获取广告列表
     * 
     * 业务逻辑：
     * 1. 验证分页查询参数的有效性
     * 2. 构建查询条件（位置过滤）
     * 3. 执行分页查询并排序
     * 4. 返回广告列表结果
     * 
     * @param page 页码，默认为1
     * @param pageSize 页大小，默认为20
     * @param position 广告位置，可选
     * @return 广告列表分页结果
     */
    @Operation(summary = "获取广告列表")
    @GetMapping("/list")
    public Result<PageResult<Advertisement>> getAdvertisementList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String position) {
        // 记录请求开始
        log.info("开始获取广告列表: page={}, pageSize={}, position={}", page, pageSize, position);
        
        try {
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
            
            // 步骤2：调用Service层执行分页查询
            PageResult<Advertisement> result = promotionService.getAdvertisementList(page, pageSize, position);
            
            // 记录成功结果
            log.info("广告列表获取成功: total={}, records={}, page={}, pageSize={}", 
                result.getTotal(), result.getRecords().size(), page, pageSize);
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常
            log.error("广告列表获取失败: page={}, pageSize={}, position={}, error={}", 
                page, pageSize, position, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取广告详情
     * 
     * 业务逻辑：
     * 1. 验证广告ID的有效性
     * 2. 查询指定ID的广告信息
     * 3. 检查广告是否存在
     * 4. 返回广告详细信息
     * 
     * @param id 广告ID
     * @return 广告详细信息
     * @throws ResourceNotFoundException 当广告不存在时抛出
     */
    @Operation(summary = "获取广告详情")
    @GetMapping("/{id}")
    public Result<Advertisement> getAdvertisementById(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始获取广告详情: id={}", id);
        
        try {
            // 步骤1：验证参数
            // 检查广告ID是否有效
            if (id == null || id <= 0) {
                log.warn("广告ID无效: id={}", id);
                throw new IllegalArgumentException("广告ID无效");
            }
            
            // 步骤2：调用Service层查询广告详情
            Advertisement advertisement = promotionService.getAdvertisementById(id);
            
            // 记录成功结果
            log.info("广告详情获取成功: id={}, title={}, position={}", 
                advertisement.getId(), advertisement.getTitle(), advertisement.getPosition());
            
            return Result.success(advertisement);
            
        } catch (Exception e) {
            // 记录异常
            log.error("广告详情获取失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 创建广告
     * 
     * 业务逻辑：
     * 1. 验证广告数据的完整性
     * 2. 检查广告标题是否重复
     * 3. 验证广告位置和时间范围
     * 4. 保存广告到数据库
     * 5. 记录创建操作日志
     * 
     * @param dto 广告数据传输对象
     * @return 操作结果
     * @throws ValidationException 当数据验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     */
    @Operation(summary = "创建广告")
    @PostMapping
    @OperationLog(module = "推广管理", type = "创建", description = "创建广告")
    public Result<Void> createAdvertisement(@Validated @RequestBody AdvertisementDTO dto) {
        // 记录请求开始
        log.info("开始创建广告: title={}, position={}, type={}", 
            dto.getTitle(), dto.getPosition(), dto.getType());
        
        try {
            // 步骤1：验证参数
            // @Validated注解已经进行了基本验证，这里进行业务验证
            if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                log.warn("广告标题为空");
                throw new IllegalArgumentException("广告标题不能为空");
            }
            
            if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
                log.warn("广告位置为空");
                throw new IllegalArgumentException("广告位置不能为空");
            }
            
            // 步骤2：调用Service层创建广告
            promotionService.createAdvertisement(dto);
            
            // 记录成功结果
            log.info("广告创建成功: title={}, position={}", dto.getTitle(), dto.getPosition());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("广告创建失败: title={}, position={}, error={}", 
                dto.getTitle(), dto.getPosition(), e.getMessage(), e);
            throw e;
        }
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
     * @return 操作结果
     * @throws ValidationException 当数据验证失败时抛出
     * @throws BusinessException 当业务规则验证失败时抛出
     * @throws ResourceNotFoundException 当广告不存在时抛出
     */
    @Operation(summary = "更新广告")
    @PutMapping("/{id}")
    @OperationLog(module = "推广管理", type = "更新", description = "更新广告")
    public Result<Void> updateAdvertisement(@PathVariable Long id, @Validated @RequestBody AdvertisementDTO dto) {
        // 记录请求开始
        log.info("开始更新广告: id={}, title={}, position={}", id, dto.getTitle(), dto.getPosition());
        
        try {
            // 步骤1：验证参数
            // 检查广告ID是否有效
            if (id == null || id <= 0) {
                log.warn("广告ID无效: id={}", id);
                throw new IllegalArgumentException("广告ID无效");
            }
            
            if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                log.warn("广告标题为空: id={}", id);
                throw new IllegalArgumentException("广告标题不能为空");
            }
            
            if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
                log.warn("广告位置为空: id={}", id);
                throw new IllegalArgumentException("广告位置不能为空");
            }
            
            // 步骤2：调用Service层更新广告
            promotionService.updateAdvertisement(id, dto);
            
            // 记录成功结果
            log.info("广告更新成功: id={}, title={}, position={}", id, dto.getTitle(), dto.getPosition());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("广告更新失败: id={}, title={}, error={}", 
                id, dto.getTitle(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 删除广告
     * 
     * 业务逻辑：
     * 1. 验证广告ID的有效性
     * 2. 检查广告是否存在
     * 3. 检查广告是否正在使用中
     * 4. 执行删除操作
     * 5. 记录删除操作日志
     * 
     * @param id 广告ID
     * @return 操作结果
     * @throws ResourceNotFoundException 当广告不存在时抛出
     * @throws BusinessException 当广告正在使用中时抛出
     */
    @Operation(summary = "删除广告")
    @DeleteMapping("/{id}")
    @OperationLog(module = "推广管理", type = "删除", description = "删除广告")
    public Result<Void> deleteAdvertisement(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始删除广告: id={}", id);
        
        try {
            // 步骤1：验证参数
            // 检查广告ID是否有效
            if (id == null || id <= 0) {
                log.warn("广告ID无效: id={}", id);
                throw new IllegalArgumentException("广告ID无效");
            }
            
            // 步骤2：调用Service层删除广告
            // Service层会检查广告是否存在以及是否可以删除
            promotionService.deleteAdvertisement(id);
            
            // 记录成功结果
            log.info("广告删除成功: id={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("广告删除失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 更新广告状态
     * 
     * 业务逻辑：
     * 1. 验证广告ID和状态的有效性
     * 2. 检查广告是否存在
     * 3. 更新广告状态
     * 4. 记录状态变更日志
     * 
     * @param id 广告ID
     * @param status 新状态（0-禁用，1-启用）
     * @return 操作结果
     * @throws ResourceNotFoundException 当广告不存在时抛出
     */
    @Operation(summary = "更新广告状态")
    @PutMapping("/{id}/status")
    @OperationLog(module = "推广管理", type = "更新状态", description = "更新广告状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        // 记录请求开始
        log.info("开始更新广告状态: id={}, status={}", id, status);
        
        try {
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
            
            // 步骤2：调用Service层更新广告状态
            promotionService.updateStatus(id, status);
            
            // 记录成功结果
            String statusText = status == 1 ? "启用" : "禁用";
            log.info("广告状态更新成功: id={}, status={}({})", id, status, statusText);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("广告状态更新失败: id={}, status={}, error={}", 
                id, status, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 更新广告排序
     * 
     * 业务逻辑：
     * 1. 验证广告ID和排序值的有效性
     * 2. 检查广告是否存在
     * 3. 更新广告排序值
     * 4. 记录排序变更日志
     * 
     * @param id 广告ID
     * @param sortOrder 新排序值
     * @return 操作结果
     * @throws ResourceNotFoundException 当广告不存在时抛出
     */
    @Operation(summary = "更新广告排序")
    @PutMapping("/{id}/sort")
    @OperationLog(module = "推广管理", type = "更新排序", description = "更新广告排序")
    public Result<Void> updateSortOrder(@PathVariable Long id, @RequestParam Integer sortOrder) {
        // 记录请求开始
        log.info("开始更新广告排序: id={}, sortOrder={}", id, sortOrder);
        
        try {
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
            
            // 步骤2：调用Service层更新广告排序
            promotionService.updateSortOrder(id, sortOrder);
            
            // 记录成功结果
            log.info("广告排序更新成功: id={}, sortOrder={}", id, sortOrder);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("广告排序更新失败: id={}, sortOrder={}, error={}", 
                id, sortOrder, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 记录广告点击
     * 
     * 业务逻辑：
     * 1. 验证广告ID的有效性
     * 2. 检查广告是否存在且启用
     * 3. 增加广告点击计数
     * 4. 记录点击统计日志
     * 
     * @param id 广告ID
     * @return 操作结果
     * @throws ResourceNotFoundException 当广告不存在时抛出
     */
    @Operation(summary = "记录点击")
    @PostMapping("/{id}/click")
    public Result<Void> recordClick(@PathVariable Long id) {
        // 记录请求开始
        log.info("开始记录广告点击: id={}", id);
        
        try {
            // 步骤1：验证参数
            // 检查广告ID是否有效
            if (id == null || id <= 0) {
                log.warn("广告ID无效: id={}", id);
                throw new IllegalArgumentException("广告ID无效");
            }
            
            // 步骤2：调用Service层记录点击
            // Service层会检查广告是否存在且启用
            promotionService.recordClick(id);
            
            // 记录成功结果
            log.info("广告点击记录成功: id={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("广告点击记录失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取广告位置选项
     * 
     * 业务逻辑：
     * 1. 获取系统支持的所有广告位置
     * 2. 返回位置选项列表
     * 3. 供前端下拉选择使用
     * 
     * @return 广告位置选项列表
     */
    @Operation(summary = "获取广告位置选项")
    @GetMapping("/positions")
    public Result<List<Map<String, String>>> getPositionOptions() {
        // 记录请求开始
        log.info("开始获取广告位置选项");
        
        try {
            // 调用Service层获取位置选项
            List<Map<String, String>> options = promotionService.getPositionOptions();
            
            // 记录成功结果
            log.info("广告位置选项获取成功: count={}", options.size());
            
            return Result.success(options);
            
        } catch (Exception e) {
            // 记录异常
            log.error("广告位置选项获取失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取用户端广告
     * 
     * 业务逻辑：
     * 1. 验证广告位置参数的有效性
     * 2. 查询指定位置的启用广告
     * 3. 过滤时间范围内的有效广告
     * 4. 按排序字段排序返回
     * 
     * @param position 广告位置
     * @return 有效的广告列表
     */
    @Operation(summary = "获取用户端广告")
    @GetMapping("/active")
    public Result<List<Advertisement>> getActiveAdvertisements(@RequestParam String position) {
        // 记录请求开始
        log.info("开始获取用户端广告: position={}", position);
        
        try {
            // 步骤1：验证参数
            // 检查广告位置是否有效
            if (position == null || position.trim().isEmpty()) {
                log.warn("广告位置为空");
                throw new IllegalArgumentException("广告位置不能为空");
            }
            
            // 步骤2：调用Service层获取有效广告
            List<Advertisement> advertisements = promotionService.getActiveAdvertisements(position);
            
            // 记录成功结果
            log.info("用户端广告获取成功: position={}, count={}", position, advertisements.size());
            
            return Result.success(advertisements);
            
        } catch (Exception e) {
            // 记录异常
            log.error("用户端广告获取失败: position={}, error={}", position, e.getMessage(), e);
            throw e;
        }
    }
}
