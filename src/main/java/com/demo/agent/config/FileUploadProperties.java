package com.demo.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 知识库文件上传配置（file.upload.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {

    /**
     * 上传根目录，支持 ${user.home} 等 Spring 占位符
     */
    private String path = "${user.home}/.easyagent/uploads";

    /**
     * 允许扩展名（不含点），如 txt,pdf,docx
     */
    private List<String> allowedTypes = List.of("txt", "pdf", "docx");

    public boolean isAllowedExtension(String extensionWithDot) {
        if (extensionWithDot == null || extensionWithDot.isBlank()) {
            return false;
        }
        String ext = extensionWithDot.startsWith(".")
                ? extensionWithDot.substring(1)
                : extensionWithDot;
        return allowedTypes.stream()
                .anyMatch(t -> t.equalsIgnoreCase(ext.trim()));
    }

    public String allowedTypesLabel() {
        return String.join("、", allowedTypes);
    }

    public static boolean isSupportedKnowledgeFileName(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".txt")
                || lower.endsWith(".md")
                || lower.endsWith(".pdf")
                || lower.endsWith(".docx");
    }
}
