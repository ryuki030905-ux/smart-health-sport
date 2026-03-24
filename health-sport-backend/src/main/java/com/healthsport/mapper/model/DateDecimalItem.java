package com.healthsport.mapper.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
// 统计查询时用的临时结果对象。
// 一般表示“某一天 + 一个小数值”，比如某天体重、某天热量这种数据。
public class DateDecimalItem {

    private LocalDate statDate;

    private BigDecimal value;
}

