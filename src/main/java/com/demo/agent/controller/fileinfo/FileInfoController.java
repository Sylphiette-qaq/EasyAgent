package com.demo.agent.controller.fileinfo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.agent.common.Result;
import com.demo.agent.common.UserContext;
import com.demo.agent.model.entity.FileInfoEntity;
import com.demo.agent.model.request.FileInfoRequest;
import com.demo.agent.model.response.FileInfoResponse;
import com.demo.agent.config.FileUploadProperties;
import com.demo.agent.service.fileinfo.FileInfoService;
import com.demo.agent.service.fileinfo.UploadedFileResult;
import com.demo.agent.service.agent.AgentService;
import com.demo.agent.model.entity.AgentEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 文件信息表Controller
 */
@RestController
@RequestMapping("/fileinfo")
public class FileInfoController {
    
    @Autowired
    private FileInfoService fileInfoService;

    @Autowired
    private FileUploadProperties fileUploadProperties;

    @Autowired
    private AgentService agentService;


    /** 新增文件信息 */
    @PostMapping
    public Result<FileInfoResponse> add(@RequestBody FileInfoRequest req) {
        FileInfoEntity fileInfoEntity = new FileInfoEntity();
        BeanUtils.copyProperties(req, fileInfoEntity);
        fileInfoService.saveFileInfo(fileInfoEntity);
        FileInfoResponse resp = new FileInfoResponse();
        BeanUtils.copyProperties(fileInfoEntity, resp);
        return Result.success(resp);
    }

    /** 根据ID查询文件信息 */
    @GetMapping("/{id}")
    public Result<FileInfoResponse> getById(@PathVariable Long id) {
        FileInfoEntity fileInfoEntity = fileInfoService.getById(id);
        if (fileInfoEntity == null) {
            return Result.fail("文件信息不存在");
        }
        FileInfoResponse resp = new FileInfoResponse();
        BeanUtils.copyProperties(fileInfoEntity, resp);
        return Result.success(resp);
    }

    /** 更新文件信息 */
    @PutMapping
    public Result<FileInfoResponse> update(@RequestBody FileInfoRequest req, @RequestParam Long id) {
        FileInfoEntity fileInfoEntity = fileInfoService.getById(id);
        if (fileInfoEntity == null) {
            return Result.fail("文件信息不存在");
        }
        BeanUtils.copyProperties(req, fileInfoEntity);
        fileInfoEntity.setId(id);
        fileInfoService.updateById(fileInfoEntity);
        FileInfoResponse resp = new FileInfoResponse();
        BeanUtils.copyProperties(fileInfoEntity, resp);
        return Result.success(resp);
    }

    /** 删除文件信息 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        FileInfoEntity fileInfoEntity = fileInfoService.getById(id);
        if (fileInfoEntity == null) {
            return Result.fail("文件信息不存在");
        }
        return Result.success(fileInfoService.removeById(id));
    }

    /** 查询所有文件信息 */
    @GetMapping
    public Result<List<FileInfoResponse>> list() {
        List<FileInfoEntity> list = fileInfoService.list();
        List<FileInfoResponse> respList = list.stream().map(entity -> {
            FileInfoResponse resp = new FileInfoResponse();
            BeanUtils.copyProperties(entity, resp);
            return resp;
        }).collect(Collectors.toList());
        return Result.success(respList);
    }

    /** 分页条件查询文件信息 */
    @PostMapping("/page")
    public Result<IPage<FileInfoResponse>> page(@RequestBody FileInfoRequest query) {
        LambdaQueryWrapper<FileInfoEntity> wrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (query.getAgentId() != null) {
            wrapper.eq(FileInfoEntity::getAgentId, query.getAgentId());
        }
        if (query.getUserId() != null) {
            wrapper.eq(FileInfoEntity::getUserId, query.getUserId());
        }
        if (query.getOriginalFilename() != null && !query.getOriginalFilename().isEmpty()) {
            wrapper.like(FileInfoEntity::getOriginalFilename, query.getOriginalFilename());
        }
        if (query.getStatus() != null) {
            wrapper.eq(FileInfoEntity::getStatus, query.getStatus());
        }
        
        // 按上传时间倒序排列
        wrapper.orderByDesc(FileInfoEntity::getUploadTime);
        
        Page<FileInfoEntity> page = new Page<>(query.getPageNum() == null ? 1 : query.getPageNum(), 
                                               query.getPageSize() == null ? 10 : query.getPageSize());
        IPage<FileInfoEntity> fileInfoPage = fileInfoService.page(page, wrapper);
        
        IPage<FileInfoResponse> respPage = fileInfoPage.convert(entity -> {
            FileInfoResponse resp = new FileInfoResponse();
            BeanUtils.copyProperties(entity, resp);
            return resp;
        });
        
        return Result.success(respPage);
    }

    /** 获取用户的文件信息列表 */
    @PostMapping("/getUserFiles")
    public Result<IPage<FileInfoResponse>> getUserFiles(@RequestBody FileInfoRequest query) {
        LambdaQueryWrapper<FileInfoEntity> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询当前用户的文件
        Long userId = UserContext.getUserId();
        wrapper.eq(FileInfoEntity::getUserId, userId);
        
        // 条件查询
        if (query.getAgentId() != null) {
            wrapper.eq(FileInfoEntity::getAgentId, query.getAgentId());
        }
        if (query.getOriginalFilename() != null && !query.getOriginalFilename().isEmpty()) {
            wrapper.like(FileInfoEntity::getOriginalFilename, query.getOriginalFilename());
        }
        if (query.getStatus() != null) {
            wrapper.eq(FileInfoEntity::getStatus, query.getStatus());
        }
        
        // 按上传时间倒序排列
        wrapper.orderByDesc(FileInfoEntity::getUploadTime);
        
        Page<FileInfoEntity> page = new Page<>(query.getPageNum() == null ? 1 : query.getPageNum(), 
                                               query.getPageSize() == null ? 10 : query.getPageSize());
        IPage<FileInfoEntity> fileInfoPage = fileInfoService.page(page, wrapper);
        
        IPage<FileInfoResponse> respPage = fileInfoPage.convert(entity -> {
            FileInfoResponse resp = new FileInfoResponse();
            BeanUtils.copyProperties(entity, resp);
            return resp;
        });
        
        return Result.success(respPage);
    }

    /** 根据Agent ID获取文件列表 */
    @GetMapping("/agent/{agentId}")
    public Result<List<FileInfoResponse>> getFilesByAgentId(@PathVariable Long agentId) {
        LambdaQueryWrapper<FileInfoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfoEntity::getAgentId, agentId);
        wrapper.eq(FileInfoEntity::getUserId, UserContext.getUserId());
        wrapper.orderByDesc(FileInfoEntity::getUploadTime);
        
        List<FileInfoEntity> list = fileInfoService.list(wrapper);
        List<FileInfoResponse> respList = list.stream().map(entity -> {
            FileInfoResponse resp = new FileInfoResponse();
            BeanUtils.copyProperties(entity, resp);
            return resp;
        }).collect(Collectors.toList());
        
        return Result.success(respList);
    }

    /** 更新文件处理状态 */
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestParam Long id, 
                                       @RequestParam Integer status, 
                                       @RequestParam(required = false) String errorMessage) {
        fileInfoService.updateFileStatus(id, status, errorMessage);
        return Result.success("状态更新成功");
    }
    
    /** 上传文件 */
    @PostMapping("/uploadFile")
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("agentId") Long agentId,
            @RequestParam(value = "description", required = false) String description) {
        
        try {
            // 1. 验证文件
            if (file.isEmpty()) {
                return Result.fail("文件不能为空");
            }
            
            // 2. 验证文件类型（支持 txt, pdf, docx）
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.fail("文件名不能为空");
            }
            
            String fileExtension = getFileExtension(originalFilename);
            if (!fileUploadProperties.isAllowedExtension(fileExtension)) {
                return Result.fail("不支持的文件类型，仅支持: " + fileUploadProperties.allowedTypesLabel());
            }
            
            // 3. 保存并向量化
            UploadedFileResult uploaded = fileInfoService.saveUploadedFile(file, agentId);

            Map<String, Object> result = new HashMap<>();
            String message;
            if (uploaded.vectorChunks() > 0) {
                message = "文件上传成功，已向量化 " + uploaded.vectorChunks() + " 个文本分块";
            } else {
                message = "文件已保存，向量化未产生有效分块（请检查文件格式与内容）";
            }
            result.put("message", message);
            result.put("fileInfoId", uploaded.fileInfoId());
            result.put("vectorChunks", uploaded.vectorChunks());

            // 若用户已发起过对话，内存中的 Agent 未挂载 RAG，需重建
            agentService.refreshAgentAfterKnowledgeChange(agentId);
            
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.fail("文件上传失败：" + e.getMessage());
        }
    }

    
    /** 获取文件扩展名 */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
    
}