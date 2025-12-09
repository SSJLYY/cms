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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 * 
 * 功能说明：
 * - 提供系统配置的查询接口，支持按类别、按键值查询
 * - 提供系统配置的更新接口，支持单个和批量更新
 * - 提供配置重置功能，恢复到默认值
 * - 提供配置测试功能，验证邮件和存储配置的有效性
 * - 区分公开配置和管理配置，确保数据安全
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

    /**
     * 获取公开配置（供前台使用）
     * 
     * 业务逻辑：
     * 1. 定义前台可访问的配置项列表
     * 2. 从数据库查询指定的配置项
     * 3. 封装返回结果，确保数据格式统一
     * 
     * @return 包含公开配置的结果对象
     */
    @GetMapping("/public")
    @Operation(summary = "获取公开配置")
    public Result<Map<String, Object>> getPublicConfigs() {
        // 记录请求开始
        log.info("开始获取公开配置");
        
        try {
            // 步骤1：定义前台需要的配置项
            // 这些配置项是安全的，可以暴露给前台使用
            List<String> publicKeys = Arrays.asList(
                "site.name", "site.title", "site.description", "site.keywords",
                "site.announcement", "site.copyright", "site.buildTime", "site.icp", "site.theme"
            );
            log.debug("定义公开配置项: keys={}", publicKeys);
            
            // 步骤2：批量查询配置项
            // 调用Service层获取指定键的配置值
            Map<String, String> configs = configService.getConfigsByKeys(publicKeys);
            log.debug("查询到公开配置: count={}", configs.size());
            
            // 步骤3：封装返回结果
            // 将配置数据包装在统一的结构中
            Map<String, Object> result = new HashMap<>();
            result.put("configs", configs);
            
            // 记录成功结果
            log.info("公开配置获取成功: configCount={}", configs.size());
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常
            log.error("公开配置获取失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取所有配置（按类别分组）
     * 
     * 业务逻辑：
     * 1. 查询数据库中的所有配置项
     * 2. 按配置类别进行分组
     * 3. 返回分组后的配置数据
     * 
     * @return 按类别分组的配置列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有配置")
    public Result<Map<String, List<SystemConfig>>> getAllConfigs() {
        // 记录请求开始
        log.info("开始获取所有配置");
        
        try {
            // 调用Service层获取所有配置并按类别分组
            Map<String, List<SystemConfig>> configs = configService.getAllConfigs();
            
            // 统计配置数量
            int totalConfigs = configs.values().stream()
                .mapToInt(List::size)
                .sum();
            
            // 记录成功结果
            log.info("所有配置获取成功: categories={}, totalConfigs={}", 
                configs.keySet().size(), totalConfigs);
            
            return Result.success(configs);
            
        } catch (Exception e) {
            // 记录异常
            log.error("所有配置获取失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取配置分类列表
     * 
     * 业务逻辑：
     * 1. 查询所有配置项的分类信息
     * 2. 去重并排序分类名称
     * 3. 返回分类列表供前端选择使用
     * 
     * @return 配置分类名称列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取配置分类列表")
    public Result<List<String>> getConfigCategories() {
        // 记录请求开始
        log.info("开始获取配置分类列表");
        
        try {
            // 调用Service层获取所有配置分类
            List<String> categories = configService.getConfigCategories();
            
            // 记录成功结果
            log.info("配置分类列表获取成功: categories={}, count={}", 
                categories, categories.size());
            
            return Result.success(categories);
            
        } catch (Exception e) {
            // 记录异常
            log.error("配置分类列表获取失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据类别获取配置
     * 
     * 业务逻辑：
     * 1. 验证分类参数的有效性
     * 2. 查询指定分类下的所有配置项
     * 3. 按排序字段排序返回结果
     * 
     * @param category 配置分类名称
     * @return 指定分类下的配置列表
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "根据类别获取配置")
    public Result<List<SystemConfig>> getConfigsByCategory(@PathVariable String category) {
        // 记录请求开始
        log.info("开始根据类别获取配置: category={}", category);
        
        try {
            // 调用Service层查询指定分类的配置
            List<SystemConfig> configs = configService.getConfigsByCategory(category);
            
            // 记录成功结果
            log.info("根据类别获取配置成功: category={}, count={}", 
                category, configs.size());
            
            return Result.success(configs);
            
        } catch (Exception e) {
            // 记录异常
            log.error("根据类别获取配置失败: category={}, error={}", 
                category, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据键获取配置
     * 
     * 业务逻辑：
     * 1. 验证配置键参数的有效性
     * 2. 查询数据库获取指定键的配置项
     * 3. 如果配置不存在则抛出异常
     * 
     * @param key 配置键名
     * @return 配置项详细信息
     * @throws ResourceNotFoundException 当配置项不存在时抛出
     */
    @GetMapping("/{key}")
    @Operation(summary = "根据键获取配置")
    public Result<SystemConfig> getConfigByKey(@PathVariable String key) {
        // 记录请求开始
        log.info("开始根据键获取配置: key={}", key);
        
        try {
            // 调用Service层查询指定键的配置
            SystemConfig config = configService.getConfigByKey(key);
            
            // 记录成功结果（注意不记录敏感配置值）
            log.info("根据键获取配置成功: key={}, category={}", 
                key, config.getCategory());
            
            return Result.success(config);
            
        } catch (Exception e) {
            // 记录异常
            log.error("根据键获取配置失败: key={}, error={}", 
                key, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 批量获取配置
     * 
     * 业务逻辑：
     * 1. 验证配置键列表参数的有效性
     * 2. 批量查询指定键的配置值
     * 3. 返回键值对映射结果
     * 
     * @param keys 配置键名列表
     * @return 配置键值对映射
     */
    @PostMapping("/batch")
    @Operation(summary = "批量获取配置")
    public Result<Map<String, String>> getConfigsByKeys(@RequestBody List<String> keys) {
        // 记录请求开始
        log.info("开始批量获取配置: keys={}, count={}", keys, keys.size());
        
        try {
            // 调用Service层批量查询配置
            Map<String, String> configs = configService.getConfigsByKeys(keys);
            
            // 记录成功结果
            log.info("批量获取配置成功: requestCount={}, resultCount={}", 
                keys.size(), configs.size());
            
            return Result.success(configs);
            
        } catch (Exception e) {
            // 记录异常
            log.error("批量获取配置失败: keys={}, error={}", 
                keys, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 更新配置
     * 
     * 业务逻辑：
     * 1. 验证配置键和值的有效性
     * 2. 检查配置项是否存在
     * 3. 更新配置值到数据库
     * 4. 记录配置变更日志
     * 
     * @param key 配置键名
     * @param body 包含新配置值的请求体
     * @return 操作结果
     * @throws ResourceNotFoundException 当配置项不存在时抛出
     */
    @PutMapping("/{key}")
    @Operation(summary = "更新配置")
    @OperationLog(module = "系统配置", type = "更新", description = "更新系统配置", audit = true)
    public Result<Void> updateConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        // 记录请求开始（注意敏感信息脱敏）
        String value = body.get("value");
        log.info("开始更新配置: key={}", key);
        
        try {
            // 步骤1：验证参数
            // 检查配置值是否为空
            if (value == null) {
                log.warn("配置值为空: key={}", key);
                throw new IllegalArgumentException("配置值不能为空");
            }
            
            // 步骤2：调用Service层更新配置
            // Service层会验证配置项是否存在
            configService.updateConfig(key, value);
            
            // 记录成功结果（敏感配置不记录具体值）
            boolean isSensitive = key.toLowerCase().contains("password") || 
                                key.toLowerCase().contains("secret") ||
                                key.toLowerCase().contains("token");
            if (isSensitive) {
                log.info("配置更新成功: key={}, value=******", key);
            } else {
                log.info("配置更新成功: key={}, value={}", key, value);
            }
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("配置更新失败: key={}, error={}", key, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 批量更新配置
     * 
     * 业务逻辑：
     * 1. 验证配置数据的完整性
     * 2. 逐个更新配置项
     * 3. 记录更新结果和失败信息
     * 4. 返回批量操作结果
     * 
     * @param configs 配置键值对映射
     * @return 操作结果
     */
    @PutMapping("/batch")
    @Operation(summary = "批量更新配置")
    @OperationLog(module = "系统配置", type = "批量更新", description = "批量更新系统配置", audit = true)
    public Result<Void> updateConfigs(@RequestBody Map<String, String> configs) {
        // 记录请求开始
        log.info("开始批量更新配置: count={}, keys={}", 
            configs.size(), configs.keySet());
        
        try {
            // 步骤1：验证参数
            // 检查配置映射是否为空
            if (configs == null || configs.isEmpty()) {
                log.warn("批量更新配置参数为空");
                throw new IllegalArgumentException("配置数据不能为空");
            }
            
            // 步骤2：调用Service层批量更新配置
            // Service层会处理每个配置项的更新和异常
            configService.updateConfigs(configs);
            
            // 记录成功结果
            log.info("批量更新配置成功: count={}", configs.size());
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("批量更新配置失败: count={}, keys={}, error={}", 
                configs.size(), configs.keySet(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 重置配置
     * 
     * 业务逻辑：
     * 1. 验证配置键的有效性
     * 2. 检查配置项是否存在默认值
     * 3. 将配置值重置为默认值
     * 4. 记录重置操作日志
     * 
     * @param key 配置键名
     * @return 操作结果
     * @throws ResourceNotFoundException 当配置项不存在时抛出
     */
    @PostMapping("/{key}/reset")
    @Operation(summary = "重置配置")
    @OperationLog(module = "系统配置", type = "重置", description = "重置系统配置", audit = true)
    public Result<Void> resetConfig(@PathVariable String key) {
        // 记录请求开始
        log.info("开始重置配置: key={}", key);
        
        try {
            // 调用Service层重置配置
            // Service层会检查配置项是否存在以及是否有默认值
            configService.resetConfig(key);
            
            // 记录成功结果
            log.info("配置重置成功: key={}", key);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录异常
            log.error("配置重置失败: key={}, error={}", key, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 测试邮件配置
     * 
     * 业务逻辑：
     * 1. 验证邮件配置参数的完整性
     * 2. 使用提供的配置尝试发送测试邮件
     * 3. 返回测试结果（成功/失败）
     * 4. 记录测试过程和结果
     * 
     * @param emailConfig 邮件配置参数（主机、端口、用户名、密码等）
     * @return 测试结果，true表示配置有效，false表示配置无效
     */
    @PostMapping("/test/email")
    @Operation(summary = "测试邮件配置")
    public Result<Boolean> testEmailConfig(@RequestBody Map<String, String> emailConfig) {
        // 记录请求开始（敏感信息脱敏）
        Map<String, String> logConfig = new HashMap<>(emailConfig);
        logConfig.put("password", "******"); // 脱敏处理密码
        log.info("开始测试邮件配置: config={}", logConfig);
        
        try {
            // 步骤1：验证配置参数
            // 检查必要的邮件配置参数是否存在
            if (emailConfig == null || emailConfig.isEmpty()) {
                log.warn("邮件配置参数为空");
                throw new IllegalArgumentException("邮件配置参数不能为空");
            }
            
            // 步骤2：调用Service层测试邮件配置
            // Service层会尝试建立邮件连接并发送测试邮件
            boolean success = configService.testEmailConfig(emailConfig);
            
            // 记录测试结果
            if (success) {
                log.info("邮件配置测试成功: host={}, port={}", 
                    emailConfig.get("host"), emailConfig.get("port"));
            } else {
                log.warn("邮件配置测试失败: host={}, port={}", 
                    emailConfig.get("host"), emailConfig.get("port"));
            }
            
            return Result.success(success);
            
        } catch (Exception e) {
            // 记录异常
            log.error("邮件配置测试异常: host={}, error={}", 
                emailConfig.get("host"), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 测试存储配置
     * 
     * 业务逻辑：
     * 1. 验证存储配置参数的完整性
     * 2. 使用提供的配置尝试连接存储服务
     * 3. 执行简单的读写测试操作
     * 4. 返回测试结果和详细信息
     * 
     * @param storageConfig 存储配置参数（类型、端点、密钥等）
     * @return 测试结果，true表示配置有效，false表示配置无效
     */
    @PostMapping("/test/storage")
    @Operation(summary = "测试存储配置")
    public Result<Boolean> testStorageConfig(@RequestBody Map<String, String> storageConfig) {
        // 记录请求开始（敏感信息脱敏）
        Map<String, String> logConfig = new HashMap<>(storageConfig);
        logConfig.put("secretKey", "******"); // 脱敏处理密钥
        logConfig.put("accessKey", "******"); // 脱敏处理访问密钥
        log.info("开始测试存储配置: config={}", logConfig);
        
        try {
            // 步骤1：验证配置参数
            // 检查必要的存储配置参数是否存在
            if (storageConfig == null || storageConfig.isEmpty()) {
                log.warn("存储配置参数为空");
                throw new IllegalArgumentException("存储配置参数不能为空");
            }
            
            // 步骤2：调用Service层测试存储配置
            // Service层会尝试连接存储服务并执行测试操作
            boolean success = configService.testStorageConfig(storageConfig);
            
            // 记录测试结果
            if (success) {
                log.info("存储配置测试成功: type={}, endpoint={}", 
                    storageConfig.get("type"), storageConfig.get("endpoint"));
            } else {
                log.warn("存储配置测试失败: type={}, endpoint={}", 
                    storageConfig.get("type"), storageConfig.get("endpoint"));
            }
            
            return Result.success(success);
            
        } catch (Exception e) {
            // 记录异常
            log.error("存储配置测试异常: type={}, error={}", 
                storageConfig.get("type"), e.getMessage(), e);
            throw e;
        }
    }
}
