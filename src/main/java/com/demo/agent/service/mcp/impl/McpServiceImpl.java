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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class McpServiceImpl extends ServiceImpl<McpMapper, McpEntity> implements McpService {

    @Override
    public void registerMcp(McpToolConfig config) {
        for (Map.Entry<String, McpServerProperties> entry : config.getMcpServers().entrySet()) {
            String name = entry.getKey();
            McpServerProperties properties = entry.getValue();
            McpEntity mcpEntity = new McpEntity();
            mcpEntity.setType(Eums.McpTypeEnum.SSE.getCode());
            mcpEntity.setName(name);
            mcpEntity.setJson(McpJsonTool.parseObjectToJson(properties));
            mcpEntity.setUserId(UserContext.getUserId());
            mcpEntity.setCreateBy(UserContext.getUserId());
            mcpEntity.setUpdateBy(UserContext.getUserId());
            int id = baseMapper.insert(mcpEntity);
        }
    }

    @Override
    public void validateSseConnections(McpToolConfig config) {
        for (Map.Entry<String, McpServerProperties> entry : config.getMcpServers().entrySet()) {
            String toolName = entry.getKey();
            McpServerProperties properties = entry.getValue();
                validateSseConnection(toolName, properties);
        }
    }

    /**
     * 校验单个SSE连接
     * @param toolName 工具名称
     * @param properties SSE连接配置
     * @throws RuntimeException 当连接不可用时抛出异常
     */
    private void validateSseConnection(String toolName, McpServerProperties properties) {
        String url = properties.getUrl();
        
        if (url == null || url.trim().isEmpty()) {
            throw new RuntimeException(String.format("工具 '%s' 的SSE连接URL不能为空,或用户上传了非sse连接的mcp", toolName));
        }
        
        try {
            // 使用CompletableFuture实现超时控制
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    
                    // 尝试发送HEAD请求检查连接可用性
                    ResponseEntity<String> response = restTemplate.exchange(
                        URI.create(url),
                        HttpMethod.HEAD,
                        null,
                        String.class
                    );
                    
                    // 检查响应状态码
                    HttpStatus statusCode = (HttpStatus) response.getStatusCode();
                    return statusCode.is2xxSuccessful() || statusCode.is3xxRedirection();
                    
                } catch (Exception e) {
                    // 如果HEAD请求失败，尝试GET请求（某些服务器可能不支持HEAD）
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        ResponseEntity<String> response = restTemplate.exchange(
                            URI.create(url),
                            HttpMethod.GET,
                            null,
                            String.class
                        );
                        
                        HttpStatus statusCode = (HttpStatus) response.getStatusCode();
                        return statusCode.is2xxSuccessful();
                        
                    } catch (Exception ex) {
                        log.info(String.format("SSE连接校验失败 - 工具: %s, URL: %s, 错误: %s",
                            toolName, url, ex.getMessage()));
                        return false;
                    }
                }
            });
            
            // 设置10秒超时
            Boolean isConnectable = future.get(10, TimeUnit.SECONDS);
            
            if (!isConnectable) {
                throw new RuntimeException(String.format("工具 '%s' 的SSE连接不可用，URL: %s", toolName, url));
            }
            
            log.info(String.format("SSE连接校验成功 - 工具: %s, URL: %s", toolName, url));
            
        } catch (TimeoutException e) {
            throw new RuntimeException(String.format("工具 '%s' 的SSE连接超时，URL: %s", toolName, url));
        } catch (Exception e) {
            throw new RuntimeException(String.format("工具 '%s' 的SSE连接校验失败: %s", toolName, e.getMessage()));
        }
    }

}
