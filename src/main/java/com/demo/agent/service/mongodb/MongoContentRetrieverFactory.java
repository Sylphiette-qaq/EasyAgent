package com.demo.agent.service.mongodb;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MongoDB内容检索器工厂类
 * 用于创建每个Agent对应的MongoContentRetriever实例
 * 解决MongoContentRetriever无法使用@Bean注解但需要依赖注入的问题
 */
@Component
public class MongoContentRetrieverFactory {

    @Autowired
    private MongoVectorStoreService mongoVectorStoreService;

    @Autowired
    private VectorSimilarityService vectorSimilarityService;

    /**
     * 创建MongoContentRetriever实例（使用默认配置）
     * @param embeddingModel 嵌入模型
     * @param agentId Agent ID
     * @return MongoContentRetriever实例
     */
    public MongoContentRetriever createRetriever(EmbeddingModel embeddingModel, String agentId) {
        return new MongoContentRetriever(
            embeddingModel, 
            agentId, 
            mongoVectorStoreService, 
            vectorSimilarityService
        );
    }

    /**
     * 创建MongoContentRetriever实例（自定义配置）
     * @param embeddingModel 嵌入模型
     * @param agentId Agent ID
     * @param maxResults 最大返回结果数
     * @param similarityThreshold 相似度阈值
     * @param similarityType 相似度类型
     * @return MongoContentRetriever实例
     */
    public MongoContentRetriever createRetriever(EmbeddingModel embeddingModel, String agentId,
                                                 int maxResults, double similarityThreshold, 
                                                 String similarityType) {
        return new MongoContentRetriever(
            embeddingModel, 
            agentId, 
            maxResults, 
            similarityThreshold, 
            similarityType,
            mongoVectorStoreService, 
            vectorSimilarityService
        );
    }

    /**
     * 创建MongoContentRetriever实例（部分自定义配置）
     * @param embeddingModel 嵌入模型
     * @param agentId Agent ID
     * @param maxResults 最大返回结果数
     * @return MongoContentRetriever实例
     */
    public MongoContentRetriever createRetriever(EmbeddingModel embeddingModel, String agentId, int maxResults) {
        return new MongoContentRetriever(
            embeddingModel, 
            agentId, 
            maxResults, 
            0.7, // 默认相似度阈值
            "cosine", // 默认相似度类型
            mongoVectorStoreService, 
            vectorSimilarityService
        );
    }
}