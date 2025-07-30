package com.demo.agent.service.agent.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.common.UserContext;
import com.demo.agent.common.redis.RedisOperation;
import com.demo.agent.mapper.AgentMapper;
import com.demo.agent.model.entity.*;
import com.demo.agent.model.entity.FileInfoEntity;
import com.demo.agent.service.agent.AgentService;
import com.demo.agent.service.ai.AssistantStream;
import com.demo.agent.service.ai.LlmModelService;
import com.demo.agent.service.fileinfo.FileInfoService;
import com.demo.agent.service.mcp.McpService;
import com.demo.agent.service.session.SessionService;
import com.demo.agent.tool.McpJsonTool;
import com.demo.agent.tool.PersistentChatMemoryStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static com.demo.agent.common.Constants.MESSAGE_MEMORY_PREFIX;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;


@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, AgentEntity> implements AgentService {

    private static final Map<String, AssistantStream> assistantMap = new ConcurrentHashMap<>();

    @Resource
    private LlmModelService llmModelService;

    @Resource
    private SessionService sessionService;

    @Resource
    private PersistentChatMemoryStore persistentChatMemoryStore;

    @Resource
    private McpService mcpService;

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private RedisOperation redisOperation;

    @Override
    public void addAgentByUser(AgentEntity agentEntity) {
        // 1.判断用户输入的模型是否存在
        try {
            if (agentEntity.getLlmModelId() != null) {
                llmModelService.getById(agentEntity.getLlmModelId());
            }
        } catch (Exception e) {
            throw new RuntimeException("模型不存在");
        }

        // 2.判断用户输入的MCP是否存在
        String mcpIds = agentEntity.getMcpIds();
        String[] mcpIdArray = mcpIds.split(",");
        try {
            for (String mcpId : mcpIdArray) {
                mcpService.getById(mcpId);
            }
        } catch (Exception e) {
            throw new RuntimeException("MCP不存在");
        }
        agentEntity.setUserId(UserContext.getUserId());
        // 3.新增Agent
        save(agentEntity);
    }

    @Override
    public String useAgent(Long agentId, Long sessionId, String userInput) {

        String key = MESSAGE_MEMORY_PREFIX + sessionId;
        // 先判断当前对话id在redis中是否存在
        if (!redisOperation.exists(key)) {
            // 若不存在，则判断当前对话id在数据库中是否存在
            if (sessionService.getById(sessionId) == null) {
                // 数据库中也不存在，则创建唯一id的新对话
                SessionEntity session = new SessionEntity();
                session.setAgentId(agentId);
                session.setUserId(UserContext.getUserId());
                session.setId(sessionId);
                session.setName("新对话");
                session.setCreateBy(UserContext.getUserId());
                session.setCreatedAt(new Date());
                session.setUpdateBy(UserContext.getUserId());
                session.setUpdatedAt(new Date());
                sessionService.save(session);
            }
        }

        Long userId = UserContext.getUserId();
        if (assistantMap.getOrDefault(String.valueOf(userId + agentId), null) == null) {
            AssistantStream assistantStream = creatAgent(agentId, userId);
            assistantMap.put(String.valueOf(userId + agentId), assistantStream);
        }

        AssistantStream assistantStream = assistantMap.getOrDefault(String.valueOf(userId + agentId), null);

        TokenStream tokenStream = assistantStream.chat(sessionId, userInput);

        String[] finalContent = new String[1];

        CountDownLatch latch = new CountDownLatch(1);

        tokenStream
                .onPartialResponse(partial -> {
                }) // 必须有
                .onCompleteResponse(response -> {
                    finalContent[0] = response.aiMessage().text();
                    System.out.println("AI 回复: " + finalContent[0]);
                    latch.countDown(); // 通知主线程任务完成
                })
                .onError(error -> {
                    error.printStackTrace();
                    latch.countDown(); // 出错也放行
                })
                .start();

        try {
            latch.await(); // 阻塞直到 countDown 被调用
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待 AI 回复被中断", e);
        }

        return finalContent[0];
    }

    @Override
    public void useAgentStream(Long agentId, Long sessionId, String userInput, SseEmitter emitter) {
        try {
            String key = MESSAGE_MEMORY_PREFIX + sessionId;
            // 先判断当前对话id在redis中是否存在
            if (!redisOperation.exists(key)) {
                // 若不存在，则判断当前对话id在数据库中是否存在
                if (sessionService.getById(sessionId) == null) {
                    // 数据库中也不存在，则创建唯一id的新对话
                    SessionEntity session = new SessionEntity();
                    session.setAgentId(agentId);
                    session.setUserId(UserContext.getUserId());
                    session.setId(sessionId);
                    session.setName("新对话");
                    session.setCreateBy(UserContext.getUserId());
                    session.setCreatedAt(new Date());
                    session.setUpdateBy(UserContext.getUserId());
                    session.setUpdatedAt(new Date());
                    sessionService.save(session);
                }
            }

            Long userId = UserContext.getUserId();
            if (assistantMap.getOrDefault(String.valueOf(userId + agentId), null) == null) {
                AssistantStream assistantStream = creatAgent(agentId, userId);
                assistantMap.put(String.valueOf(userId + agentId), assistantStream);
            }

            AssistantStream assistantStream = assistantMap.getOrDefault(String.valueOf(userId + agentId), null);

            TokenStream tokenStream = assistantStream.chat(sessionId, userInput);

            tokenStream
                    .onPartialResponse(partialResponse -> {
                        try {
                            // 发送增量响应
                            emitter.send(SseEmitter.event()
                                    .name("partial")
                                    .data(partialResponse));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    })
                    .onCompleteResponse(response -> {
                        try {
                            // 发送完整响应
                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data(response.aiMessage().text()));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    })
                    .onError(error -> {
                        error.printStackTrace();
                        emitter.completeWithError(error);
                    })
                    .start();

        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    /**
     * 切换MCP工具
     *
     * @param agentId
     */
    @Override
    public void changeAgent(Long agentId) {
        // 当agent的相关配置修改后，需要重新构建agent
        assistantMap.remove(String.valueOf(UserContext.getUserId() + agentId));
        AssistantStream assistantStream = creatAgent(agentId, UserContext.getUserId());
        assistantMap.put(String.valueOf(UserContext.getUserId() + agentId), assistantStream);
    }

    /**
     * 重新构建agent
     *
     * @param agentId
     * @param userId
     * @return
     */
    public AssistantStream creatAgent(Long agentId, Long userId) {
        // 1.获取Agent
        AgentEntity agentEntity = getById(agentId);
        if (agentEntity == null) {
            throw new RuntimeException("Agent不存在");
        }

        // 2.获取模型
        LlmModelEntity llmModelEntity = llmModelService.getById(agentEntity.getLlmModelId());
        if (llmModelEntity == null) {
            throw new RuntimeException("模型不存在");
        }

        // 3.生成模型
        String apiKey = llmModelEntity.getApiKey();
        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(llmModelEntity.getApiUrl())
                .apiKey(apiKey)
                .modelName(llmModelEntity.getName())
                .build();

        // 4.获取MCP并组装
        List<McpClient> mcpClientList = buildMcpClients(agentEntity.getMcpIds(), userId);
        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClientList)
                .build();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(persistentChatMemoryStore)
                .build();

        // 5.RAG
        String uploadDir = "D:/java-project/EasyAgent/EasyAgent-backend/uploads/agent_" + agentId;
        ContentRetriever contentRetriever = createContentRetriever(uploadDir);

        // 6.获取系统提示词
        String systemMessage;
        if (getById(agentId).getSystemMessage() != null) {
            systemMessage = getById(agentId).getSystemMessage();
        } else {
            systemMessage = "";
        }


        // 使用Builder模式动态构建AssistantStream
        AiServices<AssistantStream> builder = AiServices.builder(AssistantStream.class)
                .streamingChatModel(model)
                .toolProvider(toolProvider)
                .chatMemoryProvider(chatMemoryProvider);

        // 只有当systemMessage不为空时才设置systemMessageProvider
        if (systemMessage != null && !systemMessage.trim().isEmpty()) {
            builder.systemMessageProvider(chatMemoryId -> systemMessage);
        }
        if(contentRetriever!=null){
            builder.contentRetriever(contentRetriever);
        }

        return builder.build();
    }

    /**
     * 构建MCP客户端列表
     *
     * @param mcpIds MCP ID字符串，用逗号分隔
     * @param userId 用户ID
     * @return MCP客户端列表
     */
    private List<McpClient> buildMcpClients(String mcpIds, Long userId) {
        List<McpClient> mcpClientList = new ArrayList<>();

        if (mcpIds == null || mcpIds.trim().isEmpty()) {
            return mcpClientList;
        }

        String[] mcpIdArray = mcpIds.split(",");

        for (String mcpId : mcpIdArray) {
            if (mcpId.trim().isEmpty()) continue;

            long id = Long.parseLong(mcpId.trim());
            McpEntity mcpEntityById = mcpService.getById(id);
            if (mcpEntityById == null) {
                throw new RuntimeException("MCP不存在: " + id);
            }

            McpServerProperties mcpServerProperties = McpJsonTool.parseJsonToObject(mcpEntityById.getJson());

            // stdio连接
            if (mcpEntityById.getType() == 0) {
                List<String> commandList = new ArrayList<>();
                String command = mcpServerProperties.getCommand();
                if ("npx".equals(command)) {
                    // 替换为系统npx命令路径
                    command = "C:\\Program Files\\nodejs\\npx.cmd";
                }
                commandList.add(command);
                commandList.addAll(mcpServerProperties.getArgs());

                StdioMcpTransport stdioMcpTransport = new StdioMcpTransport.Builder()
                        .command(commandList)
                        .logEvents(true)
                        .build();

                mcpClientList.add(new DefaultMcpClient.Builder()
                        .key(userId + mcpEntityById.getName())
                        .transport(stdioMcpTransport)
                        .build());
            }

            // sse连接
            if (mcpEntityById.getType() == 1) {
                HttpMcpTransport httpMcpTransport = new HttpMcpTransport.Builder()
                        .sseUrl(mcpServerProperties.getUrl())
                        .build();

                mcpClientList.add(new DefaultMcpClient.Builder()
                        .key(userId + mcpEntityById.getName())
                        .transport(httpMcpTransport)
                        .build());
            }
        }

        return mcpClientList;
    }


    /**
     * 构建检索生成器
     * @param filesDir
     * @return
     */
    public ContentRetriever createContentRetriever(String filesDir) {
        // 检查目录是否存在且为有效目录
        File dir = new File(filesDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }

        DocumentParser documentParser = new TextDocumentParser();
        List<Document> documents;
        try {
            documents = loadDocuments(filesDir, documentParser);
        } catch (Exception e) {
            // 处理文件加载异常（如权限问题）
            return null;
        }

        // 检查是否有文档被加载
        if (documents == null || documents.isEmpty()) {
            return null;
        }

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.splitAll(documents);


        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();


        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);


        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.5)
                .build();
    }

}