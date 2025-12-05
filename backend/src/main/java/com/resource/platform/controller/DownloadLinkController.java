package com.resource.platform.controller;

import com.resource.platform.common.Result;
import com.resource.platform.dto.DownloadLinkDTO;
import com.resource.platform.service.DownloadLinkService;
import com.resource.platform.vo.DownloadLinkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "下载链接管理")
@RestController
@RequestMapping("/api/download-links")
public class DownloadLinkController {

    @Autowired
    private DownloadLinkService downloadLinkService;

    @Operation(summary = "获取资源的下载链接")
    @GetMapping("/resource/{resourceId}")
    public Result<List<DownloadLinkVO>> getDownloadLinks(@PathVariable Long resourceId) {
        List<DownloadLinkVO> links = downloadLinkService.getDownloadLinks(resourceId);
        return Result.success(links);
    }

    @Operation(summary = "添加下载链接")
    @PostMapping("/admin/create")
    public Result<DownloadLinkVO> createDownloadLink(@Validated @RequestBody DownloadLinkDTO dto) {
        DownloadLinkVO linkVO = downloadLinkService.createDownloadLink(dto);
        return Result.success(linkVO);
    }

    @Operation(summary = "删除下载链接")
    @DeleteMapping("/admin/delete/{id}")
    public Result<Void> deleteDownloadLink(@PathVariable Long id) {
        downloadLinkService.deleteDownloadLink(id);
        return Result.success();
    }
}
