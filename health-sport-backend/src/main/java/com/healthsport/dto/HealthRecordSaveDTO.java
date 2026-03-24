package com.healthsport.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 保存健康档案时用的参数。
// 表单里填的日期、身高、体重、血压这些值，都会先到这个对象里。
public class HealthRecordSaveDTO {

    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    @NotNull(message = "身高不能为空")
    @DecimalMin(value = "0.01", message = "身高必须大于0")
    private BigDecimal height;

    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "0.01", message = "体重必须大于0")
    private BigDecimal weight;

    private Integer systolicBp;

    private Integer diastolicBp;

    private BigDecimal bloodSugar;

    private Integer heartRate;

    private String remark;
}

