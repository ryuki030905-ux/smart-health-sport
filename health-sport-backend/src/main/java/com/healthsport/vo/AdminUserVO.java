package com.healthsport.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 管理员页面展示用户列表时用的对象。
// 可以理解成专门给后台用户管理页准备的一份用户信息。
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private Integer gender;
    private Integer age;
    private String role;
    private Integer status;
    private Integer aiDailyLimit;
    private java.time.LocalDateTime createTime;
}

