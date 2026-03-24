package com.healthsport.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

// 常用的健康计算放这里
public class HealthCalculator {

    private static final BigDecimal INTENSITY_LOW = new BigDecimal("0.8");
    private static final BigDecimal INTENSITY_MEDIUM = BigDecimal.ONE;
    private static final BigDecimal INTENSITY_HIGH = new BigDecimal("1.2");

    private HealthCalculator() {
    }

    // 算 BMI
    public static BigDecimal calcBMI(double weight, double heightCm) {
        if (weight <= 0 || heightCm <= 0) {
            throw new IllegalArgumentException("体重和身高必须大于0");
        }
        double heightMeter = heightCm / 100.0;
        double bmi = weight / (heightMeter * heightMeter);
        return BigDecimal.valueOf(bmi).setScale(2, RoundingMode.HALF_UP);
    }

    // 按 BMI 判断体重状态
    public static String judgeStatus(double bmi) {
        if (bmi < 18.5) {
            return "UNDERWEIGHT";
        }
        if (bmi < 24) {
            return "NORMAL";
        }
        if (bmi < 28) {
            return "OVERWEIGHT";
        }
        return "OBESE";
    }

    // 简单估一下体脂率
    public static Double calcBodyFat(double bmi, int age, int gender) {
        int genderFactor = gender == 1 ? 1 : 0;
        double bodyFat = 1.20 * bmi + 0.23 * age - 10.8 * genderFactor - 5.4;
        double finalValue = Math.max(0, bodyFat);
        return BigDecimal.valueOf(finalValue).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // 算运动消耗热量
    public static BigDecimal calcCalories(BigDecimal met, BigDecimal weight, Integer durationMin, String intensity) {
        if (met == null || weight == null || durationMin == null || durationMin <= 0) {
            throw new IllegalArgumentException("MET、体重、时长参数非法");
        }
        BigDecimal durationHour = BigDecimal.valueOf(durationMin).divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);
        BigDecimal factor = switch ((intensity == null ? "MEDIUM" : intensity).toUpperCase()) {
            case "LOW" -> INTENSITY_LOW;
            case "HIGH" -> INTENSITY_HIGH;
            default -> INTENSITY_MEDIUM;
        };
        return met.multiply(weight)
                .multiply(durationHour)
                .multiply(factor)
                .setScale(2, RoundingMode.HALF_UP);
    }
}

