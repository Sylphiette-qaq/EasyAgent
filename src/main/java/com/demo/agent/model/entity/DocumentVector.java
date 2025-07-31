package com.demo.agent.model.entity;

import lombok.Data;

import java.util.List;

/**
 * 文档向量数据传输对象
 */
@Data
public class DocumentVector {
    private String content;
    private List<Double> vector;
    private String metadata;

    public DocumentVector(String content, List<Double> vector, String metadata) {
        this.content = content;
        this.vector = vector;
        this.metadata = metadata;
    }


}