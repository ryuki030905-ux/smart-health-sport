package com.healthsport.controller;

import com.healthsport.service.StatisticsService;
import com.healthsport.utils.Result;
import com.healthsport.vo.CalorieBalanceVO;
import com.healthsport.vo.WeeklyExerciseVO;
import com.healthsport.vo.WeightTrendVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/api/v1/statistics")
// StatisticsController：接口层，负责接收请求并返回结果
public class StatisticsController {

    private final StatisticsService statisticsService;

    // 构造方法：把依赖先注入进来
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/weight-trend")
    public Result<WeightTrendVO> weightTrend(
            @RequestParam(defaultValue = "30") @Min(value = 1, message = "days必须>=1") Integer days) {
        return Result.success(statisticsService.weightTrend(days));
    }

    @GetMapping("/calorie-balance")
    public Result<CalorieBalanceVO> calorieBalance(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @Min(value = 1, message = "month必须在1-12") @Max(value = 12, message = "month必须在1-12") Integer month) {
        return Result.success(statisticsService.calorieBalance(year, month));
    }

    @GetMapping("/weekly-exercise")
    public Result<WeeklyExerciseVO> weeklyExercise(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate) {
        return Result.success(statisticsService.weeklyExercise(startDate));
    }
}

