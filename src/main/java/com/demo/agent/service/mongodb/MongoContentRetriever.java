package com.demo.agent.service.mongodb;

import com.demo.agent.config.FileUploadProperties;
import com.demo.agent.service.fileinfo.KnowledgeDocumentParser;
import com.demo.agent.model.entity.DocumentVector;
import com.demo.agent.model.entity.VectorSearchResult;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * MongoDB向量内容检索器
 * 集成MongoDB向量存储和应用层相似度计算的ContentRetriever实现
 */
public class MongoContentRetriever implements ContentRetriever {

    private static final Logger logger = LoggerFactory.getLogger(MongoContentRetriever.class);


    private final MongoVectorStoreService mongoVectorStoreService;

    private final VectorSimilarityService vectorSimilarityService;

    private final KnowledgeDocumentParser knowledgeDocumentParser;

    private EmbeddingModel embeddingModel;
    private String agentId;
    private int maxResults;
    private double similarityThreshold;
    private String similarityType;

    // 默认配置
    private static final int DEFAULT_MAX_RESULTS = 5;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;
    private static final String DEFAULT_SIMILARITY_TYPE = "cosine";


    /**
     * 构造函数（完整配置）
     * @param embeddingModel 嵌入模型
     * @param agentId Agent ID
     * @param maxResults 最大返回结果数
     * @param similarityThreshold 相似度阈值
     * @param similarityType 相似度类型
     * @param mongoVectorStoreService MongoDB向量存储服务
     * @param vectorSimilarityService 向量相似度服务
     */
    public MongoContentRetriever(EmbeddingModel embeddingModel, String agentId,
                                int maxResults, double similarityThreshold, String similarityType,
                                MongoVectorStoreService mongoVectorStoreService,
                                VectorSimilarityService vectorSimilarityService,
                                KnowledgeDocumentParser knowledgeDocumentParser) {
        this.embeddingModel = embeddingModel;
        this.agentId = agentId;
        this.maxResults = maxResults;
        this.similarityThreshold = similarityThreshold;
        this.similarityType = similarityType;
        this.mongoVectorStoreService = mongoVectorStoreService;
        this.vectorSimilarityService = vectorSimilarityService;
        this.knowledgeDocumentParser = knowledgeDocumentParser;
    }

    /**
     * 构造函数（使用默认配置）
     * @param embeddingModel 嵌入模型
     * @param agentId Agent ID
     * @param mongoVectorStoreService MongoDB向量存储服务
     * @param vectorSimilarityService 向量相似度服务
     */
    public MongoContentRetriever(EmbeddingModel embeddingModel, String agentId,
                                MongoVectorStoreService mongoVectorStoreService,
                                VectorSimilarityService vectorSimilarityService,
                                KnowledgeDocumentParser knowledgeDocumentParser) {
        this(embeddingModel, agentId, DEFAULT_MAX_RESULTS, DEFAULT_SIMILARITY_THRESHOLD,
                DEFAULT_SIMILARITY_TYPE, mongoVectorStoreService, vectorSimilarityService,
                knowledgeDocumentParser);
    }

    @Override
    public List<Content> retrieve(Query query) {
        try {
            logger.info("开始检索相关内容，Agent: {}, 查询: {}", agentId, query.text());

            // 1. 将查询文本向量化
            Embedding queryEmbedding = embeddingModel.embed(query.text()).content();
            List<Double> queryVector = convertEmbeddingToList(queryEmbedding);

            // 2. 在MongoDB中搜索相似向量
            List<VectorSearchResult> searchResults = mongoVectorStoreService.searchSimilarDocuments(
                queryVector, agentId, maxResults, similarityThreshold
            );

            // 3. 转换为Content对象
            List<Content> contents = searchResults.stream()
                .map(this::convertToContent)
                .collect(Collectors.toList());

            logger.info("检索完成，Agent: {}, 返回 {} 个相关内容", agentId, contents.size());
            return contents;

        } catch (Exception e) {
            logger.error("内容检索失败，Agent: {}, 查询: {}, 错误: {}", agentId, query.text(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 加载并存储文档到MongoDB
     * @param documentsPath 文档路径
     * @return 成功存储的文档数量
     */
    public int loadAndStoreDocuments(String documentsPath) {
        try {
            logger.info("开始加载文档，Agent: {}, 路径: {}", agentId, documentsPath);

            Path path = Paths.get(documentsPath);
            File file = path.toFile();

            if (!file.exists()) {
                logger.warn("文档路径不存在: {}", documentsPath);
                return 0;
            }

            List<DocumentVector> documentVectors = new ArrayList<>();

            if (file.isDirectory()) {
                File[] files = file.listFiles((dir, name) ->
                        FileUploadProperties.isSupportedKnowledgeFileName(name));

                if (files != null) {
                    for (File docFile : files) {
                        documentVectors.addAll(processDocument(docFile));
                    }
                }
            } else if (FileUploadProperties.isSupportedKnowledgeFileName(file.getName())) {
                documentVectors.addAll(processDocument(file));
            } else {
                logger.warn("不支持的文件类型，跳过: {}", file.getName());
            }

            // 批量存储到MongoDB
            if (!documentVectors.isEmpty()) {
                List<String> documentIds = mongoVectorStoreService.storeVectors(documentVectors, agentId);
                logger.info("成功存储 {} 个文档分块，Agent: {}", documentIds.size(), agentId);
                return documentIds.size();
            }

            return 0;

        } catch (Exception e) {
            logger.error("加载和存储文档失败，Agent: {}, 路径: {}, 错误: {}", agentId, documentsPath, e.getMessage(), e);
            throw new RuntimeException("加载和存储文档失败", e);
        }
    }

    /**
     * 处理单个文档
     * @param file 文档文件
     * @return 文档向量列表
     */
    private List<DocumentVector> processDocument(File file) {
        List<DocumentVector> documentVectors = new ArrayList<>();

        try {
            Document document = knowledgeDocumentParser.parse(file);
            String text = document.text();
            if (text == null || text.isBlank()) {
                logger.warn("文档解析结果为空: {}", file.getName());
                return documentVectors;
            }

            // 分割文档
            DocumentSplitter splitter = DocumentSplitters.recursive(300, 50);
            List<TextSegment> segments = splitter.split(document);

            // 3. 为每个分段生成向量
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);

                // 生成嵌入向量
                Embedding embedding = embeddingModel.embed(segment).content();
                List<Double> vector = convertEmbeddingToList(embedding);

                // 创建元数据
                // todo 可以增加摘要字段
                String metadata = String.format(
                    "{\"sourceFile\":\"%s\",\"chunkIndex\":%d,\"totalChunks\":%d,\"documentType\":\"%s\"}",
                    file.getName(), i, segments.size(), getFileExtension(file.getName())
                );

                // 创建文档向量对象
                DocumentVector docVector =
                    new DocumentVector(segment.text(), vector, metadata);

                documentVectors.add(docVector);
            }

            logger.debug("处理文档完成: {}, 分块数: {}", file.getName(), segments.size());

        } catch (Exception e) {
            logger.error("处理文档失败: {}, 错误: {}", file.getName(), e.getMessage(), e);
        }

        return documentVectors;
    }

    /**
     * 将Embedding转换为Double列表
     * @param embedding 嵌入向量
     * @return Double列表
     */
    private List<Double> convertEmbeddingToList(Embedding embedding) {
        float[] vector = embedding.vector();
        List<Double> doubleList = new ArrayList<>();
        for (float value : vector) {
            doubleList.add((double) value);
        }
        return doubleList;
    }

    /**
     * 将搜索结果转换为Content对象
     * @param searchResult 搜索结果
     * @return Content对象
     */
    private Content convertToContent(VectorSearchResult searchResult) {
        return Content.from(searchResult.getContent());
    }

    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "unknown";
    }

    /**
     * 清空指定Agent的所有向量数据
     * @return 删除的文档数量
     */
    public long clearVectorData() {
        try {
            long deletedCount = mongoVectorStoreService.deleteVectorsByAgent(agentId);
            logger.info("清空向量数据完成，Agent: {}, 删除数量: {}", agentId, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            logger.error("清空向量数据失败，Agent: {}, 错误: {}", agentId, e.getMessage(), e);
            throw new RuntimeException("清空向量数据失败", e);
        }
    }

    /**
     * 获取向量数据统计信息
     * @return 统计信息
     */
    public VectorStatistics getStatistics() {
        try {
            long totalCount = mongoVectorStoreService.getVectorCount(agentId);

            VectorStatistics stats = new VectorStatistics();
            stats.setAgentId(agentId);
            stats.setTotalDocuments(totalCount);
            stats.setSimilarityType("cosine"); // 现在只支持余弦相似度
            stats.setSimilarityThreshold(similarityThreshold);
            stats.setMaxResults(maxResults);

            return stats;
        } catch (Exception e) {
            logger.error("获取统计信息失败，Agent: {}, 错误: {}", agentId, e.getMessage(), e);
            throw new RuntimeException("获取统计信息失败", e);
        }
    }

    // Getters and Setters
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public void setSimilarityType(String similarityType) {
        // 保留方法以保持兼容性，但现在只支持余弦相似度
        this.similarityType = "cosine";
    }

    /**
     * 向量统计信息
     */
    public static class VectorStatistics {
        private String agentId;
        private long totalDocuments;
        private String similarityType;
        private double similarityThreshold;
        private int maxResults;

        // Getters and Setters
        public String getAgentId() { return agentId; }
        public void setAgentId(String agentId) { this.agentId = agentId; }

        public long getTotalDocuments() { return totalDocuments; }
        public void setTotalDocuments(long totalDocuments) { this.totalDocuments = totalDocuments; }

        public String getSimilarityType() { return similarityType; }
        public void setSimilarityType(String similarityType) { this.similarityType = similarityType; }

        public double getSimilarityThreshold() { return similarityThreshold; }
        public void setSimilarityThreshold(double similarityThreshold) { this.similarityThreshold = similarityThreshold; }

        public int getMaxResults() { return maxResults; }
        public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
    }
}