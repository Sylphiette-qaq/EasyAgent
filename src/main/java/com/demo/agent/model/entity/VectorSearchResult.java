package com.demo.agent.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 向量搜索结果
 */
@Data
public class VectorSearchResult {
    private String documentId;
    private String content;
    private double similarity;
    private String metadata;
    private LocalDateTime createdAt;

}