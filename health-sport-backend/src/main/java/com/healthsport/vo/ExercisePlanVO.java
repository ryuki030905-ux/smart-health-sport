package com.healthsport.vo;

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
// 前端展示运动计划时用的对象。
// 比实体类更偏展示一点，比如会多一个进度百分比这种字段。
public class ExercisePlanVO {

    private Long id;
    private String planName;
    private String planType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer targetDuration;
    private Integer targetTimes;
    private Integer actualDuration;
    private Integer actualTimes;
    private String status;
    private BigDecimal progressRate;
}

