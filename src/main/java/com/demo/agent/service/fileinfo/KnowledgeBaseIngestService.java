package com.demo.agent.service.fileinfo;

import com.demo.agent.config.FileUploadProperties;
import com.demo.agent.service.mongodb.MongoContentRetriever;
import com.demo.agent.service.mongodb.MongoContentRetrieverFactory;
import com.demo.agent.service.mongodb.MongoVectorStoreService;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 将 Agent 知识库目录中的文档向量化并写入 MongoDB
 */
@Service
public class KnowledgeBaseIngestService {

    private final UploadPathService uploadPathService;
    private final MongoContentRetrieverFactory mongoContentRetrieverFactory;
    private final MongoVectorStoreService mongoVectorStoreService;

    public KnowledgeBaseIngestService(
            UploadPathService uploadPathService,
            MongoContentRetrieverFactory mongoContentRetrieverFactory,
            MongoVectorStoreService mongoVectorStoreService) {
        this.uploadPathService = uploadPathService;
        this.mongoContentRetrieverFactory = mongoContentRetrieverFactory;
        this.mongoVectorStoreService = mongoVectorStoreService;
    }

    /**
     * 全量重建指定 Agent 的知识库向量（先清空再加载目录）
     *
     * @return 写入的文本分块数量；目录不存在时返回 0
     */
    public int ingestAgentDirectory(Long agentId) {
        IngestOutcome outcome = ingest(agentId);
        return outcome == null ? 0 : outcome.chunkCount();
    }

    /**
     * 为对话 RAG 挂载检索器：仅查询 MongoDB 已有向量，不重复 ingest。
     * 若尚无向量且上传目录也无支持格式的文件，返回 null。
     */
    public ContentRetriever createRetrieverForAgent(Long agentId) {
        String agentIdStr = String.valueOf(agentId);
        if (mongoVectorStoreService.getVectorCount(agentIdStr) > 0) {
            return newRetriever(agentIdStr);
        }
        Path agentDir = uploadPathService.resolveAgentDir(agentId);
        if (hasSupportedFiles(agentDir)) {
            return newRetriever(agentIdStr);
        }
        return null;
    }

    private MongoContentRetriever newRetriever(String agentIdStr) {
        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
        return mongoContentRetrieverFactory.createRetriever(embeddingModel, agentIdStr);
    }

    private boolean hasSupportedFiles(Path agentDir) {
        if (!Files.isDirectory(agentDir)) {
            return false;
        }
        try (Stream<Path> paths = Files.list(agentDir)) {
            return paths.anyMatch(p ->
                    Files.isRegularFile(p)
                            && FileUploadProperties.isSupportedKnowledgeFileName(p.getFileName().toString()));
        } catch (IOException e) {
            return false;
        }
    }

    private IngestOutcome ingest(Long agentId) {
        Path agentDir = uploadPathService.resolveAgentDir(agentId);
        if (!Files.isDirectory(agentDir)) {
            return null;
        }

        MongoContentRetriever retriever = newRetriever(String.valueOf(agentId));
        retriever.clearVectorData();
        int chunks = retriever.loadAndStoreDocuments(agentDir.toString());
        return new IngestOutcome(chunks, retriever);
    }

    private record IngestOutcome(int chunkCount, MongoContentRetriever retriever) {
    }
}
