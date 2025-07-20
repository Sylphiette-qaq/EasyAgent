package com.demo.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import com.demo.agent.model.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 大模型信息表
 */
@Data
@TableName("llm_model")
public class LlmModelEntity extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模型名称（如Qwen/QwQ-32B） */
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称不能超过100字符")
    @TableField(value = "name")
    private String name;

    /** API地址 */
    @NotBlank(message = "API地址不能为空")
    @Size(max = 255, message = "API地址不能超过255字符")
    private String apiUrl;

    /** API Key */
    @NotBlank(message = "API Key不能为空")
    @Size(max = 255, message = "API Key不能超过255字符")
    private String apiKey;

    /** 用户id */
    @NotBlank(message = "用户id不能为空")
    @Size(max = 255, message = "用户id不能超过255字符")
    private Long userId;

    /** 状态（1:可用 0:禁用） */
    @NotBlank(message = "状态不能为空")
    private Integer status;

    /** 模型描述 */
    @Size(max = 255, message = "模型描述不能超过255字符")
    private String description;
} 