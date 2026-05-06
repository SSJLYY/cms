package com.resource.platform.module.resource.controller;

import com.resource.platform.common.BizErrorCode;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.common.Result;
import com.resource.platform.module.resource.dto.DownloadLinkDTO;
import com.resource.platform.module.resource.service.DownloadLinkService;
import com.resource.platform.module.resource.vo.DownloadLinkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 下载链接管理控制器
 *
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "下载链接管理")
@RestController
@RequestMapping("/api/download-links")
public class DownloadLinkController {

    @Autowired
    private DownloadLinkService downloadLinkService;

    @Operation(summary = "获取资源的下载链接")
    @GetMapping("/resource/{resourceId}")
    public Result<List<DownloadLinkVO>> getDownloadLinks(@PathVariable Long resourceId) {
        if (resourceId == null || resourceId <= 0) {
            log.warn("资源ID无效: resourceId={}", resourceId);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "资源ID无效");
        }

        List<DownloadLinkVO> links = downloadLinkService.getDownloadLinks(resourceId);
        return Result.success(links);
    }

    @Operation(summary = "添加下载链接")
    @PostMapping("/admin/create")
    public Result<DownloadLinkVO> createDownloadLink(@Validated @RequestBody DownloadLinkDTO dto) {
        if (dto.getResourceId() == null || dto.getResourceId() <= 0) {
            log.warn("资源ID无效: resourceId={}", dto.getResourceId());
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "资源ID无效");
        }

        if (dto.getDownloadUrl() == null || dto.getDownloadUrl().trim().isEmpty()) {
            log.warn("下载链接URL为空: resourceId={}", dto.getResourceId());
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "下载链接URL不能为空");
        }

        DownloadLinkVO linkVO = downloadLinkService.createDownloadLink(dto);
        return Result.success(linkVO);
    }

    @Operation(summary = "删除下载链接")
    @DeleteMapping("/admin/delete/{id}")
    public Result<Void> deleteDownloadLink(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.warn("下载链接ID无效: id={}", id);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "下载链接ID无效");
        }

        downloadLinkService.deleteDownloadLink(id);
        return Result.success();
    }
}
