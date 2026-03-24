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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("exercise_plan")
// 运动计划表。
// 用户自己定的计划会存在这里，比如周期、目标时长、目标次数这些。
// 页面上显示的计划进度，也是拿这里的数据去算的。
public class ExercisePlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String planName;

    private String planType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer targetDuration;

    private Integer targetTimes;

    private Integer actualDuration;

    private Integer actualTimes;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

