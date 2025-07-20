package com.demo.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.agent.model.entity.LlmModelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * LlmModel表数据库访问层
 */
@Mapper
public interface LlmModelMapper extends BaseMapper<LlmModelEntity> {
} 