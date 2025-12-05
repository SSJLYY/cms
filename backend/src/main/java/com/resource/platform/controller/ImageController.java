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
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取图片统计信息")
    public Result<ImageStatisticsVO> getStatistics() {
        ImageStatisticsVO statistics = imageService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 上传单张图片
     */
    @PostMapping("/upload")
    @Operation(summary = "上传单张图片")
    @OperationLog(module = "图片管理", type = "上传", description = "上传图片")
    public Result<ImageVO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        log.info("Controller收到图片上传请求: filename={}, size={}, uploaderId={}", 
                file.getOriginalFilename(), file.getSize(), uploaderId);
        try {
            ImageVO imageVO = imageService.uploadImage(file, uploaderId);
            log.info("Controller图片上传成功: imageId={}", imageVO.getId());
            return Result.success(imageVO);
        } catch (Exception e) {
            log.error("Controller图片上传失败", e);
            throw e;
        }
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传图片")
    @OperationLog(module = "图片管理", type = "批量上传", description = "批量上传图片")
    public Result<List<ImageVO>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        List<ImageVO> imageVOs = imageService.uploadImages(files, uploaderId);
        return Result.success(imageVOs);
    }

    /**
     * 查询图片列表
     */
    @PostMapping("/query")
    @Operation(summary = "查询图片列表")
    public Result<Page<ImageVO>> queryImages(@RequestBody ImageQueryDTO queryDTO) {
        Page<ImageVO> page = imageService.queryImages(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取图片详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取图片详情")
    public Result<ImageVO> getImageById(@PathVariable Long id) {
        ImageVO imageVO = imageService.getImageById(id);
        return Result.success(imageVO);
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除图片")
    @OperationLog(module = "图片管理", type = "删除", description = "删除图片", audit = true)
    public Result<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return Result.success();
    }

    /**
     * 批量删除图片
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除图片")
    @OperationLog(module = "图片管理", type = "批量删除", description = "批量删除图片", audit = true)
    public Result<Void> deleteImages(@RequestBody List<Long> ids) {
        imageService.deleteImages(ids);
        return Result.success();
    }

    /**
     * 检查图片使用情况
     */
    @GetMapping("/{id}/usage")
    @Operation(summary = "检查图片使用情况")
    public Result<Boolean> checkImageUsage(@PathVariable Long id) {
        boolean isUsed = imageService.checkImageUsage(id);
        return Result.success(isUsed);
    }
}
