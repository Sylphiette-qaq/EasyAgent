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


    public SimilarityResult(String id, String content, double v, List<Double> vector) {
    }
}