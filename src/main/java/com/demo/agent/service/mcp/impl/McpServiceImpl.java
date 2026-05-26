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
import com.demo.agent.tool.McpTypeResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;

@Service
@Slf4j
public class McpServiceImpl extends ServiceImpl<McpMapper, McpEntity> implements McpService {

    /** 建立 TCP/TLS 连接超时（毫秒） */
    private static final int CONNECT_TIMEOUT_MS = 8_000;
    /**
     * 等待响应状态行与头（毫秒）。SSE 端点会长时间推送正文，不能按「读满 body」来计时；
     * 此处只读到 HTTP 状态码后即 disconnect，避免阻塞在流上。
     */
    private static final int READ_TIMEOUT_MS = 18_000;

    /** 日志中隐藏 query 中的 key 等敏感参数 */
    private static String redactUrlForLog(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        return url.replaceAll("([?&]key=)[^&]+", "$1***");
    }

    /**
     * 用 {@link HttpURLConnection} 探测 URL：只取状态码后立即 {@link HttpURLConnection#disconnect()}，
     * 不读取 SSE 正文，避免 RestTemplate/Spring 在 close 时排空流导致长时间阻塞。
     * <p>先 HEAD，失败或非 2xx/3xx 时再 GET（部分网关对 HEAD 响应慢或不支持）。</p>
     */
    private static boolean probeSseEndpointReachable(String url) {
        try {
            if (httpProbe(url, "HEAD")) {
                return true;
            }
        } catch (IOException e) {
            log.debug("SSE HEAD 探测失败: {} — {}", redactUrlForLog(url), e.getMessage());
        }
        try {
            return httpProbe(url, "GET");
        } catch (IOException e) {
            log.info("SSE GET 探测失败: {} — {}", redactUrlForLog(url), e.getMessage());
            return false;
        }
    }

    private static boolean httpProbe(String url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestMethod(method);
        conn.setInstanceFollowRedirects(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Accept", "*/*");
        try {
            int code = conn.getResponseCode();
            // 2xx 成功；3xx 若未跟随完毕也可能落在此，一般跟随后应为 2xx
            return code >= 200 && code < 400;
        } finally {
            conn.disconnect();
        }
    }

    @Override
    public void registerMcp(McpToolConfig config) {
        for (Map.Entry<String, McpServerProperties> entry : config.getMcpServers().entrySet()) {
            String name = entry.getKey();
            McpServerProperties properties = entry.getValue();
            McpEntity mcpEntity = new McpEntity();
            mcpEntity.setType(McpTypeResolver.resolveMcpType(properties));
            mcpEntity.setName(name);
            mcpEntity.setJson(McpJsonTool.parseObjectToJson(properties));
            mcpEntity.setUserId(UserContext.getUserId());
            mcpEntity.setCreateBy(UserContext.getUserId());
            mcpEntity.setUpdateBy(UserContext.getUserId());
            baseMapper.insert(mcpEntity);
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
        if (McpTypeResolver.isStdioOnly(properties)) {
            throw new RuntimeException(String.format(
                    "工具 '%s' 为 stdio 配置（含 command），请使用管理员上传接口 /mcp/uploadAdmin",
                    toolName));
        }

        String url = properties.getUrl();

        if (url == null || url.trim().isEmpty()) {
            throw new RuntimeException(String.format("工具 '%s' 的SSE连接URL不能为空,或用户上传了非sse连接的mcp", toolName));
        }

        boolean ok = probeSseEndpointReachable(url);
        if (!ok) {
            throw new RuntimeException(String.format(
                "工具 '%s' 的SSE连接不可用，URL: %s",
                toolName,
                redactUrlForLog(url)));
        }
        log.info("SSE连接校验成功 - 工具: {} — URL: {}", toolName, redactUrlForLog(url));
    }

}
