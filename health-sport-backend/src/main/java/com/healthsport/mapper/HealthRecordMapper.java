package com.healthsport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthsport.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// HealthRecordMapper：Mapper 接口，处理数据库读写
public interface HealthRecordMapper extends BaseMapper<HealthRecord> {
}

