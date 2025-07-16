package com.demo.agent.model.request;

import com.demo.agent.model.base.BaseEntity;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Agent信息表-请求类
 */
@Data
public class AgentRequest extends BaseEntity {
    /** 名称 */
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100字符")
    private String name;

    /** 使用的大语言模型id */
    @NotBlank(message = "大语言模型id不能为空")
    @Size(max = 100, message = "大语言模型id不能超过100字符")
    private String llmModelId;

    /** mcp工具列表id用逗号分隔 */
    @NotBlank(message = "mcp工具列表id不能为空")
    @Size(max = 100, message = "mcp工具列表id不能超过100字符")
    private String mcpIds;

    /** 用户ID */
    private Long userId;

    /** 描述 */
    @Size(max = 255, message = "描述不能超过255字符")
    private String description;

    /** 状态 */
    @Size(max = 20, message = "状态不能超过20字符")
    private String status;
} 