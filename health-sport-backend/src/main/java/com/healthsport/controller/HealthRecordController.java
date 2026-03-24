package com.healthsport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.healthsport.dto.HealthRecordSaveDTO;
import com.healthsport.entity.HealthRecord;
import com.healthsport.service.HealthRecordService;
import com.healthsport.utils.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/health/records")
// HealthRecordController：接口层，负责接收请求并返回结果
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    // 构造方法：把依赖先注入进来，这样写应该没问题
    public HealthRecordController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @PostMapping
    // 这个方法处理一下create逻辑
    public Result<HealthRecord> create(@Valid @RequestBody HealthRecordSaveDTO dto) {
        return Result.success(healthRecordService.save(dto));
    }

    @GetMapping
    public Result<IPage<HealthRecord>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "current必须>=1") long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size必须>=1") long size) {
        return Result.success(healthRecordService.pageCurrentUser(current, size));
    }

    @GetMapping("/latest")
    // 这个方法处理一下latest逻辑
    public Result<HealthRecord> latest() {
        return Result.success(healthRecordService.latest());
    }

    @PutMapping("/{id}")
    // 更新数据，先校验再落库
    public Result<HealthRecord> update(@PathVariable Long id, @Valid @RequestBody HealthRecordSaveDTO dto) {
        return Result.success(healthRecordService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    // 删除数据，这里处理删除流程
    public Result<Void> delete(@PathVariable Long id) {
        healthRecordService.delete(id);
        return Result.success(null);
    }
}

