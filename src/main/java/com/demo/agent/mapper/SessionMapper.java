package com.demo.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.agent.model.entity.SessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SessionMapper extends BaseMapper<SessionEntity> {
} 