package com.demo.agent.model.entity;

import com.demo.agent.model.base.BaseEntity;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * MCP信息表实体
 */
@Data
public class Mcp extends BaseEntity {
    /** 主键ID */
    private Long id;

    /** 名称 */
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100字符")
    private String name;

    /** 类型 */
    @Size(max = 50, message = "类型不能超过50字符")
    private String type;

    /** 命令 */
    @Size(max = 255, message = "命令不能超过255字符")
    private String command;

    /** 参数 */
    @Size(max = 255, message = "参数不能超过255字符")
    private String args;

    /** URL */
    @Size(max = 255, message = "URL不能超过255字符")
    private String url;

    /** 环境变量Key */
    @Size(max = 100, message = "环境变量Key不能超过100字符")
    private String envKey;

    /** 环境变量Value */
    @Size(max = 255, message = "环境变量Value不能超过255字符")
    private String envValue;

    /** 描述 */
    @Size(max = 255, message = "描述不能超过255字符")
    private String description;

    /** 状态 */
    @Size(max = 20, message = "状态不能超过20字符")
    private String status;
}
