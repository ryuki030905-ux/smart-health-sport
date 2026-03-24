package com.healthsport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthsport.dto.ExercisePlanSaveDTO;
import com.healthsport.dto.ExerciseRecordSaveDTO;
import com.healthsport.entity.ExerciseDict;
import com.healthsport.entity.ExercisePlan;
import com.healthsport.entity.ExerciseRecord;
import com.healthsport.entity.HealthRecord;
import com.healthsport.exception.BusinessException;
import com.healthsport.mapper.ExerciseDictMapper;
import com.healthsport.mapper.ExercisePlanMapper;
import com.healthsport.mapper.ExerciseRecordMapper;
import com.healthsport.mapper.HealthRecordMapper;
import com.healthsport.service.ExerciseService;
import com.healthsport.utils.HealthCalculator;
import com.healthsport.utils.SecurityUtils;
import com.healthsport.vo.ExercisePlanVO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRecordMapper exerciseRecordMapper;
    private final ExerciseDictMapper exerciseDictMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final ExercisePlanMapper exercisePlanMapper;

    public ExerciseServiceImpl(ExerciseRecordMapper exerciseRecordMapper,
                               ExerciseDictMapper exerciseDictMapper,
                               HealthRecordMapper healthRecordMapper,
                               ExercisePlanMapper exercisePlanMapper) {
        this.exerciseRecordMapper = exerciseRecordMapper;
        this.exerciseDictMapper = exerciseDictMapper;
        this.healthRecordMapper = healthRecordMapper;
        this.exercisePlanMapper = exercisePlanMapper;
    }

    @Override
    public ExerciseRecord saveRecord(ExerciseRecordSaveDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        ExerciseDict exerciseDict = exerciseDictMapper.selectById(dto.getExerciseDictId());
        // 先看看选的运动项目存不存在
        if (exerciseDict == null) {
            throw new BusinessException(404, "运动项目不存在");
        }

        HealthRecord latestHealth = healthRecordMapper.selectOne(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getUserId, userId)
                        .orderByDesc(HealthRecord::getRecordDate)
                        .orderByDesc(HealthRecord::getId)
                        .last("limit 1")
        );
        // 没有体重的话，这里就没法算消耗热量
        if (latestHealth == null || latestHealth.getWeight() == null) {
            throw new BusinessException(400, "请先完善健康记录体重数据");
        }

        String intensity = normalizeIntensity(dto.getIntensity());
        BigDecimal calories = HealthCalculator.calcCalories(
                exerciseDict.getMetValue(),
                latestHealth.getWeight(),
                dto.getDurationMin(),
                intensity
        );

        ExerciseRecord record = ExerciseRecord.builder()
                .userId(userId)
                .exerciseDictId(dto.getExerciseDictId())
                .exerciseDate(dto.getExerciseDate())
                .durationMin(dto.getDurationMin())
                .intensity(intensity)
                .caloriesBurned(calories)
                .remark(dto.getRemark())
                .build();
        exerciseRecordMapper.insert(record);
        return record;
    }

    @Override
    public List<ExerciseRecord> listRecordsByDate(LocalDate date) {
        Long userId = SecurityUtils.getCurrentUserId();
        return exerciseRecordMapper.selectList(
                new LambdaQueryWrapper<ExerciseRecord>()
                        .eq(ExerciseRecord::getUserId, userId)
                        .eq(date != null, ExerciseRecord::getExerciseDate, date)
                        .orderByAsc(ExerciseRecord::getId)
        );
    }

    @Override
    @Cacheable(value = "exerciseDict", keyGenerator = "userKeyGenerator")
    public List<ExerciseDict> listExerciseDict() {
        return exerciseDictMapper.selectList(
                new LambdaQueryWrapper<ExerciseDict>()
                        .orderByAsc(ExerciseDict::getId)
        );
    }

    @Override
    public ExercisePlan createPlan(ExercisePlanSaveDTO dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException(400, "结束日期不能早于开始日期");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        ExercisePlan plan = ExercisePlan.builder()
                .userId(userId)
                .planName(dto.getPlanName())
                .planType(StringUtils.hasText(dto.getPlanType()) ? dto.getPlanType().toUpperCase() : "WEEKLY")
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .targetDuration(dto.getTargetDuration())
                .targetTimes(dto.getTargetTimes())
                .actualDuration(0)
                .actualTimes(0)
                .status("ONGOING")
                .build();
        exercisePlanMapper.insert(plan);
        return plan;
    }

    @Override
    public List<ExercisePlanVO> listPlans() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ExercisePlan> plans = exercisePlanMapper.selectList(
                new LambdaQueryWrapper<ExercisePlan>()
                        .eq(ExercisePlan::getUserId, userId)
                        .orderByDesc(ExercisePlan::getCreateTime)
                        .orderByDesc(ExercisePlan::getId)
        );
        return plans.stream().map(plan -> ExercisePlanVO.builder()
                .id(plan.getId())
                .planName(plan.getPlanName())
                .planType(plan.getPlanType())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .targetDuration(plan.getTargetDuration())
                .targetTimes(plan.getTargetTimes())
                .actualDuration(plan.getActualDuration())
                .actualTimes(plan.getActualTimes())
                .status(plan.getStatus())
                .progressRate(calcProgressRate(plan.getActualDuration(), plan.getTargetDuration()))
                .build()).toList();
    }

    // 强度统一转成大写，顺手校验一下范围
    private String normalizeIntensity(String intensity) {
        if (!StringUtils.hasText(intensity)) {
            return "MEDIUM";
        }
        String value = intensity.toUpperCase();
        if (!"LOW".equals(value) && !"MEDIUM".equals(value) && !"HIGH".equals(value)) {
            throw new BusinessException(400, "intensity仅支持LOW/MEDIUM/HIGH");
        }
        return value;
    }

    // 算计划完成百分比
    private BigDecimal calcProgressRate(Integer actualDuration, Integer targetDuration) {
        if (targetDuration == null || targetDuration <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        int actual = actualDuration == null ? 0 : actualDuration;
        return BigDecimal.valueOf(actual)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(targetDuration), 2, RoundingMode.HALF_UP);
    }
}

