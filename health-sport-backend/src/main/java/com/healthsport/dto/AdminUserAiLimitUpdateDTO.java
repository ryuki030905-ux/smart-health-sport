package com.healthsport.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserAiLimitUpdateDTO {

    @NotNull(message = "aiDailyLimit不能为空")
    @Min(value = 0, message = "aiDailyLimit不能小于0")
    @Max(value = 9999, message = "aiDailyLimit不能大于9999")
    private Integer aiDailyLimit;
}
