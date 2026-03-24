package com.healthsport.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthsport.entity.FoodDict;
import com.healthsport.exception.BusinessException;
import com.healthsport.mapper.FoodDictMapper;
import com.healthsport.utils.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/admin/food-dict")
// AdminFoodDictController：管理端接口，处理后台相关请求
public class AdminFoodDictController {

    private final FoodDictMapper foodDictMapper;

    // 构造方法：把依赖先注入进来
    public AdminFoodDictController(FoodDictMapper foodDictMapper) {
        this.foodDictMapper = foodDictMapper;
    }

    @GetMapping
    // 查询list结果，按条件返回数据
    public Result<List<FoodDict>> list() {
        List<FoodDict> list = foodDictMapper.selectList(
                new LambdaQueryWrapper<FoodDict>().orderByAsc(FoodDict::getId));
        return Result.success(list);
    }

    @PostMapping
    // 这个方法处理一下create逻辑
    public Result<FoodDict> create(@Valid @RequestBody FoodDict body) {
        body.setId(null);
        // 这里判断一下空值，避免后面空指针
        if (body.getIsDeleted() == null) {
            body.setIsDeleted(0);
        }
        foodDictMapper.insert(body);
        return Result.success(body);
    }

    @PutMapping("/{id}")
    // 更新数据，先校验再落库
    public Result<FoodDict> update(@PathVariable Long id, @Valid @RequestBody FoodDict body) {
        FoodDict existing = requireFood(id);
        body.setId(existing.getId());
        body.setIsDeleted(existing.getIsDeleted());
        foodDictMapper.updateById(body);
        return Result.success(foodDictMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    // 删除数据，这里处理删除流程
    public Result<Void> delete(@PathVariable @NotNull Long id) {
        requireFood(id);
        foodDictMapper.deleteById(id);
        return Result.success(null);
    }

    // 这个方法处理一下requireFood逻辑
    private FoodDict requireFood(Long id) {
        FoodDict existing = foodDictMapper.selectById(id);
        // 这里判断一下空值，避免后面空指针
        if (existing == null) {
            throw new BusinessException(404, "食物字典数据不存在");
        }
        return existing;
    }
}

