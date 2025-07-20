package com.demo.agent.service.agent.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.common.UserContext;
import com.demo.agent.mapper.AgentMapper;
import com.demo.agent.model.entity.*;
import com.demo.agent.service.agent.AgentService;
import com.demo.agent.service.ai.AssistantStream;
import com.demo.agent.service.ai.LlmModelService;
import com.demo.agent.service.mcp.McpService;
import com.demo.agent.service.session.SessionService;
import com.demo.agent.tool.McpJsonTool;
import com.demo.agent.tool.PersistentChatMemoryStore;
import com.demo.agent.tool.TimeBasedIdGenerator;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

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

        if(sessionService.getById(sessionId) == null){
            // 新对话
            SessionEntity session = new SessionEntity();
            session.setAgentId(agentId);
            session.setUserId(UserContext.getUserId());
            session.setId(sessionId);
            session.setName("新对话");
            session.setCreateBy(UserContext.getUserId());
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdateBy(UserContext.getUserId());
            session.setUpdatedAt(LocalDateTime.now());
            sessionService.save(session);
        }


        Long userId = UserContext.getUserId();
        if (assistantMap.getOrDefault(String.valueOf(userId + agentId), null) == null) {
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

            // 4.获取MCP
            String mcpIds = agentEntity.getMcpIds();
            String[] mcpIdArray = mcpIds.split(",");

            // 5.根据mcp的类型组装agent

            List<McpClient> mcpClientList = new ArrayList<>();

            for (String mcpId : mcpIdArray) {
                long id = Long.parseLong(mcpId);
                McpEntity mcpEntityById = mcpService.getById(id);
                if (mcpEntityById == null) {
                    throw new RuntimeException("MCP不存在");
                }

                McpServerProperties mcpServerProperties = McpJsonTool.parseJsonToObject(mcpEntityById.getJson());

                // 5.1 stdio连接
                if (mcpEntityById.getType() == 0) {
                    List<String> commandList = new ArrayList<>();
                    commandList.add(mcpServerProperties.getCommand());
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

                // 5.2 sse连接
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

            ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                    .id(memoryId)
                    .maxMessages(10)
                    .chatMemoryStore(persistentChatMemoryStore)
                    .build();


            McpToolProvider toolProvider = McpToolProvider.builder()
                    .mcpClients(mcpClientList)
                    .build();

            AssistantStream assistant = AiServices.builder(AssistantStream.class)
                    .streamingChatModel(model)
                    .toolProvider(toolProvider)
                    .chatMemoryProvider(chatMemoryProvider)
                    .build();

            assistantMap.put(String.valueOf(userId + agentId), assistant);
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
}