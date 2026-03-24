package com.healthsport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthsport.entity.ExerciseRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// ExerciseRecordMapper：Mapper 接口，处理数据库读写
public interface ExerciseRecordMapper extends BaseMapper<ExerciseRecord> {
}

