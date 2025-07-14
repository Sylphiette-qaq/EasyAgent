package com.demo.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 大模型信息表
 */
@Data
@TableName("llm_model")
public class LlmModel {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模型名称（如Qwen/QwQ-32B） */
    private String name;

    /** API地址 */
    private String apiUrl;

    /** API Token */
    private String apiToken;

    /** 状态（1:可用 0:禁用） */
    private Integer status;

    /** 模型描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
} 