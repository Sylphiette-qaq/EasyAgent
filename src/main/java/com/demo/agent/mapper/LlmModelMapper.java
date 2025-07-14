package com.demo.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.agent.entity.LlmModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * LlmModel表数据库访问层
 */
@Mapper
public interface LlmModelMapper extends BaseMapper<LlmModel> {
} 