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
@TableName("diet_record")
// 饮食记录表。
// 这个类记的是用户每天实际吃了什么、吃了多少。
// food_dict 更像食物资料表，这个才是真正每天提交上来的记录。
public class DietRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long foodDictId;

    private String mealType;

    private LocalDate dietDate;

    private BigDecimal quantityG;

    private BigDecimal calories;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

