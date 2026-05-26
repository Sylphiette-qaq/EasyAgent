package com.demo.agent.service.mongodb;

import com.demo.agent.model.entity.DocumentVector;
import com.demo.agent.model.entity.SimilarityResult;
import com.demo.agent.model.entity.VectorEmbedding;
import com.demo.agent.model.entity.VectorSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MongoDB向量存储服务
 * 提供文档向量的存储、检索和相似度搜索功能
 */
@Service
public class MongoVectorStoreService {
    
    private static final Logger logger = LoggerFactory.getLogger(MongoVectorStoreService.class);
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private VectorSimilarityService vectorSimilarityService;
    
    private static final String COLLECTION_NAME = "vector_embeddings";
    
    /**
     * 存储单个文档向量
     * @param content 文档内容
     * @param vector 向量数据
     * @param metadata 元数据
     * @param agentId Agent ID
     * @return 存储的文档ID
     */
    public String storeVector(String content, List<Double> vector, String metadata, String agentId) {
        try {
            VectorEmbedding embedding = new VectorEmbedding();
            embedding.setId(UUID.randomUUID().toString());
            embedding.setContent(content);
            embedding.setVector(vector);
            embedding.setMetadata(metadata);
            embedding.setAgentId(agentId);
            embedding.setCreatedAt(LocalDateTime.now());
            embedding.setUpdatedAt(LocalDateTime.now());
            
            mongoTemplate.save(embedding, COLLECTION_NAME);
            
            logger.info("成功存储向量文档，ID: {}, Agent: {}", embedding.getId(), agentId);
            return embedding.getId();
        } catch (Exception e) {
            logger.error("存储向量文档失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储向量文档失败", e);
        }
    }
    
    /**
     * 批量存储文档向量
     * @param documents 文档列表
     * @param agentId Agent ID
     * @return 存储的文档ID列表
     */
    public List<String> storeVectors(List<DocumentVector> documents, String agentId) {
        List<String> documentIds = new ArrayList<>();
        
        try {
            List<VectorEmbedding> embeddings = new ArrayList<>();
            
            for (DocumentVector doc : documents) {
                if (doc.getVector() == null || doc.getVector().isEmpty()) {
                    logger.warn("跳过无向量数据的分块，Agent: {}", agentId);
                    continue;
                }
                VectorEmbedding embedding = new VectorEmbedding();
                embedding.setId(UUID.randomUUID().toString());
                embedding.setContent(doc.getContent());
                embedding.setVector(doc.getVector());
                embedding.setMetadata(doc.getMetadata());
                embedding.setAgentId(agentId);
                embedding.setCreatedAt(LocalDateTime.now());
                embedding.setUpdatedAt(LocalDateTime.now());

                embeddings.add(embedding);
                documentIds.add(embedding.getId());
            }

            if (embeddings.isEmpty()) {
                logger.warn("无有效向量可写入，Agent: {}", agentId);
                return documentIds;
            }
            
            mongoTemplate.insertAll(embeddings);
            
            logger.info("成功批量存储 {} 个向量文档，Agent: {}", embeddings.size(), agentId);
            return documentIds;
        } catch (Exception e) {
            logger.error("批量存储向量文档失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量存储向量文档失败", e);
        }
    }
    
    /**
     * 根据向量相似度搜索文档
     * @param queryVector 查询向量
     * @param agentId Agent ID
     * @param topK 返回前K个最相似的结果
     * @param threshold 相似度阈值
     * @return 相似度搜索结果
     */
    public List<VectorSearchResult> searchSimilarDocuments(
            List<Double> queryVector, 
            String agentId, 
            int topK, 
            double threshold) {
        
        try {
            // 查询指定Agent的所有向量文档
            Query query = new Query(Criteria.where("agentId").is(agentId));
            List<VectorEmbedding> allEmbeddings = mongoTemplate.find(query, VectorEmbedding.class, COLLECTION_NAME);
            
            if (allEmbeddings.isEmpty()) {
                logger.info("Agent {} 没有找到向量文档", agentId);
                return new ArrayList<>();
            }
            
            // 将当前agent所有的文档转换为相似度计算格式
            // todo 可优化，通过摘要判断只提取相关的
            List<SimilarityResult> candidates = allEmbeddings.stream()
                .filter(e -> e.getVector() != null && !e.getVector().isEmpty())
                .map(embedding -> new SimilarityResult(
                    embedding.getId(),
                    embedding.getContent(),
                    0.0,
                    embedding.getVector()
                ))
                .collect(Collectors.toList());

            if (candidates.isEmpty()) {
                logger.warn("Agent {} 的向量文档均无有效 vector 字段，请重新上传知识库文件", agentId);
                return new ArrayList<>();
            }
            
            // 执行相似度搜索
            List<SimilarityResult> similarityResults = 
                vectorSimilarityService.searchSimilarVectors(queryVector, candidates, topK);
            
            // 过滤阈值并转换结果格式
            List<VectorSearchResult> results = new ArrayList<>();
            for (SimilarityResult result : similarityResults) {
                // 余弦相似度：值越大越相似
                boolean meetsThreshold = result.getSimilarity() >= threshold;
                
                if (meetsThreshold) {
                    // 获取完整的embedding信息
                    VectorEmbedding embedding = allEmbeddings.stream()
                        .filter(e -> e.getId().equals(result.getDocumentId()))
                        .findFirst()
                        .orElse(null);
                    
                    if (embedding != null) {
                        VectorSearchResult searchResult = new VectorSearchResult();
                        searchResult.setDocumentId(result.getDocumentId());
                        searchResult.setContent(result.getContent());
                        searchResult.setSimilarity(result.getSimilarity());
                        searchResult.setMetadata(embedding.getMetadata());
                        searchResult.setCreatedAt(embedding.getCreatedAt());
                        
                        results.add(searchResult);
                    }
                }
            }
            
            logger.info("Agent {} 向量搜索完成，返回 {} 个结果", agentId, results.size());
            return results;
            
        } catch (Exception e) {
            logger.error("向量相似度搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("向量相似度搜索失败", e);
        }
    }
    
    /**
     * 根据内容关键词搜索文档
     * @param keywords 关键词
     * @param agentId Agent ID
     * @return 搜索结果
     */
    public List<VectorEmbedding> searchByKeywords(String keywords, String agentId) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("agentId").is(agentId)
                .and("content").regex(keywords, "i")); // 不区分大小写的正则匹配
            
            List<VectorEmbedding> results = mongoTemplate.find(query, VectorEmbedding.class, COLLECTION_NAME);
            
            logger.info("关键词搜索完成，Agent: {}, 关键词: {}, 结果数: {}", agentId, keywords, results.size());
            return results;
        } catch (Exception e) {
            logger.error("关键词搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("关键词搜索失败", e);
        }
    }
    
    /**
     * 删除指定Agent的所有向量文档
     * @param agentId Agent ID
     * @return 删除的文档数量
     */
    public long deleteVectorsByAgent(String agentId) {
        try {
            Query query = new Query(Criteria.where("agentId").is(agentId));
            long deletedCount = mongoTemplate.remove(query, VectorEmbedding.class, COLLECTION_NAME).getDeletedCount();
            
            logger.info("删除Agent {} 的向量文档，删除数量: {}", agentId, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            logger.error("删除向量文档失败: {}", e.getMessage(), e);
            throw new RuntimeException("删除向量文档失败", e);
        }
    }
    
    /**
     * 获取指定Agent的向量文档数量
     * @param agentId Agent ID
     * @return 文档数量
     */
    public long getVectorCount(String agentId) {
        try {
            Query query = new Query(Criteria.where("agentId").is(agentId));
            long count = mongoTemplate.count(query, VectorEmbedding.class, COLLECTION_NAME);
            
            logger.debug("Agent {} 的向量文档数量: {}", agentId, count);
            return count;
        } catch (Exception e) {
            logger.error("获取向量文档数量失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取向量文档数量失败", e);
        }
    }
    

    

}