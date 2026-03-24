package com.healthsport.mapper.model;

import lombok.Data;

import java.time.LocalDate;

@Data
// 统计查询时用的临时结果对象。
// 一般表示“某一天 + 一个整数值”，比如某天的运动分钟数。
public class DateIntegerItem {

    private LocalDate statDate;

    private Integer value;
}

