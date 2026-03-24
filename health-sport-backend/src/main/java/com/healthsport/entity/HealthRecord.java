package com.healthsport.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("health_record")
// 健康档案记录表。
// 一条数据就是某一天录入的一次健康记录。
// 身高、体重、血压、BMI 这些都在这里，后面画趋势图时也经常会用到这张表。
public class HealthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate recordDate;

    private BigDecimal height;

    private BigDecimal weight;

    private Integer systolicBp;

    private Integer diastolicBp;

    private BigDecimal bloodSugar;

    private Integer heartRate;

    private BigDecimal bmi;

    private BigDecimal bodyFat;

    private String healthStatus;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

