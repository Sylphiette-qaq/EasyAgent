package com.demo.agent.service.fileinfo;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.agent.model.entity.FileInfoEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * 文件信息表Service接口
 */
public interface FileInfoService extends IService<FileInfoEntity> {
    
    /**
     * 保存文件信息
     * @param fileInfoEntity 文件信息实体
     */
    void saveFileInfo(FileInfoEntity fileInfoEntity);
    
    /**
     * 更新文件处理状态
     * @param id 文件ID
     * @param status 状态
     * @param errorMessage 错误信息（可选）
     */
    void updateFileStatus(Long id, Integer status, String errorMessage);
    
    /**
     * 保存上传的文件
     * @param file 上传的文件
     * @param agentId Agent ID
     * @throws IOException 文件操作异常
     */
    void saveUploadedFile(MultipartFile file, Long agentId) throws IOException;
}