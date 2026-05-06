package com.resource.platform.module.system.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.BizErrorCode;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.common.Result;
import com.resource.platform.module.system.entity.SystemConfig;
import com.resource.platform.module.system.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 *
 * @author 系统
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@Tag(name = "系统配置", description = "系统配置管理接口")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping("/public")
    @Operation(summary = "获取公开配置")
    public Result<Map<String, Object>> getPublicConfigs() {
        List<String> publicKeys = Arrays.asList(
            "site.name", "site.title", "site.description", "site.keywords",
            "site.announcement", "site.copyright", "site.buildTime", "site.icp", "site.theme"
        );
        log.debug("定义公开配置项: keys={}", publicKeys);

        Map<String, String> configs = configService.getConfigsByKeys(publicKeys);
        log.debug("查询到公开配置: count={}", configs.size());

        Map<String, Object> result = new HashMap<>();
        result.put("configs", configs);
        return Result.success(result);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, List<SystemConfig>>> getAllConfigs() {
        Map<String, List<SystemConfig>> configs = configService.getAllConfigs();
        return Result.success(configs);
    }

    @GetMapping("/categories")
    @Operation(summary = "获取配置分类列表")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<String>> getConfigCategories() {
        List<String> categories = configService.getConfigCategories();
        return Result.success(categories);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "根据类别获取配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SystemConfig>> getConfigsByCategory(@PathVariable String category) {
        List<SystemConfig> configs = configService.getConfigsByCategory(category);
        return Result.success(configs);
    }

    @GetMapping("/{key}")
    @Operation(summary = "根据键获取配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemConfig> getConfigByKey(@PathVariable String key) {
        SystemConfig config = configService.getConfigByKey(key);
        return Result.success(config);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量获取配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> getConfigsByKeys(@RequestBody List<String> keys) {
        Map<String, String> configs = configService.getConfigsByKeys(keys);
        return Result.success(configs);
    }

    @PutMapping("/{key}")
    @Operation(summary = "更新配置")
    @OperationLog(module = "系统配置", type = "更新", description = "更新系统配置", audit = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");

        if (value == null) {
            log.warn("配置值为空: key={}", key);
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "配置值不能为空");
        }

        configService.updateConfig(key, value);
        return Result.success();
    }

    @PutMapping("/batch")
    @Operation(summary = "批量更新配置")
    @OperationLog(module = "系统配置", type = "批量更新", description = "批量更新系统配置", audit = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateConfigs(@RequestBody Map<String, String> configs) {
        if (configs == null || configs.isEmpty()) {
            log.warn("批量更新配置参数为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "配置数据不能为空");
        }

        configService.updateConfigs(configs);
        return Result.success();
    }

    @PostMapping("/{key}/reset")
    @Operation(summary = "重置配置")
    @OperationLog(module = "系统配置", type = "重置", description = "重置系统配置", audit = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetConfig(@PathVariable String key) {
        configService.resetConfig(key);
        return Result.success();
    }

    @PostMapping("/test/email")
    @Operation(summary = "测试邮件配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> testEmailConfig(@RequestBody Map<String, String> emailConfig) {
        if (emailConfig == null || emailConfig.isEmpty()) {
            log.warn("邮件配置参数为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "邮件配置参数不能为空");
        }

        boolean success = configService.testEmailConfig(emailConfig);
        if (!success) {
            log.warn("邮件配置测试失败: host={}, port={}",
                emailConfig.get("host"), emailConfig.get("port"));
        }
        return Result.success(success);
    }

    @PostMapping("/test/storage")
    @Operation(summary = "测试存储配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> testStorageConfig(@RequestBody Map<String, String> storageConfig) {
        if (storageConfig == null || storageConfig.isEmpty()) {
            log.warn("存储配置参数为空");
            throw new BusinessException(BizErrorCode.PARAM_ERROR, "存储配置参数不能为空");
        }

        boolean success = configService.testStorageConfig(storageConfig);
        if (!success) {
            log.warn("存储配置测试失败: type={}, endpoint={}",
                storageConfig.get("type"), storageConfig.get("endpoint"));
        }
        return Result.success(success);
    }
}
