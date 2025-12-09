package com.resource.platform.controller;

import com.resource.platform.annotation.OperationLog;
import com.resource.platform.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 调试工具控制器
 * 
 * 功能说明：
 * - 提供密码编码和验证的调试功能
 * - 支持密码加密测试和验证测试
 * - 用于开发和测试环境的密码调试
 * - 帮助开发人员验证密码加密逻辑
 * 
 * 主要职责：
 * - 密码编码功能测试
 * - 密码验证功能测试
 * - 调试信息的安全处理
 * - 开发环境的辅助工具
 * 
 * 安全注意：
 * - 仅用于开发和测试环境
 * - 生产环境应禁用此控制器
 * - 密码信息需要脱敏处理
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "调试工具")
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 密码编码调试接口
     * 
     * 业务逻辑：
     * 1. 验证输入密码的有效性
     * 2. 使用Spring Security的密码编码器进行加密
     * 3. 返回原始密码和加密后的密码（脱敏处理）
     * 4. 记录调试操作日志
     * 
     * 安全注意：
     * - 原始密码在日志中会被脱敏处理
     * - 仅用于开发和测试环境
     * - 生产环境应禁用此接口
     * 
     * @param password 需要编码的原始密码
     * @return 包含原始密码（脱敏）和编码后密码的结果
     * @throws IllegalArgumentException 当密码为空时抛出
     */
    @Operation(summary = "密码编码调试")
    @GetMapping("/encode-password")
    @OperationLog(module = "调试工具", type = "密码编码", description = "调试密码编码功能")
    public Result<Map<String, String>> encodePassword(@RequestParam String password) {
        // 记录请求开始（密码脱敏处理）
        log.info("开始密码编码调试: passwordLength={}", password != null ? password.length() : 0);
        
        try {
            // 步骤1：验证输入参数
            // 检查密码是否为空或无效
            if (password == null || password.trim().isEmpty()) {
                log.warn("密码编码调试失败: 密码为空");
                throw new IllegalArgumentException("密码不能为空");
            }
            
            // 检查密码长度是否合理
            if (password.length() > 100) {
                log.warn("密码编码调试失败: 密码长度过长, length={}", password.length());
                throw new IllegalArgumentException("密码长度不能超过100个字符");
            }
            
            // 步骤2：执行密码编码
            // 使用Spring Security的BCrypt密码编码器进行加密
            log.debug("执行密码编码操作");
            String encoded = passwordEncoder.encode(password);
            
            // 步骤3：构建返回结果
            // 创建结果Map，注意原始密码需要脱敏处理
            log.debug("构建密码编码结果");
            Map<String, String> result = new HashMap<>();
            
            // 原始密码脱敏处理：只显示长度信息
            result.put("rawPassword", maskPassword(password));
            result.put("encodedPassword", encoded);
            
            // 记录成功结果（不记录具体密码内容）
            log.info("密码编码调试成功: passwordLength={}, encodedLength={}", 
                password.length(), encoded.length());
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常（不记录具体密码内容）
            log.error("密码编码调试失败: passwordLength={}, error={}", 
                password != null ? password.length() : 0, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 密码验证调试接口
     * 
     * 业务逻辑：
     * 1. 验证输入参数的有效性
     * 2. 使用Spring Security的密码编码器进行验证
     * 3. 比较原始密码和加密密码是否匹配
     * 4. 返回验证结果（密码信息脱敏处理）
     * 5. 记录调试操作日志
     * 
     * 安全注意：
     * - 原始密码在日志和返回结果中会被脱敏处理
     * - 仅用于开发和测试环境
     * - 生产环境应禁用此接口
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 已编码的密码
     * @return 包含验证结果的Map，密码信息已脱敏
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    @Operation(summary = "密码验证调试")
    @GetMapping("/verify-password")
    @OperationLog(module = "调试工具", type = "密码验证", description = "调试密码验证功能")
    public Result<Map<String, Object>> verifyPassword(
            @RequestParam String rawPassword,
            @RequestParam String encodedPassword) {
        // 记录请求开始（密码脱敏处理）
        log.info("开始密码验证调试: rawPasswordLength={}, encodedPasswordLength={}", 
            rawPassword != null ? rawPassword.length() : 0,
            encodedPassword != null ? encodedPassword.length() : 0);
        
        try {
            // 步骤1：验证输入参数
            // 检查原始密码是否为空
            if (rawPassword == null || rawPassword.trim().isEmpty()) {
                log.warn("密码验证调试失败: 原始密码为空");
                throw new IllegalArgumentException("原始密码不能为空");
            }
            
            // 检查编码密码是否为空
            if (encodedPassword == null || encodedPassword.trim().isEmpty()) {
                log.warn("密码验证调试失败: 编码密码为空");
                throw new IllegalArgumentException("编码密码不能为空");
            }
            
            // 检查编码密码格式是否正确（BCrypt格式通常以$2a$开头）
            if (!encodedPassword.startsWith("$2a$") && !encodedPassword.startsWith("$2b$") && !encodedPassword.startsWith("$2y$")) {
                log.warn("密码验证调试警告: 编码密码格式可能不正确, prefix={}", 
                    encodedPassword.length() > 4 ? encodedPassword.substring(0, 4) : encodedPassword);
            }
            
            // 步骤2：执行密码验证
            // 使用Spring Security的密码编码器进行验证
            log.debug("执行密码验证操作");
            boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
            
            // 步骤3：构建返回结果
            // 创建结果Map，注意密码信息需要脱敏处理
            log.debug("构建密码验证结果: matches={}", matches);
            Map<String, Object> result = new HashMap<>();
            
            // 原始密码和编码密码都进行脱敏处理
            result.put("rawPassword", maskPassword(rawPassword));
            result.put("encodedPassword", maskEncodedPassword(encodedPassword));
            result.put("matches", matches);
            
            // 记录成功结果（不记录具体密码内容）
            log.info("密码验证调试成功: rawPasswordLength={}, encodedPasswordLength={}, matches={}", 
                rawPassword.length(), encodedPassword.length(), matches);
            
            return Result.success(result);
            
        } catch (Exception e) {
            // 记录异常（不记录具体密码内容）
            log.error("密码验证调试失败: rawPasswordLength={}, encodedPasswordLength={}, error={}", 
                rawPassword != null ? rawPassword.length() : 0,
                encodedPassword != null ? encodedPassword.length() : 0,
                e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 原始密码脱敏处理
     * 
     * 将原始密码替换为星号，只保留长度信息
     * 
     * @param password 原始密码
     * @return 脱敏后的密码显示
     */
    private String maskPassword(String password) {
        if (password == null) {
            return null;
        }
        
        // 根据密码长度生成对应数量的星号
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            masked.append("*");
        }
        
        // 添加长度信息
        return masked.toString() + " (长度:" + password.length() + ")";
    }
    
    /**
     * 编码密码脱敏处理
     * 
     * 只显示编码密码的前缀和长度信息，隐藏具体内容
     * 
     * @param encodedPassword 编码后的密码
     * @return 脱敏后的编码密码显示
     */
    private String maskEncodedPassword(String encodedPassword) {
        if (encodedPassword == null) {
            return null;
        }
        
        // 只显示前4个字符（通常是$2a$）和长度信息
        String prefix = encodedPassword.length() > 4 ? encodedPassword.substring(0, 4) : encodedPassword;
        return prefix + "******* (长度:" + encodedPassword.length() + ")";
    }
}
