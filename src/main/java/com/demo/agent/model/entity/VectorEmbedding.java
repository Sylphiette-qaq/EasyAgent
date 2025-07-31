package com.demo.agent.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 向量嵌入实体类
 * 用于在MongoDB中存储文档的向量化数据
 */
@Document(collection = "vector_embeddings")
@Data
public class VectorEmbedding {
    
    @Id
    private String id;
    
    /**
     * 原始文档内容
     */
    private String content;
    
    /**
     * 向量数据
     */
    private List<Double> vector;
    
    /**
     * 元数据信息（JSON格式字符串）
     */
    private String metadata;
    
    /**
     * 所属Agent ID
     */
    @Indexed
    private String agentId;
    
    /**
     * 文档来源文件名
     */
    private String sourceFile;
    
    /**
     * 文档分块索引（用于标识同一文档的不同分块）
     */
    private Integer chunkIndex;
    
    /**
     * 向量维度
     */
    private Integer dimension;
    
    /**
     * 文档类型（如：txt, pdf, md等）
     */
    private String documentType;
    
    /**
     * 文档重要性评分（0-1之间）
     */
    private Double importanceScore;
    
    /**
     * 文档主题标签
     */
    private List<String> topicTags;
    
    /**
     * 创建时间
     */
    @Indexed
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessedAt;
    
    /**
     * 访问次数
     */
    private Long accessCount;
    
    /**
     * 是否已删除（软删除标记）
     */
    private Boolean deleted;
    
    // 构造函数
    public VectorEmbedding() {
        this.accessCount = 0L;
        this.deleted = false;
        this.importanceScore = 0.0;
    }
    
    public VectorEmbedding(String content, List<Double> vector, String agentId) {
        this();
        this.content = content;
        this.vector = vector;
        this.agentId = agentId;
        this.dimension = vector != null ? vector.size() : 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    
    public void setVector(List<Double> vector) {
        this.vector = vector;
        this.dimension = vector != null ? vector.size() : 0;
    }


    
    /**
     * 增加访问次数
     */
    public void incrementAccessCount() {
        this.accessCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 软删除
     */
    public void softDelete() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 恢复删除
     */
    public void restore() {
        this.deleted = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "VectorEmbedding{" +
                "id='" + id + '\'' +
                ", content='" + (content != null && content.length() > 50 ? 
                    content.substring(0, 50) + "..." : content) + '\'' +
                ", agentId='" + agentId + '\'' +
                ", dimension=" + dimension +
                ", importanceScore=" + importanceScore +
                ", createdAt=" + createdAt +
                '}';
    }
}