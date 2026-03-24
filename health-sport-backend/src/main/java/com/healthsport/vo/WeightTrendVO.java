package com.healthsport.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 体重趋势图返回的数据。
// 里面主要就是日期和体重，前端直接拿去画折线图。
public class WeightTrendVO {

    private List<String> xAxis;

    private List<BigDecimal> series;
}

