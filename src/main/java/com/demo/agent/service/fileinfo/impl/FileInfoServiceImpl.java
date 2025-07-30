package com.demo.agent.service.fileinfo.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.common.UserContext;
import com.demo.agent.mapper.FileInfoMapper;
import com.demo.agent.model.entity.FileInfoEntity;
import com.demo.agent.service.fileinfo.FileInfoService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 文件信息表Service实现类
 */
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfoEntity> implements FileInfoService {

    @Override
    public void saveFileInfo(FileInfoEntity fileInfoEntity) {
        // 设置用户ID
        fileInfoEntity.setUserId(UserContext.getUserId());
        // 设置上传时间
        fileInfoEntity.setUploadTime(LocalDateTime.now());
        // 设置默认状态为处理中
        if (fileInfoEntity.getStatus() == null) {
            fileInfoEntity.setStatus(0);
        }
        // 设置基础字段
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
    public void saveUploadedFile(MultipartFile file, Long agentId) throws IOException {
        // 创建文件存储目录
        String uploadDir = "D:/java-project/EasyAgent/EasyAgent-backend/uploads/agent_" + agentId;
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("文件名不能为空");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;

        // 保存文件
        String filePath = uploadDir + "/" + uniqueFilename;
        File targetFile = new File(filePath);
        file.transferTo(targetFile);

        FileInfoEntity fileInfoEntity = new FileInfoEntity();
        fileInfoEntity.setAgentId(agentId);
        fileInfoEntity.setUserId(UserContext.getUserId());
        fileInfoEntity.setOriginalFilename(originalFilename);
        fileInfoEntity.setFilePath(filePath);
        fileInfoEntity.setStatus(0); // 处理中
        saveFileInfo(fileInfoEntity);

        // 更新文件状态为已处理
        updateFileStatus(fileInfoEntity.getId(), 1, null);
    }
}