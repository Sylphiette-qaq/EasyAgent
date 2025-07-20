package com.demo.agent.model.response;

import com.demo.agent.model.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * MCP信息表-返回类
 */
@Data
public class McpResponse extends BaseEntity {
    private Long id;
    private String name;
    private String type;
    private String json;
    private Long userId;
    private String description;
    private String status;
} 