package com.demo.agent.service.fileinfo;

import com.demo.agent.config.FileUploadProperties;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 解析知识库文件在磁盘上的存储路径
 */
@Service
public class UploadPathService {

    private final FileUploadProperties fileUploadProperties;

    public UploadPathService(FileUploadProperties fileUploadProperties) {
        this.fileUploadProperties = fileUploadProperties;
    }

    public Path resolveBasePath() {
        return Paths.get(fileUploadProperties.getPath()).toAbsolutePath().normalize();
    }

    public Path resolveAgentDir(Long agentId) {
        return resolveBasePath().resolve("agent_" + agentId);
    }

    public Path ensureAgentDir(Long agentId) {
        Path agentDir = resolveAgentDir(agentId);
        try {
            Files.createDirectories(agentDir);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("无法创建上传目录: " + agentDir, e);
        }
        return agentDir;
    }
}
