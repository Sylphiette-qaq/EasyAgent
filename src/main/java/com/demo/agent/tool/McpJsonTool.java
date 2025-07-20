package com.demo.agent.tool;

import com.demo.agent.model.entity.McpServerProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class McpJsonTool {

    public static McpServerProperties parseJsonToObject(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        McpServerProperties config;
        try {
            config = objectMapper.readValue(json, McpServerProperties.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Mcp配置json字符串转换为对象失败");
        }
        return config;
    }

    public static String parseObjectToJson(McpServerProperties mcpServerProperties) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(mcpServerProperties);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Mcp配置转换为json字符串失败");
        }
        return jsonString;
    }
}
