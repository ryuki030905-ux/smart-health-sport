package com.healthsport.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 登录成功后返回的数据。
// 里面主要有 token、过期时间，还有当前登录用户的一点基本信息。
public class LoginVO {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserVO userInfo;
}

