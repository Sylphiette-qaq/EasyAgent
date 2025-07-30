package com.demo.agent.model.request;

import com.demo.agent.model.base.BaseEntity;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 文件信息表-请求类
 */
@Data
public class FileInfoRequest extends BaseEntity {
    /** Agent ID */
    private Long agentId;

    /** 用户ID */
    private Long userId;

    /** 原始文件名 */
    @Size(max = 255, message = "原始文件名不能超过255字符")
    private String originalFilename;

    /** 文件描述 */
    private String description;

    /** 状态：0-处理中，1-已处理，2-处理失败 */
    private Integer status;

    /** 错误信息 */
    private String errorMessage;
    
    /** 分页参数 */
    private Integer pageNum;
    private Integer pageSize;
}