package com.demo.agent.tool;

import com.demo.agent.common.Eums;
import com.demo.agent.model.entity.McpServerProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class McpTypeResolver {

    private McpTypeResolver() {
    }

    /**
     * 根据显式 type、command、url 推断 MCP 传输类型 code。
     */
    public static Integer resolveMcpType(McpServerProperties properties) {
        if (properties == null) {
            return Eums.McpTypeEnum.SSE.getCode();
        }

        String explicitType = properties.getType();
        if (explicitType != null && !explicitType.isBlank()) {
            Integer code = Eums.McpTypeEnum.getCodeByDescription(explicitType.trim());
            if (code != null) {
                return code;
            }
        }

        String command = properties.getCommand();
        if (command != null && !command.isBlank()) {
            return Eums.McpTypeEnum.STDIO.getCode();
        }

        String url = properties.getUrl();
        if (url != null && !url.isBlank()) {
            return Eums.McpTypeEnum.SSE.getCode();
        }

        log.warn("无法从 MCP 配置推断类型，默认使用 SSE: type={}, command={}, url={}",
                explicitType, command, url);
        return Eums.McpTypeEnum.SSE.getCode();
    }

    /** stdio：有 command 且无可用 url */
    public static boolean isStdioOnly(McpServerProperties properties) {
        if (properties == null) {
            return false;
        }
        String command = properties.getCommand();
        boolean hasCommand = command != null && !command.isBlank();
        String url = properties.getUrl();
        boolean hasUrl = url != null && !url.isBlank();
        if (!hasCommand) {
            return false;
        }
        Integer resolved = resolveMcpType(properties);
        return Eums.McpTypeEnum.STDIO.getCode().equals(resolved) && !hasUrl;
    }
}
