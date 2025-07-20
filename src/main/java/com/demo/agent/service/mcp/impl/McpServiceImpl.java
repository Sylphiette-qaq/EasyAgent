package com.demo.agent.service.mcp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.common.Eums;
import com.demo.agent.common.UserContext;
import com.demo.agent.mapper.McpMapper;
import com.demo.agent.model.entity.McpEntity;
import com.demo.agent.model.entity.McpServerProperties;
import com.demo.agent.model.entity.McpToolConfig;
import com.demo.agent.service.mcp.McpService;
import com.demo.agent.tool.McpJsonTool;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class McpServiceImpl extends ServiceImpl<McpMapper, McpEntity> implements McpService {

    @Override
    public void registerMcp(McpToolConfig config) {
        for (Map.Entry<String, McpServerProperties> entry : config.getMcpServers().entrySet()) {
            String name = entry.getKey();
            McpServerProperties properties = entry.getValue();
            McpEntity mcpEntity = new McpEntity();
            mcpEntity.setType(Eums.McpTypeEnum.getCodeByDescription(properties.getType()));
            mcpEntity.setName(name);
            mcpEntity.setJson(McpJsonTool.parseObjectToJson(properties));
            mcpEntity.setUserId(UserContext.getUserId());
            mcpEntity.setCreateBy(UserContext.getUserId());
            mcpEntity.setUpdateBy(UserContext.getUserId());
            baseMapper.insert(mcpEntity);
        }
    }

}
