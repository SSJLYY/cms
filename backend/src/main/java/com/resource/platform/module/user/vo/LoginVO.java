package com.resource.platform.module.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应 VO
 *
 * <p>包含 JWT 令牌和用户基本信息。
 */
@Data
@AllArgsConstructor
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JWT 访问令牌 */
    private String token;

    /** 用户信息 */
    private UserVO user;
}
