package com.healthsport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthsport.dto.HealthRecordSaveDTO;
import com.healthsport.entity.HealthRecord;
import com.healthsport.entity.User;
import com.healthsport.exception.BusinessException;
import com.healthsport.mapper.HealthRecordMapper;
import com.healthsport.mapper.UserMapper;
import com.healthsport.service.HealthRecordService;
import com.healthsport.utils.HealthCalculator;
import com.healthsport.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordMapper healthRecordMapper;
    private final UserMapper userMapper;

    public HealthRecordServiceImpl(HealthRecordMapper healthRecordMapper, UserMapper userMapper) {
        this.healthRecordMapper = healthRecordMapper;
        this.userMapper = userMapper;
    }

    @Override
    public HealthRecord save(HealthRecordSaveDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = requireUser(userId);

        BigDecimal bmi = HealthCalculator.calcBMI(dto.getWeight().doubleValue(), dto.getHeight().doubleValue());
        String status = HealthCalculator.judgeStatus(bmi.doubleValue());
        Double bodyFat = HealthCalculator.calcBodyFat(bmi.doubleValue(), safeAge(user.getAge()), safeGender(user.getGender()));

        HealthRecord record = HealthRecord.builder()
                .userId(userId)
                .recordDate(dto.getRecordDate())
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .systolicBp(dto.getSystolicBp())
                .diastolicBp(dto.getDiastolicBp())
                .bloodSugar(dto.getBloodSugar())
                .heartRate(dto.getHeartRate())
                .bmi(bmi)
                .bodyFat(BigDecimal.valueOf(bodyFat).setScale(2, RoundingMode.HALF_UP))
                .healthStatus(status)
                .remark(dto.getRemark())
                .build();

        healthRecordMapper.insert(record);
        return record;
    }

    @Override
    public IPage<HealthRecord> pageCurrentUser(long current, long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return healthRecordMapper.selectPage(
                new Page<>(current, size),
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getUserId, userId)
                        .orderByDesc(HealthRecord::getRecordDate)
                        .orderByDesc(HealthRecord::getId)
        );
    }

    @Override
    public HealthRecord latest() {
        Long userId = SecurityUtils.getCurrentUserId();
        return healthRecordMapper.selectOne(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getUserId, userId)
                        .orderByDesc(HealthRecord::getRecordDate)
                        .orderByDesc(HealthRecord::getId)
                        .last("limit 1")
        );
    }

    @Override
    public HealthRecord update(Long id, HealthRecordSaveDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        HealthRecord existing = getOwnedRecord(id, userId);
        User user = requireUser(userId);

        BigDecimal bmi = HealthCalculator.calcBMI(dto.getWeight().doubleValue(), dto.getHeight().doubleValue());
        String status = HealthCalculator.judgeStatus(bmi.doubleValue());
        Double bodyFat = HealthCalculator.calcBodyFat(bmi.doubleValue(), safeAge(user.getAge()), safeGender(user.getGender()));

        existing.setRecordDate(dto.getRecordDate());
        existing.setHeight(dto.getHeight());
        existing.setWeight(dto.getWeight());
        existing.setSystolicBp(dto.getSystolicBp());
        existing.setDiastolicBp(dto.getDiastolicBp());
        existing.setBloodSugar(dto.getBloodSugar());
        existing.setHeartRate(dto.getHeartRate());
        existing.setBmi(bmi);
        existing.setBodyFat(BigDecimal.valueOf(bodyFat).setScale(2, RoundingMode.HALF_UP));
        existing.setHealthStatus(status);
        existing.setRemark(dto.getRemark());

        healthRecordMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        HealthRecord existing = getOwnedRecord(id, userId);
        healthRecordMapper.deleteById(existing.getId());
    }

    // 这里只能操作自己的记录
    private HealthRecord getOwnedRecord(Long id, Long userId) {
        HealthRecord record = healthRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "健康记录不存在");
        }
        if (!userId.equals(record.getUserId())) {
            throw new BusinessException(403, "无权操作该记录");
        }
        return record;
    }

    // 当前用户都查不到的话，就直接报错
    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private int safeAge(Integer age) {
        return age == null ? 18 : age;
    }

    private int safeGender(Integer gender) {
        return gender == null ? 0 : (gender == 1 ? 1 : 0);
    }
}

