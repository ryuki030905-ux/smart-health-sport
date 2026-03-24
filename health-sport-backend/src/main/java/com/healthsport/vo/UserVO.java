package com.healthsport.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 返回给前端的用户基本信息。
// 这里只放前端真要用的字段，像密码这种肯定不会往外带。
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String role;
}

