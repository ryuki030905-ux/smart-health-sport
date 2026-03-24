package com.healthsport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthsport.entity.ExerciseDict;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// ExerciseDictMapper：Mapper 接口，处理数据库读写
public interface ExerciseDictMapper extends BaseMapper<ExerciseDict> {
}

