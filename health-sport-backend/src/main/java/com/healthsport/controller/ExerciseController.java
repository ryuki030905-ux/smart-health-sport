package com.healthsport.controller;

import com.healthsport.dto.ExercisePlanSaveDTO;
import com.healthsport.dto.ExerciseRecordSaveDTO;
import com.healthsport.entity.ExerciseDict;
import com.healthsport.entity.ExercisePlan;
import com.healthsport.entity.ExerciseRecord;
import com.healthsport.service.ExerciseService;
import com.healthsport.utils.Result;
import com.healthsport.vo.ExercisePlanVO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exercise")
// ExerciseController：接口层，负责接收请求并返回结果
public class ExerciseController {

    private final ExerciseService exerciseService;

    // 构造方法：把依赖先注入进来
    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @PostMapping("/records")
    // 这个方法处理一下createRecord逻辑
    public Result<ExerciseRecord> createRecord(@Valid @RequestBody ExerciseRecordSaveDTO dto) {
        return Result.success(exerciseService.saveRecord(dto));
    }

    @GetMapping("/records")
    public Result<List<ExerciseRecord>> listRecordsByDate(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(exerciseService.listRecordsByDate(date));
    }

    @GetMapping("/dict")
    // 查询listDict结果，按条件返回数据，这样写应该没问题
    public Result<List<ExerciseDict>> listDict() {
        return Result.success(exerciseService.listExerciseDict());
    }

    @PostMapping("/plans")
    // 这个方法处理一下createPlan逻辑
    public Result<ExercisePlan> createPlan(@Valid @RequestBody ExercisePlanSaveDTO dto) {
        return Result.success(exerciseService.createPlan(dto));
    }

    @GetMapping("/plans")
    // 查询listPlans结果，按条件返回数据
    public Result<List<ExercisePlanVO>> listPlans() {
        return Result.success(exerciseService.listPlans());
    }
}

