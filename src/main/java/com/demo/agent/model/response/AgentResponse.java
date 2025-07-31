package com.demo.agent.model.response;

import com.demo.agent.model.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Agent信息表-返回类
 */
@Data
public class AgentResponse extends BaseEntity {
    private Long id;
    private String name;
    private Long llmModelId;
    private String mcpIds;
    private String fileIds;
    /** 用户ID */
    private Long userId;
    private String description;
    private String systemPrompt;
    private String status;
}