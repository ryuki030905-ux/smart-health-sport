package com.healthsport.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 当前登录用户的简化信息。
// 过滤器把 token 解析完之后，会把用户 id、用户名、角色塞到这里。
// 后面业务代码如果想知道当前是谁登录了，一般就是从这里拿。
public class JwtUserPrincipal {

    private Long userId;
    private String username;
    private String role;
}

