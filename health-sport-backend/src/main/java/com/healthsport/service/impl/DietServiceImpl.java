package com.healthsport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthsport.dto.DietRecordSaveDTO;
import com.healthsport.entity.DietRecord;
import com.healthsport.entity.FoodDict;
import com.healthsport.exception.BusinessException;
import com.healthsport.mapper.DietRecordMapper;
import com.healthsport.mapper.FoodDictMapper;
import com.healthsport.service.DietService;
import com.healthsport.utils.SecurityUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DietServiceImpl implements DietService {

    private final DietRecordMapper dietRecordMapper;
    private final FoodDictMapper foodDictMapper;

    public DietServiceImpl(DietRecordMapper dietRecordMapper, FoodDictMapper foodDictMapper) {
        this.dietRecordMapper = dietRecordMapper;
        this.foodDictMapper = foodDictMapper;
    }

    @Override
    public DietRecord saveRecord(DietRecordSaveDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        FoodDict food = foodDictMapper.selectById(dto.getFoodDictId());
        // 先确认这个食物在字典里能查到
        if (food == null) {
            throw new BusinessException(404, "食物不存在");
        }

        BigDecimal calories = dto.getQuantityG()
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                .multiply(food.getCaloriesPer100g())
                .setScale(2, RoundingMode.HALF_UP);

        DietRecord record = DietRecord.builder()
                .userId(userId)
                .foodDictId(dto.getFoodDictId())
                .mealType(dto.getMealType().toUpperCase())
                .dietDate(dto.getDietDate())
                .quantityG(dto.getQuantityG())
                .calories(calories)
                .remark(dto.getRemark())
                .build();
        dietRecordMapper.insert(record);
        return record;
    }

    @Override
    // 按日期查当天饮食记录
    public List<DietRecord> listByDate(LocalDate date) {
        Long userId = SecurityUtils.getCurrentUserId();
        return dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(date != null, DietRecord::getDietDate, date)
                        .orderByAsc(DietRecord::getMealType)
                        .orderByAsc(DietRecord::getId)
        );
    }

    @Override
    // 按关键字搜食物
    @Cacheable(value = "foodDict", keyGenerator = "userKeyGenerator")
    public List<FoodDict> searchFood(String keyword) {
        return foodDictMapper.selectList(
                new LambdaQueryWrapper<FoodDict>()
                        .like(StringUtils.hasText(keyword), FoodDict::getName, keyword)
                        .orderByAsc(FoodDict::getId)
        );
    }
}

