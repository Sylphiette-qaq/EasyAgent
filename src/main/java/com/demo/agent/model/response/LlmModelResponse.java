package com.demo.agent.model.response;

import com.demo.agent.model.base.BaseEntity;
import lombok.Data;

/**
 * 大模型信息表-返回类
 */
@Data
public class LlmModelResponse extends BaseEntity {
    private Long id;
    private String name;
    private String apiUrl;
    private String apiKey;
    private Long userId;
    private Integer status;
    private String description;
} 