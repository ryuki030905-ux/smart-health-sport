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
// 热量收支图用的返回对象。
// 这里会放摄入、消耗和差值，前端直接拿去画图。
public class CalorieBalanceVO {

    private List<String> xAxis;

    private List<BigDecimal> intake;

    private List<BigDecimal> burned;

    private List<BigDecimal> balance;
}

