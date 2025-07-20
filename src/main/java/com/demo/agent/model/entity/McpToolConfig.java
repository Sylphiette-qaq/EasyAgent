package com.demo.agent.model.entity;

import lombok.Data;

import java.util.Map;

// 表示整个 JSON 文件
@Data
public class McpToolConfig {
    private Map<String, McpServerProperties> mcpServers;
}