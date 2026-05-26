package com.demo.agent.service.mongodb;

import com.demo.agent.model.entity.SimilarityResult;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 向量相似度计算服务
 * 提供余弦相似度计算算法的应用层实现
 */
@Service
public class VectorSimilarityService {

    /**
     * 计算余弦相似度
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 余弦相似度值 (0-1之间，1表示完全相似)
     */
    public double cosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("向量维度必须相同");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += Math.pow(vector1.get(i), 2);
            norm2 += Math.pow(vector2.get(i), 2);
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    

    
    /**
     * 在向量列表中搜索最相似的向量
     * @param queryVector 查询向量
     * @param candidateVectors 候选向量列表
     * @param topK 返回前K个最相似的结果
     * @return 相似度搜索结果列表
     */
    public List<SimilarityResult> searchSimilarVectors(
            List<Double> queryVector, 
            List<SimilarityResult> candidateVectors, 
            int topK) {
        
        List<SimilarityResult> results = new ArrayList<>();
        
        for (SimilarityResult candidate : candidateVectors) {
            if (candidate.getVector() == null || candidate.getVector().isEmpty()) {
                continue;
            }
            double similarity = cosineSimilarity(queryVector, candidate.getVector());
            candidate.setSimilarity(similarity);
            results.add(candidate);
        }

        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 余弦相似度：值越大越相似
        results.sort(Comparator.comparingDouble(SimilarityResult::getSimilarity).reversed());
        
        // 返回前topK个结果
        return results.subList(0, Math.min(topK, results.size()));
    }
    
    /**
     * 批量计算向量与查询向量的余弦相似度
     * @param queryVector 查询向量
     * @param vectors 向量列表
     * @return 相似度列表
     */
    public List<Double> batchCalculateSimilarity(
            List<Double> queryVector, 
            List<List<Double>> vectors) {
        
        List<Double> similarities = new ArrayList<>();
        for (List<Double> vector : vectors) {
            double similarity = cosineSimilarity(queryVector, vector);
            similarities.add(similarity);
        }
        
        return similarities;
    }
    
    /**
     * 向量归一化
     * @param vector 原始向量
     * @return 归一化后的向量
     */
    public List<Double> normalizeVector(List<Double> vector) {
        double norm = 0.0;
        for (double value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        
        if (norm == 0.0) {
            return new ArrayList<>(Collections.nCopies(vector.size(), 0.0));
        }
        
        List<Double> normalizedVector = new ArrayList<>();
        for (double value : vector) {
            normalizedVector.add(value / norm);
        }
        
        return normalizedVector;
    }
    
    /**
     * 计算向量的模长
     * @param vector 向量
     * @return 模长
     */
    public double vectorMagnitude(List<Double> vector) {
        double sum = 0.0;
        for (double value : vector) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }
}