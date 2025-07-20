package com.demo.agent.service.mcp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.agent.model.entity.McpEntity;
import com.demo.agent.model.entity.McpToolConfig;
import org.springframework.stereotype.Service;

@Service
public interface McpService extends IService<McpEntity> {
    void registerMcp(McpToolConfig config);
}
