package com.demo.agent.service.agent.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.common.UserContext;
import com.demo.agent.mapper.AgentMapper;
import com.demo.agent.model.entity.Agent;
import com.demo.agent.model.entity.LlmModel;
import com.demo.agent.model.entity.Mcp;
import com.demo.agent.service.agent.AgentService;
import com.demo.agent.service.ai.Assistant;
import com.demo.agent.service.ai.AssistantStream;
import com.demo.agent.service.ai.LlmModelService;
import com.demo.agent.service.mcp.McpService;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {

    private static final Map<String, AssistantStream> assistantMap = new ConcurrentHashMap<>();

    @Resource
    private LlmModelService llmModelService;

    @Resource
    private McpService mcpService;

    @Override
    public void addAgentByUser(Agent agent) {
        // 1.判断用户输入的模型是否存在
        try {
            if (agent.getLlmModelId() != null) {
                llmModelService.getById(agent.getLlmModelId());
            }
        } catch (Exception e) {
            throw new RuntimeException("模型不存在");
        }

        // 2.判断用户输入的MCP是否存在
        String mcpIds = agent.getMcpIds();
        String[] mcpIdArray = mcpIds.split(",");
        try {
            for (String mcpId : mcpIdArray) {
                mcpService.getById(mcpId);
            }
        } catch (Exception e) {
            throw new RuntimeException("MCP不存在");
        }
        // 3.新增Agent
        save(agent);
    }

    @Override
    public String useAgent(Long agentId, String userInput) {

        Long userId = UserContext.getUserId();
        if (assistantMap.getOrDefault(String.valueOf(userId + agentId), null) == null) {
            // 1.获取Agent
            Agent agent = getById(agentId);
            if (agent == null) {
                throw new RuntimeException("Agent不存在");
            }

            // 2.获取模型
            LlmModel llmModel = llmModelService.getById(agent.getLlmModelId());
            if (llmModel == null) {
                throw new RuntimeException("模型不存在");
            }

            // 3.生成模型
            String apiKey = llmModel.getApiKey();
            StreamingChatModel model = OpenAiStreamingChatModel.builder()
                    .baseUrl(llmModel.getApiUrl())
                    .apiKey(apiKey)
                    .modelName(llmModel.getName())
                    .build();

            // 4.获取MCP
            String mcpIds = agent.getMcpIds();
            String[] mcpIdArray = mcpIds.split(",");

            // 5.根据mcp的类型组装agent

            List<McpClient> mcpClientList = new ArrayList<>();

            for (String mcpId : mcpIdArray) {
                long id = Long.parseLong(mcpId);
                Mcp mcpById = mcpService.getById(id);
                if (mcpById == null) {
                    throw new RuntimeException("MCP不存在");
                }

                // 5.1 stdio连接
                if (mcpById.getType().equals("0")) {
                    List<String> commandList = new ArrayList<>();
                    commandList.add("C:/Program Files/nodejs/npx.cmd");
                    String[] args = mcpById.getArgs().split(",");
                    commandList.addAll(Arrays.asList(args));
                    StdioMcpTransport stdioMcpTransport = new StdioMcpTransport.Builder()
                            .command(commandList)
                            .logEvents(true)
                            .build();
                    mcpClientList.add(new DefaultMcpClient.Builder()
                            .key(userId + mcpById.getName())
                            .transport(stdioMcpTransport)
                            .build());
                }

                // 5.2 sse连接
                if (mcpById.getType().equals("1")) {
                    HttpMcpTransport httpMcpTransport = new HttpMcpTransport.Builder()
                            .sseUrl(mcpById.getUrl())
                            .build();
                    mcpClientList.add(new DefaultMcpClient.Builder()
                            .key(userId + mcpById.getName())
                            .transport(httpMcpTransport)
                            .build());
                }


            }

            McpToolProvider toolProvider = McpToolProvider.builder()
                    .mcpClients(mcpClientList)
                    .build();

            AssistantStream assistant = AiServices.builder(AssistantStream.class)
                    .streamingChatModel(model)
                    .toolProvider(toolProvider)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                    .build();

            assistantMap.put(String.valueOf(userId + agentId), assistant);
        }

        AssistantStream assistantStream = assistantMap.getOrDefault(String.valueOf(userId + agentId), null);

        TokenStream tokenStream = assistantStream.chat(userId.intValue(), userInput);

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