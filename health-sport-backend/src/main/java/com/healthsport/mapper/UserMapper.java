package com.healthsport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthsport.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// UserMapper：Mapper 接口，处理数据库读写
public interface UserMapper extends BaseMapper<User> {
}

