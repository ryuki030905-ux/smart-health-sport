package com.healthsport.service.impl;

import com.healthsport.mapper.StatisticsMapper;
import com.healthsport.mapper.model.DateDecimalItem;
import com.healthsport.mapper.model.DateIntegerItem;
import com.healthsport.service.StatisticsService;
import com.healthsport.utils.SecurityUtils;
import com.healthsport.vo.CalorieBalanceVO;
import com.healthsport.vo.WeeklyExerciseVO;
import com.healthsport.vo.WeightTrendVO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final List<String> WEEK_AXIS = List.of("周一", "周二", "周三", "周四", "周五", "周六", "周日");

    private final StatisticsMapper statisticsMapper;

    public StatisticsServiceImpl(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    @Override
    @Cacheable(value = "weightTrend", keyGenerator = "userKeyGenerator")
    // 查体重趋势数据
    public WeightTrendVO weightTrend(Integer days) {
        int queryDays = (days == null || days <= 0) ? 30 : days;
        Long userId = SecurityUtils.getCurrentUserId();
        List<DateDecimalItem> rows = statisticsMapper.selectWeightTrend(userId, queryDays);

        List<String> xAxis = new ArrayList<>(rows.size());
        List<BigDecimal> series = new ArrayList<>(rows.size());
        for (DateDecimalItem row : rows) {
            xAxis.add(row.getStatDate().format(MONTH_DAY_FORMATTER));
            series.add(row.getValue());
        }

        return WeightTrendVO.builder()
                .xAxis(xAxis)
                .series(series)
                .build();
    }

    @Override
    @Cacheable(value = "calorieBalance", keyGenerator = "userKeyGenerator")
    // 查某个月的热量收支数据
    public CalorieBalanceVO calorieBalance(Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int queryYear = (year == null) ? now.getYear() : year;
        int queryMonth = (month == null) ? now.getMonthValue() : month;

        YearMonth yearMonth = YearMonth.of(queryYear, queryMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Long userId = SecurityUtils.getCurrentUserId();
        List<DateDecimalItem> intakeRows = statisticsMapper.selectIntakeByDate(userId, startDate, endDate);
        List<DateDecimalItem> burnedRows = statisticsMapper.selectBurnedByDate(userId, startDate, endDate);

        Map<LocalDate, BigDecimal> intakeMap = toDecimalMap(intakeRows);
        Map<LocalDate, BigDecimal> burnedMap = toDecimalMap(burnedRows);

        List<String> xAxis = new ArrayList<>();
        List<BigDecimal> intake = new ArrayList<>();
        List<BigDecimal> burned = new ArrayList<>();
        List<BigDecimal> balance = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            BigDecimal intakeValue = intakeMap.getOrDefault(date, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal burnedValue = burnedMap.getOrDefault(date, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal balanceValue = intakeValue.subtract(burnedValue).setScale(2, RoundingMode.HALF_UP);

            xAxis.add(date.format(MONTH_DAY_FORMATTER));
            intake.add(intakeValue);
            burned.add(burnedValue);
            balance.add(balanceValue);
        }

        return CalorieBalanceVO.builder()
                .xAxis(xAxis)
                .intake(intake)
                .burned(burned)
                .balance(balance)
                .build();
    }

    @Override
    @Cacheable(value = "weeklyExercise", keyGenerator = "userKeyGenerator")
    // 查一周的运动时长数据
    public WeeklyExerciseVO weeklyExercise(LocalDate startDate) {
        LocalDate queryStartDate = (startDate == null) ? LocalDate.now().with(DayOfWeek.MONDAY) : startDate;

        Long userId = SecurityUtils.getCurrentUserId();
        List<DateIntegerItem> rows = statisticsMapper.selectWeeklyExerciseDuration(userId, queryStartDate);
        Map<LocalDate, Integer> durationMap = toIntegerMap(rows);

        List<Integer> series = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate date = queryStartDate.plusDays(i);
            series.add(durationMap.getOrDefault(date, 0));
        }

        return WeeklyExerciseVO.builder()
                .xAxis(WEEK_AXIS)
                .series(series)
                .build();
    }

    // 把小数结果转成 map，后面按日期取值会方便一点
    private Map<LocalDate, BigDecimal> toDecimalMap(List<DateDecimalItem> rows) {
        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (DateDecimalItem row : rows) {
            map.put(row.getStatDate(), row.getValue() == null ? BigDecimal.ZERO : row.getValue());
        }
        return map;
    }

    // 把整数结果也转成 map
    private Map<LocalDate, Integer> toIntegerMap(List<DateIntegerItem> rows) {
        Map<LocalDate, Integer> map = new HashMap<>();
        for (DateIntegerItem row : rows) {
            map.put(row.getStatDate(), row.getValue() == null ? 0 : row.getValue());
        }
        return map;
    }
}

