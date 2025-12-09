package com.resource.platform.controller;

import com.resource.platform.common.Result;
import com.resource.platform.dto.LoginDTO;
import com.resource.platform.service.UserService;
import com.resource.platform.util.JwtUtil;
import com.resource.platform.vo.LoginVO;
import com.resource.platform.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 
 * 功能说明：
 * - 提供管理员登录认证接口
 * - 提供管理员登出接口
 * - 提供获取当前登录用户信息接口
 * - 处理用户认证和会话管理相关的HTTP请求
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理员登录
     * 
     * 业务逻辑：
     * 1. 验证用户名和密码的有效性
     * 2. 调用认证服务验证用户凭证
     * 3. 生成JWT访问令牌
     * 4. 返回令牌和用户信息
     * 
     * @param dto 登录数据传输对象，包含用户名和密码
     * @return 登录结果，包含JWT令牌和用户信息
     * @throws RuntimeException 当用户不存在、被禁用或密码错误时抛出
     */
    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO dto) {
        // 记录登录请求开始，注意：密码不能记录到日志中
        log.info("开始处理管理员登录请求: username={}", dto.getUsername());
        
        try {
            // 步骤1：调用用户服务进行身份认证
            // 验证用户名和密码是否匹配，检查用户状态是否正常
            UserVO userVO = userService.authenticate(dto.getUsername(), dto.getPassword());
            
            // 步骤2：生成JWT访问令牌
            // 使用用户名作为令牌的主体信息
            log.debug("为用户生成JWT令牌: username={}", dto.getUsername());
            String token = jwtUtil.generateToken(dto.getUsername());
            
            // 步骤3：构建登录响应对象
            // 包含令牌和用户基本信息
            LoginVO loginVO = new LoginVO(token, userVO);
            
            // 记录登录成功
            log.info("管理员登录成功: username={}, userId={}", dto.getUsername(), userVO.getId());
            
            return Result.success(loginVO);
            
        } catch (Exception e) {
            // 记录登录失败，注意：不记录密码信息
            log.error("管理员登录失败: username={}, error={}", dto.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 管理员登出
     * 
     * 业务逻辑：
     * 1. 清除当前线程的安全上下文
     * 2. 移除用户的认证信息
     * 3. 返回登出成功响应
     * 
     * @return 登出成功响应
     */
    @Operation(summary = "管理员登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 获取当前登录用户信息用于日志记录
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "unknown";
        
        // 记录登出请求开始
        log.info("开始处理管理员登出请求: username={}", username);
        
        try {
            // 清除Spring Security上下文
            // 这会移除当前线程中存储的认证信息
            SecurityContextHolder.clearContext();
            
            // 记录登出成功
            log.info("管理员登出成功: username={}", username);
            
            return Result.success();
            
        } catch (Exception e) {
            // 记录登出异常
            log.error("管理员登出失败: username={}, error={}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取当前用户信息
     * 
     * 业务逻辑：
     * 1. 从安全上下文中获取当前认证信息
     * 2. 提取用户名
     * 3. 查询用户详细信息
     * 4. 返回用户信息
     * 
     * @return 当前登录用户的详细信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        // 记录请求开始
        log.info("开始获取当前用户信息");
        
        try {
            // 步骤1：从Spring Security上下文获取认证信息
            // 上下文中存储了当前请求的用户认证状态
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // 步骤2：提取用户名
            // getName()返回认证主体的标识，通常是用户名
            String username = authentication.getName();
            log.debug("从安全上下文获取用户名: username={}", username);
            
            // 步骤3：查询用户详细信息
            // 调用服务层根据用户名查询完整的用户信息
            UserVO userVO = userService.getUserByUsername(username);
            
            // 记录查询成功
            log.info("获取当前用户信息成功: username={}, userId={}", username, 
                userVO != null ? userVO.getId() : null);
            
            return Result.success(userVO);
            
        } catch (Exception e) {
            // 记录查询失败
            log.error("获取当前用户信息失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
}
