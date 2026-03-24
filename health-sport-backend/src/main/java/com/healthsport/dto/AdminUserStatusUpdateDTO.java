package com.healthsport.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
// 管理员改用户状态时传的参数。
// 现在基本就一个 status，看是启用还是禁用。
public class AdminUserStatusUpdateDTO {

    @NotNull(message = "status不能为空")
    @Min(value = 0, message = "status仅支持0或1")
    @Max(value = 1, message = "status仅支持0或1")
    private Integer status;
}

