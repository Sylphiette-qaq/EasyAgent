package com.demo.agent.service.mcp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.agent.model.entity.McpEntity;
import com.demo.agent.model.entity.McpToolConfig;
import org.springframework.stereotype.Service;

@Service
public interface McpService extends IService<McpEntity> {
    void registerMcp(McpToolConfig config);
    
    /**
     * 校验SSE连接的可用性
     * @param config MCP工具配置
     * @throws RuntimeException 当连接不可用时抛出异常
     */
    void validateSseConnections(McpToolConfig config);
}
