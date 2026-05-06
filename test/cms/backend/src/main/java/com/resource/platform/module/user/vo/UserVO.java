package com.resource.platform.module.user.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户视图对象
 *
 * <p>对外暴露的用户信息，不包含敏感字段（密码）。
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 角色（ADMIN / USER） */
    private String role;
}
