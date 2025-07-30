package com.demo.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demo.agent.annotation.CustomId;
import com.demo.agent.model.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件信息表实体类
 */
@Data
@TableName("file_info")
public class FileInfoEntity extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @CustomId(length = 16)
    private Long id;

    /** Agent ID */
    @NotNull(message = "Agent ID不能为空")
    private Long agentId;

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 原始文件名 */
    @NotBlank(message = "原始文件名不能为空")
    @Size(max = 255, message = "原始文件名不能超过255字符")
    private String originalFilename;

    /** 文件存储路径 */
    @NotBlank(message = "文件存储路径不能为空")
    @Size(max = 500, message = "文件存储路径不能超过500字符")
    private String filePath;

    /** 文件描述 */
    private String description;

    /** 上传时间 */
    @NotNull(message = "上传时间不能为空")
    private LocalDateTime uploadTime;

    /** 状态：0-处理中，1-已处理，2-处理失败 */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 错误信息 */
    private String errorMessage;
}