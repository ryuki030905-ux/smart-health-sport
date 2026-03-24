package com.healthsport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthsport.entity.ExercisePlan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// ExercisePlanMapper：Mapper 接口，处理数据库读写
public interface ExercisePlanMapper extends BaseMapper<ExercisePlan> {
}

