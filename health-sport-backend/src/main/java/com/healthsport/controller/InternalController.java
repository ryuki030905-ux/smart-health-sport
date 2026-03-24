package com.healthsport.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthsport.entity.ExerciseRecord;
import com.healthsport.entity.HealthRecord;
import com.healthsport.entity.DietRecord;
import com.healthsport.mapper.ExerciseRecordMapper;
import com.healthsport.mapper.HealthRecordMapper;
import com.healthsport.mapper.DietRecordMapper;
import com.healthsport.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内部 Tool API —— 仅供本机 AI Agent 回调使用。
 * 此接口不经过 Spring Security 认证层，
 * 通过 IP 来源限制（仅 127.0.0.1 / localhost）保障安全。
 */
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final HealthRecordMapper healthRecordMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final DietRecordMapper dietRecordMapper;

    public InternalController(HealthRecordMapper healthRecordMapper,
                              ExerciseRecordMapper exerciseRecordMapper,
                              DietRecordMapper dietRecordMapper) {
        this.healthRecordMapper = healthRecordMapper;
        this.exerciseRecordMapper = exerciseRecordMapper;
        this.dietRecordMapper = dietRecordMapper;
    }

    /**
     * 查询用户健康记录（体重等）
     * GET /internal/health?userId=xxx&date=2026-03-24
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> getHealth(
            HttpServletRequest request,
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        checkLocalhost(request);
        LocalDate queryDate = (date != null) ? date : LocalDate.now();

        HealthRecord record = healthRecordMapper.selectOne(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getUserId, userId)
                        .eq(HealthRecord::getRecordDate, queryDate)
                        .orderByDesc(HealthRecord::getId)
                        .last("limit 1")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("date", queryDate);
        result.put("record", record);
        return Result.success(result);
    }

    /**
     * 查询用户今日运动总时长（分钟）
     * GET /internal/exercise?userId=xxx&date=2026-03-24
     */
    @GetMapping("/exercise")
    public Result<Map<String, Object>> getExercise(
            HttpServletRequest request,
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        checkLocalhost(request);
        LocalDate queryDate = (date != null) ? date : LocalDate.now();

        List<ExerciseRecord> records = exerciseRecordMapper.selectList(
                new LambdaQueryWrapper<ExerciseRecord>()
                        .eq(ExerciseRecord::getUserId, userId)
                        .eq(ExerciseRecord::getExerciseDate, queryDate)
        );

        int totalMinutes = records.stream()
                .mapToInt(r -> r.getDurationMin() != null ? r.getDurationMin() : 0)
                .sum();

        BigDecimal totalCalories = records.stream()
                .map(r -> r.getCaloriesBurned() != null ? r.getCaloriesBurned() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("date", queryDate);
        result.put("totalMinutes", totalMinutes);
        result.put("totalCalories", totalCalories);
        result.put("recordCount", records.size());
        return Result.success(result);
    }

    /**
     * 查询用户今日饮食总摄入热量
     * GET /internal/diet?userId=xxx&date=2026-03-24
     */
    @GetMapping("/diet")
    public Result<Map<String, Object>> getDiet(
            HttpServletRequest request,
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        checkLocalhost(request);
        LocalDate queryDate = (date != null) ? date : LocalDate.now();

        List<DietRecord> records = dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getDietDate, queryDate)
        );

        BigDecimal totalCalories = records.stream()
                .map(r -> r.getCalories() != null ? r.getCalories() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("date", queryDate);
        result.put("totalCalories", totalCalories);
        result.put("recordCount", records.size());
        return Result.success(result);
    }

    /**
     * 仅允许本机调用，防止外部恶意请求
     */
    private void checkLocalhost(HttpServletRequest request) {
        assertLocalRequest(request.getRemoteAddr());
    }

    private static void assertLocalRequest(String remoteAddr) {
        if (remoteAddr == null) {
            throw new SecurityException("仅允许本机访问 internal 接口");
        }

        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return;
        }

        throw new SecurityException("仅允许本机访问 internal 接口");
    }
}
