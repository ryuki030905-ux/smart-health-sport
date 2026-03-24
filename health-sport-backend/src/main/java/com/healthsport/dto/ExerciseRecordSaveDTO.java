package com.healthsport.dto;

import jakarta.validation.constraints.Min;
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
// 运动打卡时提交的参数。
// 选了哪个运动、日期、时长、强度这些，都会先从这里接。
public class ExerciseRecordSaveDTO {

    @NotNull(message = "运动项目不能为空")
    private Long exerciseDictId;

    @NotNull(message = "运动日期不能为空")
    private LocalDate exerciseDate;

    @NotNull(message = "运动时长不能为空")
    @Min(value = 1, message = "运动时长必须大于0")
    private Integer durationMin;

    private String intensity;

    private String remark;
}

