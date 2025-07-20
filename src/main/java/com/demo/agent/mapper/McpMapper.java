package com.demo.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.agent.model.entity.McpEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mcp表数据库访问层
 */
@Mapper
public interface McpMapper extends BaseMapper<McpEntity> {
}
