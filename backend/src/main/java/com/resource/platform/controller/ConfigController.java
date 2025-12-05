package com.resource.platform.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.Result;
import com.resource.platform.entity.SystemConfig;
import com.resource.platform.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@Tag(name = "系统配置", description = "系统配置管理接口")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    /**
     * 获取公开配置（供前台使用）
     */
    @GetMapping("/public")
    @Operation(summary = "获取公开配置")
    public Result<Map<String, Object>> getPublicConfigs() {
        // 获取前台需要的配置项
        List<String> publicKeys = List.of(
            "site.name", "site.title", "site.description", "site.keywords",
            "site.announcement", "site.copyright", "site.buildTime", "site.icp"
        );
        Map<String, String> configs = configService.getConfigsByKeys(publicKeys);
        return Result.success(Map.of("configs", configs));
    }

    /**
     * 获取所有配置（按类别分组）
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有配置")
    public Result<Map<String, List<SystemConfig>>> getAllConfigs() {
        Map<String, List<SystemConfig>> configs = configService.getAllConfigs();
        return Result.success(configs);
    }

    /**
     * 获取配置分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取配置分类列表")
    public Result<List<String>> getConfigCategories() {
        List<String> categories = configService.getConfigCategories();
        return Result.success(categories);
    }

    /**
     * 根据类别获取配置
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "根据类别获取配置")
    public Result<List<SystemConfig>> getConfigsByCategory(@PathVariable String category) {
        List<SystemConfig> configs = configService.getConfigsByCategory(category);
        return Result.success(configs);
    }

    /**
     * 根据键获取配置
     */
    @GetMapping("/{key}")
    @Operation(summary = "根据键获取配置")
    public Result<SystemConfig> getConfigByKey(@PathVariable String key) {
        SystemConfig config = configService.getConfigByKey(key);
        return Result.success(config);
    }

    /**
     * 批量获取配置
     */
    @PostMapping("/batch")
    @Operation(summary = "批量获取配置")
    public Result<Map<String, String>> getConfigsByKeys(@RequestBody List<String> keys) {
        Map<String, String> configs = configService.getConfigsByKeys(keys);
        return Result.success(configs);
    }

    /**
     * 更新配置
     */
    @PutMapping("/{key}")
    @Operation(summary = "更新配置")
    @OperationLog(module = "系统配置", type = "更新", description = "更新系统配置", audit = true)
    public Result<Void> updateConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        configService.updateConfig(key, value);
        return Result.success();
    }

    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    @Operation(summary = "批量更新配置")
    @OperationLog(module = "系统配置", type = "批量更新", description = "批量更新系统配置", audit = true)
    public Result<Void> updateConfigs(@RequestBody Map<String, String> configs) {
        configService.updateConfigs(configs);
        return Result.success();
    }

    /**
     * 重置配置
     */
    @PostMapping("/{key}/reset")
    @Operation(summary = "重置配置")
    @OperationLog(module = "系统配置", type = "重置", description = "重置系统配置", audit = true)
    public Result<Void> resetConfig(@PathVariable String key) {
        configService.resetConfig(key);
        return Result.success();
    }

    /**
     * 测试邮件配置
     */
    @PostMapping("/test/email")
    @Operation(summary = "测试邮件配置")
    public Result<Boolean> testEmailConfig(@RequestBody Map<String, String> emailConfig) {
        boolean success = configService.testEmailConfig(emailConfig);
        return Result.success(success);
    }

    /**
     * 测试存储配置
     */
    @PostMapping("/test/storage")
    @Operation(summary = "测试存储配置")
    public Result<Boolean> testStorageConfig(@RequestBody Map<String, String> storageConfig) {
        boolean success = configService.testStorageConfig(storageConfig);
        return Result.success(success);
    }
}
