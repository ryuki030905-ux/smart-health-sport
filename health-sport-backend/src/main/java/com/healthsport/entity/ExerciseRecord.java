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
@TableName("exercise_record")
// 运动打卡记录表。
// 这里记的是用户真的做过的一次运动，不是运动项目字典。
// 做了什么、做了多久、强度怎么样、消耗多少热量，都会记在这里。
public class ExerciseRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long exerciseDictId;

    private LocalDate exerciseDate;

    private Integer durationMin;

    private String intensity;

    private BigDecimal caloriesBurned;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

