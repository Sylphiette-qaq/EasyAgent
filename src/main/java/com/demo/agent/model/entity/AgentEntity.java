package com.demo.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demo.agent.model.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@TableName("agent")
public class AgentEntity extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 名称 */
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100字符")
    private String name;

    /** 使用的大语言模型id */
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100字符")
    private String llmModelId;

    /** mcp工具列表id用逗号分隔 */
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100字符")
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
