package com.resource.platform.module.image.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.Result;
import com.resource.platform.module.image.dto.ImageQueryDTO;
import com.resource.platform.module.image.service.ImageService;
import com.resource.platform.module.image.vo.ImageBatchDeleteResultVO;
import com.resource.platform.module.image.vo.ImageStatisticsVO;
import com.resource.platform.module.image.vo.ImageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 图片管理控制器
 *
 * @author 系统
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
@Tag(name = "图片管理", description = "图片上传、查询、删除等接口")
@PreAuthorize("hasRole('ADMIN')")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/statistics")
    @Operation(summary = "获取图片统计信息")
    public Result<ImageStatisticsVO> getStatistics() {
        ImageStatisticsVO statistics = imageService.getStatistics();
        return Result.success(statistics);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传单张图片")
    @OperationLog(module = "图片管理", type = "上传", description = "上传图片")
    public Result<ImageVO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        log.debug("处理图片上传: filename={}, size={} bytes", file.getOriginalFilename(), file.getSize());
        ImageVO imageVO = imageService.uploadImage(file, uploaderId);
        return Result.success(imageVO);
    }

    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传图片")
    @OperationLog(module = "图片管理", type = "批量上传", description = "批量上传图片")
    public Result<List<ImageVO>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        log.debug("批量图片上传: fileCount={}, uploaderId={}", files.size(), uploaderId);
        List<ImageVO> imageVOs = imageService.uploadImages(files, uploaderId);
        return Result.success(imageVOs);
    }

    @PostMapping("/query")
    @Operation(summary = "查询图片列表")
    public Result<Page<ImageVO>> queryImages(@RequestBody ImageQueryDTO queryDTO) {
        Page<ImageVO> page = imageService.queryImages(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取图片详情")
    public Result<ImageVO> getImageById(@PathVariable Long id) {
        ImageVO imageVO = imageService.getImageById(id);
        return Result.success(imageVO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除图片")
    @OperationLog(module = "图片管理", type = "删除", description = "删除图片", audit = true)
    public Result<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量删除图片")
    @OperationLog(module = "图片管理", type = "批量删除", description = "批量删除图片", audit = true)
    public Result<ImageBatchDeleteResultVO> deleteImages(@RequestBody List<Long> ids) {
        return Result.success(imageService.deleteImages(ids));
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "检查图片使用情况")
    public Result<Boolean> checkImageUsage(@PathVariable Long id) {
        boolean isUsed = imageService.checkImageUsage(id);
        return Result.success(isUsed);
    }
}
