package com.healthsport.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
// 饮食记录提交时的参数。
// 食物 id、餐次、日期、摄入量这些值，都会先接到这里。
public class DietRecordSaveDTO {

    @NotNull(message = "食物不能为空")
    private Long foodDictId;

    @NotBlank(message = "餐次不能为空")
    private String mealType;

    @NotNull(message = "日期不能为空")
    private LocalDate dietDate;

    @NotNull(message = "摄入量不能为空")
    @DecimalMin(value = "0.01", message = "摄入量必须大于0")
    private BigDecimal quantityG;

    private String remark;
}

