package com.demo.agent.model.response;

import com.demo.agent.model.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件信息表-返回类
 */
@Data
public class FileInfoResponse extends BaseEntity {
    /** 主键ID */
    private Long id;

    /** Agent ID */
    private Long agentId;

    /** 用户ID */
    private Long userId;

    /** 原始文件名 */
    private String originalFilename;

    /** 文件存储路径 */
    private String filePath;

    /** 文件描述 */
    private String description;

    /** 上传时间 */
    private LocalDateTime uploadTime;

    /** 状态：0-处理中，1-已处理，2-处理失败 */
    private Integer status;

    /** 错误信息 */
    private String errorMessage;
}