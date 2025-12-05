package com.resource.platform.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private UserVO userInfo;
    
    public LoginVO(String token, UserVO userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }
}
