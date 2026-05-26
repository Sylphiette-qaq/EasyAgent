package com.demo.agent.service.fileinfo.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.common.UserContext;
import com.demo.agent.mapper.FileInfoMapper;
import com.demo.agent.model.entity.FileInfoEntity;
import com.demo.agent.service.fileinfo.FileInfoService;
import com.demo.agent.service.fileinfo.KnowledgeBaseIngestService;
import com.demo.agent.service.fileinfo.UploadPathService;
import com.demo.agent.service.fileinfo.UploadedFileResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 文件信息表Service实现类
 */
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfoEntity> implements FileInfoService {

    private final UploadPathService uploadPathService;
    private final KnowledgeBaseIngestService knowledgeBaseIngestService;

    public FileInfoServiceImpl(
            UploadPathService uploadPathService,
            KnowledgeBaseIngestService knowledgeBaseIngestService) {
        this.uploadPathService = uploadPathService;
        this.knowledgeBaseIngestService = knowledgeBaseIngestService;
    }

    @Override
    public void saveFileInfo(FileInfoEntity fileInfoEntity) {
        fileInfoEntity.setUserId(UserContext.getUserId());
        fileInfoEntity.setUploadTime(LocalDateTime.now());
        if (fileInfoEntity.getStatus() == null) {
            fileInfoEntity.setStatus(0);
        }
        fileInfoEntity.setCreateBy(UserContext.getUserId());
        fileInfoEntity.setCreatedAt(new Date());
        fileInfoEntity.setUpdateBy(UserContext.getUserId());
        fileInfoEntity.setUpdatedAt(new Date());

        save(fileInfoEntity);
    }

    @Override
    public void updateFileStatus(Long id, Integer status, String errorMessage) {
        FileInfoEntity fileInfo = getById(id);
        if (fileInfo != null) {
            fileInfo.setStatus(status);
            fileInfo.setErrorMessage(errorMessage);
            fileInfo.setUpdateBy(UserContext.getUserId());
            fileInfo.setUpdatedAt(new Date());
            updateById(fileInfo);
        }
    }

    @Override
    public UploadedFileResult saveUploadedFile(MultipartFile file, Long agentId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IOException("文件名不能为空");
        }

        Path agentDir = uploadPathService.ensureAgentDir(agentId);
        String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
        Path targetPath = agentDir.resolve(uniqueFilename).normalize();

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        FileInfoEntity fileInfoEntity = new FileInfoEntity();
        fileInfoEntity.setAgentId(agentId);
        fileInfoEntity.setUserId(UserContext.getUserId());
        fileInfoEntity.setOriginalFilename(originalFilename);
        fileInfoEntity.setFilePath(targetPath.toAbsolutePath().toString());
        fileInfoEntity.setStatus(0);
        saveFileInfo(fileInfoEntity);

        try {
            int chunks = knowledgeBaseIngestService.ingestAgentDirectory(agentId);
            updateFileStatus(fileInfoEntity.getId(), 1, null);
            return new UploadedFileResult(fileInfoEntity.getId(), chunks);
        } catch (Exception e) {
            String err = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            updateFileStatus(fileInfoEntity.getId(), 2, err);
            throw new IOException("向量化失败: " + err, e);
        }
    }
}
