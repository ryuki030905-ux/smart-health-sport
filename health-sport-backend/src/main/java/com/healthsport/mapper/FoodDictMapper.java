package com.healthsport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthsport.entity.FoodDict;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// FoodDictMapper：Mapper 接口，处理数据库读写
public interface FoodDictMapper extends BaseMapper<FoodDict> {
}

