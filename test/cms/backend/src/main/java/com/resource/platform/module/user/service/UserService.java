package com.resource.platform.module.user.service;

import com.resource.platform.module.user.vo.UserVO;

public interface UserService {
    /**
     * 验证用户凭证
     */
    UserVO authenticate(String username, String password);
    
    /**
     * 根据用户名查询用户
     */
    UserVO getUserByUsername(String username);
}
