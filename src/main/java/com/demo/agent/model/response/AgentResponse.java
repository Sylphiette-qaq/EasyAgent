package com.demo.agent.model.response;

import com.demo.agent.model.base.BaseEntity;
import lombok.Data;

/**
 * Agent信息表-返回类
 */
@Data
public class AgentResponse extends BaseEntity {
    private Long id;
    private String name;
    private String llmModelId;
    private String mcpIds;
    /** 用户ID */
    private Long userId;
    private String description;
    private String status;
} 