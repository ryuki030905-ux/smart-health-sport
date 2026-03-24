package com.healthsport.service;

import com.healthsport.vo.CalorieBalanceVO;
import com.healthsport.vo.WeeklyExerciseVO;
import com.healthsport.vo.WeightTrendVO;

import java.time.LocalDate;

// StatisticsService：服务接口，先把能力定义清楚
public interface StatisticsService {

    WeightTrendVO weightTrend(Integer days);

    CalorieBalanceVO calorieBalance(Integer year, Integer month);

    WeeklyExerciseVO weeklyExercise(LocalDate startDate);
}

