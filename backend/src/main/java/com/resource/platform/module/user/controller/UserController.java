package com.resource.platform.module.user.controller;

import com.resource.platform.common.BizErrorCode;
import com.resource.platform.common.Result;
import com.resource.platform.exception.BusinessException;
import com.resource.platform.module.user.dto.LoginDTO;
import com.resource.platform.module.user.service.UserService;
import com.resource.platform.util.JwtUtil;
import com.resource.platform.module.user.vo.LoginVO;
import com.resource.platform.module.user.vo.UserVO;
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

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO dto) {
        UserVO userVO = userService.authenticate(dto.getUsername(), dto.getPassword());

        log.debug("为用户生成JWT令牌: username={}", dto.getUsername());
        String token = jwtUtil.generateToken(dto.getUsername());

        LoginVO loginVO = new LoginVO(token, userVO);
        return Result.success(loginVO);
    }

    @Operation(summary = "管理员登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "unknown";
        log.debug("用户登出: username={}", username);

        SecurityContextHolder.clearContext();
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException(BizErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        log.debug("从安全上下文获取用户名: username={}", username);

        UserVO userVO = userService.getUserByUsername(username);
        return Result.success(userVO);
    }
}
