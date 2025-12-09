package com.resource.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.Result;
import com.resource.platform.dto.ImageQueryDTO;
import com.resource.platform.service.ImageService;
import com.resource.platform.vo.ImageStatisticsVO;
import com.resource.platform.vo.ImageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 图片管理控制器
 * 
 * 功能说明：
 * - 提供图片上传接口（单张和批量）
 * - 提供图片查询和分页接口
 * - 提供图片删除接口（单张和批量）
 * - 提供图片使用情况检查接口
 * - 提供图片统计信息接口
 * - 处理文件上传的验证和存储
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
@Tag(name = "图片管理", description = "图片上传、查询、删除等接口")
public class ImageController {

    @Autowired
    private ImageService imageService;

    /**
     * 获取图片统计信息
     * 
     * 业务逻辑：
     * 1. 统计总图片数量（未删除）
     * 2. 统计已使用和未使用的图片数量
     * 3. 统计总存储大小
     * 4. 统计今日上传数量
     * 5. 返回统计结果
     * 
     * @return 图片统计信息对象
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取图片统计信息")
    public Result<ImageStatisticsVO> getStatistics() {
        // 记录请求开始
        log.info("开始获取图片统计信息");
        
        try {
            // 调用服务层获取统计信息
            ImageStatisticsVO statistics = imageService.getStatistics();
            
            // 记录获取成功
            log.info("获取图片统计信息成功: total={}, used={}, unused={}, totalSize={}, todayUploads={}", 
                statistics.getTotalImages(), statistics.getUsedImages(), 
                statistics.getUnusedImages(), statistics.getTotalSize(), statistics.getTodayUploads());
            
            return Result.success(statistics);
            
        } catch (Exception e) {
            // 记录获取失败
            log.error("获取图片统计信息失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 上传单张图片
     * 
     * 业务逻辑：
     * 1. 验证文件格式和大小
     * 2. 获取图片尺寸信息
     * 3. 上传原图到存储服务
     * 4. 生成并上传缩略图
     * 5. 保存图片信息到数据库
     * 6. 返回图片信息
     * 
     * @param file 上传的图片文件
     * @param uploaderId 上传者ID（可选）
     * @return 上传成功的图片信息
     * @throws FileUploadException 当文件验证失败或上传失败时抛出
     */
    @PostMapping("/upload")
    @Operation(summary = "上传单张图片")
    @OperationLog(module = "图片管理", type = "上传", description = "上传图片")
    public Result<ImageVO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        
        // 记录上传请求开始
        log.info("开始处理图片上传请求: filename={}, size={} bytes, contentType={}, uploaderId={}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType(), uploaderId);
        
        try {
            // 调用服务层上传图片
            ImageVO imageVO = imageService.uploadImage(file, uploaderId);
            
            // 记录上传成功
            log.info("图片上传成功: imageId={}, filename={}, fileUrl={}", 
                imageVO.getId(), imageVO.getFileName(), imageVO.getFileUrl());
            
            return Result.success(imageVO);
            
        } catch (Exception e) {
            // 记录上传失败
            log.error("图片上传失败: filename={}, size={}, error={}", 
                file.getOriginalFilename(), file.getSize(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 批量上传图片
     * 
     * 业务逻辑：
     * 1. 遍历文件列表
     * 2. 逐个调用单张上传方法
     * 3. 收集上传成功的图片
     * 4. 记录上传失败的文件
     * 5. 返回上传结果列表
     * 
     * @param files 上传的图片文件列表
     * @param uploaderId 上传者ID（可选）
     * @return 上传成功的图片信息列表
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传图片")
    @OperationLog(module = "图片管理", type = "批量上传", description = "批量上传图片")
    public Result<List<ImageVO>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        
        // 记录批量上传请求开始
        log.info("开始处理批量图片上传请求: fileCount={}, uploaderId={}", files.size(), uploaderId);
        
        // 记录每个文件的基本信息
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            log.debug("文件[{}]: filename={}, size={} bytes", i, file.getOriginalFilename(), file.getSize());
        }
        
        try {
            // 调用服务层批量上传图片
            List<ImageVO> imageVOs = imageService.uploadImages(files, uploaderId);
            
            // 记录批量上传成功
            log.info("批量图片上传完成: total={}, success={}", files.size(), imageVOs.size());
            
            return Result.success(imageVOs);
            
        } catch (Exception e) {
            // 记录批量上传失败
            log.error("批量图片上传失败: fileCount={}, error={}", files.size(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 查询图片列表
     * 
     * 业务逻辑：
     * 1. 接收查询条件（关键词、文件类型、存储类型、使用状态等）
     * 2. 执行分页查询（只查询未删除的图片）
     * 3. 按创建时间倒序排列
     * 4. 转换为VO对象列表
     * 5. 返回分页结果
     * 
     * @param queryDTO 查询条件对象
     * @return 图片分页结果
     */
    @PostMapping("/query")
    @Operation(summary = "查询图片列表")
    public Result<Page<ImageVO>> queryImages(@RequestBody ImageQueryDTO queryDTO) {
        // 记录查询请求开始
        log.info("开始查询图片列表: page={}, pageSize={}, keyword={}, fileType={}, isUsed={}", 
            queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getKeyword(), 
            queryDTO.getFileType(), queryDTO.getIsUsed());
        
        try {
            // 调用服务层执行分页查询
            Page<ImageVO> page = imageService.queryImages(queryDTO);
            
            // 记录查询成功
            log.info("查询图片列表成功: total={}, records={}", page.getTotal(), page.getRecords().size());
            
            return Result.success(page);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("查询图片列表失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取图片详情
     * 
     * 业务逻辑：
     * 1. 根据ID查询图片信息
     * 2. 验证图片是否存在
     * 3. 转换为VO对象
     * 4. 返回图片详情
     * 
     * @param id 图片ID
     * @return 图片详情对象
     * @throws ResourceNotFoundException 当图片不存在时抛出
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取图片详情")
    public Result<ImageVO> getImageById(@PathVariable Long id) {
        // 记录查询请求开始
        log.info("开始获取图片详情: imageId={}", id);
        
        try {
            // 调用服务层查询图片详情
            ImageVO imageVO = imageService.getImageById(id);
            
            // 记录查询成功
            log.info("获取图片详情成功: imageId={}, filename={}, size={} bytes", 
                id, imageVO.getFileName(), imageVO.getFileSize());
            
            return Result.success(imageVO);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("获取图片详情失败: imageId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除图片
     * 
     * 业务逻辑：
     * 1. 验证图片是否存在
     * 2. 检查图片是否被资源使用
     * 3. 删除存储服务中的文件（原图和缩略图）
     * 4. 删除数据库中的记录
     * 
     * @param id 图片ID
     * @return 删除成功响应
     * @throws ResourceNotFoundException 当图片不存在时抛出
     * @throws ValidationException 当图片正在被使用时抛出
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除图片")
    @OperationLog(module = "图片管理", type = "删除", description = "删除图片", audit = true)
    public Result<Void> deleteImage(@PathVariable Long id) {
        // 记录删除请求开始
        log.info("开始删除图片: imageId={}", id);
        
        try {
            // 调用服务层删除图片
            imageService.deleteImage(id);
            
            // 记录删除成功
            log.info("删除图片成功: imageId={}", id);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录删除失败
            log.error("删除图片失败: imageId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 批量删除图片
     * 
     * 业务逻辑：
     * 1. 遍历图片ID列表
     * 2. 逐个调用删除方法
     * 3. 记录删除失败的图片
     * 4. 继续处理剩余图片
     * 
     * @param ids 图片ID列表
     * @return 批量删除成功响应
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除图片")
    @OperationLog(module = "图片管理", type = "批量删除", description = "批量删除图片", audit = true)
    public Result<Void> deleteImages(@RequestBody List<Long> ids) {
        // 记录批量删除请求开始
        log.info("开始批量删除图片: count={}, ids={}", ids.size(), ids);
        
        try {
            // 调用服务层批量删除图片
            imageService.deleteImages(ids);
            
            // 记录批量删除成功
            log.info("批量删除图片完成: count={}", ids.size());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录批量删除失败
            log.error("批量删除图片失败: ids={}, error={}", ids, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 检查图片使用情况
     * 
     * 业务逻辑：
     * 1. 验证图片是否存在
     * 2. 检查图片的使用状态
     * 3. 返回使用情况
     * 
     * @param id 图片ID
     * @return true表示正在被使用，false表示未被使用
     * @throws ResourceNotFoundException 当图片不存在时抛出
     */
    @GetMapping("/{id}/usage")
    @Operation(summary = "检查图片使用情况")
    public Result<Boolean> checkImageUsage(@PathVariable Long id) {
        // 记录检查请求开始
        log.info("开始检查图片使用情况: imageId={}", id);
        
        try {
            // 调用服务层检查图片使用情况
            boolean isUsed = imageService.checkImageUsage(id);
            
            // 记录检查结果
            log.info("检查图片使用情况完成: imageId={}, isUsed={}", id, isUsed);
            
            return Result.success(isUsed);
            
        } catch (Exception e) {
            // 记录检查失败
            log.error("检查图片使用情况失败: imageId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
}
