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
 * 友情链接控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/friendlinks")
@RequiredArgsConstructor
@Tag(name = "友情链接管理", description = "友情链接管理相关接口")
public class FriendLinkController {
    
    private final FriendLinkService friendLinkService;
    
    @GetMapping("/page")
    @Operation(summary = "分页查询友情链接")
    public Result<PageResult<FriendLinkVO>> queryPage(FriendLinkQueryDTO queryDTO) {
        PageResult<FriendLinkVO> result = friendLinkService.queryPage(queryDTO);
        return Result.success(result);
    }
    
    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的友情链接")
    public Result<List<FriendLinkVO>> listEnabled() {
        List<FriendLinkVO> list = friendLinkService.listEnabled();
        return Result.success(list);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取友情链接")
    public Result<FriendLinkVO> getById(@PathVariable Long id) {
        FriendLinkVO vo = friendLinkService.getById(id);
        return Result.success(vo);
    }
    
    @PostMapping
    @OperationLog(module = "友情链接管理", type = "创建")
    @Operation(summary = "创建友情链接")
    public Result<Long> create(@Validated @RequestBody FriendLinkDTO dto) {
        Long id = friendLinkService.create(dto);
        return Result.success(id);
    }
    
    @PutMapping
    @OperationLog(module = "友情链接管理", type = "更新")
    @Operation(summary = "更新友情链接")
    public Result<Void> update(@Validated @RequestBody FriendLinkDTO dto) {
        friendLinkService.update(dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @OperationLog(module = "友情链接管理", type = "删除")
    @Operation(summary = "删除友情链接")
    public Result<Void> delete(@PathVariable Long id) {
        friendLinkService.delete(id);
        return Result.success();
    }
    
    @DeleteMapping("/batch")
    @OperationLog(module = "友情链接管理", type = "批量删除")
    @Operation(summary = "批量删除友情链接")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        friendLinkService.batchDelete(ids);
        return Result.success();
    }
    
    @PutMapping("/{id}/status")
    @OperationLog(module = "友情链接管理", type = "更新状态")
    @Operation(summary = "更新友情链接状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        friendLinkService.updateStatus(id, status);
        return Result.success();
    }
}
