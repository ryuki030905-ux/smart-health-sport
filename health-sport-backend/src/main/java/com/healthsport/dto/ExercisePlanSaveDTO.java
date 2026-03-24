package com.healthsport.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 创建运动计划时提交的参数。
// 计划名、起止日期、目标时长这些值都会先放到这里。
public class ExercisePlanSaveDTO {

    @NotBlank(message = "计划名称不能为空")
    private String planName;

    private String planType;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @NotNull(message = "目标总时长不能为空")
    @Min(value = 1, message = "目标总时长必须大于0")
    private Integer targetDuration;

    @NotNull(message = "目标次数不能为空")
    @Min(value = 1, message = "目标次数必须大于0")
    private Integer targetTimes;
}

