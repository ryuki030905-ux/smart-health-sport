package com.healthsport.controller;

import com.healthsport.dto.DietRecordSaveDTO;
import com.healthsport.entity.DietRecord;
import com.healthsport.entity.FoodDict;
import com.healthsport.service.DietService;
import com.healthsport.utils.Result;
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
@RequestMapping("/api/v1/diet")
// DietController：接口层，负责接收请求并返回结果
public class DietController {

    private final DietService dietService;

    // 构造方法：把依赖先注入进来
    public DietController(DietService dietService) {
        this.dietService = dietService;
    }

    @PostMapping("/records")
    // 这个方法处理一下createRecord逻辑
    public Result<DietRecord> createRecord(@Valid @RequestBody DietRecordSaveDTO dto) {
        return Result.success(dietService.saveRecord(dto));
    }

    @GetMapping("/records")
    public Result<List<DietRecord>> listByDate(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dietService.listByDate(date));
    }

    @GetMapping("/food")
    // 这个方法处理一下searchFood逻辑，暂时先这样
    public Result<List<FoodDict>> searchFood(@RequestParam(required = false) String keyword) {
        return Result.success(dietService.searchFood(keyword));
    }
}

