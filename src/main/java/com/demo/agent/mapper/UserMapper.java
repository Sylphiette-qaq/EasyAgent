package com.demo.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.agent.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User表数据库访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
} 