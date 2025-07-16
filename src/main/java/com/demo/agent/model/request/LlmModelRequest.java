package com.demo.agent.model.request;

import com.demo.agent.model.base.BaseEntity;
import lombok.Data;

/**
 * 大模型信息表-请求类
 */
@Data
public class LlmModelRequest extends BaseEntity {
    private String name;
    private String apiUrl;
    private String apiKey;
    private Long userId;
    private Integer status;
    private String description;
} 