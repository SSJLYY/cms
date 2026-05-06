package com.resource.platform.module.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求 DTO
 */
@Data
public class LoginDTO {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名最长50个字符")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度为6-100个字符")
    private String password;
}
