package com.demo.agent.model.entity;

import lombok.Data;

import java.util.List;

/**
 * 相似度搜索结果
 */
@Data
public class SimilarityResult {
    private String documentId;
    private String content;
    private double similarity;
    private List<Double> vector;

    public SimilarityResult(String documentId, String content, double similarity, List<Double> vector) {
        this.documentId = documentId;
        this.content = content;
        this.similarity = similarity;
        this.vector = vector;
    }
}
