package com.healthsport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthsport.entity.DietRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// DietRecordMapper：Mapper 接口，处理数据库读写
public interface DietRecordMapper extends BaseMapper<DietRecord> {
}

