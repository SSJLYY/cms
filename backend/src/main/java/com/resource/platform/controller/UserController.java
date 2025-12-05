package com.resource.platform.controller;

import com.resource.platform.common.Result;
import com.resource.platform.dto.LoginDTO;
import com.resource.platform.service.UserService;
import com.resource.platform.util.JwtUtil;
import com.resource.platform.vo.LoginVO;
import com.resource.platform.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        String token = jwtUtil.generateToken(dto.getUsername());
        LoginVO loginVO = new LoginVO(token, userVO);
        return Result.success(loginVO);
    }

    @Operation(summary = "管理员登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        SecurityContextHolder.clearContext();
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserVO userVO = userService.getUserByUsername(username);
        return Result.success(userVO);
    }
}
