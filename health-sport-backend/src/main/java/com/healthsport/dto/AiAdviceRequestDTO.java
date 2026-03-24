package com.healthsport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AI 健康建议请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAdviceRequestDTO {

    /** 建议类型：DIET / EXERCISE / GENERAL */
    @NotBlank(message = "adviceType 不能为空")
    private String adviceType;

    /** 当前体重（kg），用于个性化计算 */
    private BigDecimal weight;

    /** 今日运动时长（分钟） */
    private Integer exerciseMinutesToday;

    /** 今日摄入热量（kcal） */
    private BigDecimal caloriesIntakeToday;

    /** 今日消耗热量（kcal） */
    private BigDecimal caloriesBurnedToday;

    /** 目标（如"减脂"、"增肌"、"保持"） */
    private String goal;

    /** 用户的额外问题或补充说明 */
    private String extraQuestion;

    /** 查询健康记录截止日期（为空则默认今天） */
    private LocalDate recordDate;
}
