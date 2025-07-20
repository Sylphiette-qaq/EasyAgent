package com.demo.agent.model.request;

import com.demo.agent.model.base.BaseEntity;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * MCP信息表-请求类
 */
@Data
public class McpRequest extends BaseEntity {
    /** 名称 */
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100字符")
    private String name;

    /** 类型 */
    @Size(max = 50, message = "类型不能超过50字符")
    private String type;

    /** JSON配置 */
    @NotBlank(message = "配置不能为空")
    private String json;

    /** 所属的用户id */
    @NotBlank(message = "所属的用户id")
    private Long userId;

    /** 描述 */
    @Size(max = 255, message = "描述不能超过255字符")
    private String description;

    /** 状态 */
    @Size(max = 20, message = "状态不能超过20字符")
    private String status;
} 