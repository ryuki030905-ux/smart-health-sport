package com.healthsport.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 注册时提交的参数。
// 用户填的用户名、密码、年龄这些值，会先到这里再做校验。
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度需在4-20之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度需在8-20之间")
    private String password;

    private String nickname;

    @Min(value = 0, message = "性别参数非法")
    @Max(value = 2, message = "性别参数非法")
    private Integer gender;

    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 120, message = "年龄参数非法")
    private Integer age;

    private String occupation;
}

