package com.demo.agent.tool;

import java.util.Locale;

/**
 * 解析 stdio MCP 可执行命令，避免硬编码平台路径。
 */
public final class McpCommandResolver {

    private McpCommandResolver() {
    }

    /**
     * 将配置中的 command（如 npx、node）解析为当前 OS 可执行名，依赖 PATH 查找。
     * 已是绝对/相对路径时原样返回。
     */
    public static String resolveStdioExecutable(String command) {
        if (command == null || command.isBlank()) {
            return command;
        }
        String name = command.trim();
        if (name.contains("/") || name.contains("\\")) {
            return name;
        }
        if (isWindows()) {
            return name + ".cmd";
        }
        return name;
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return os.contains("win");
    }
}
