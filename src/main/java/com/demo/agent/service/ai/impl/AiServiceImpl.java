package com.demo.agent.service.ai.impl;

import com.demo.agent.common.UserContext;
import com.demo.agent.service.ai.AiService;
import com.demo.agent.service.ai.Assistant;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiServiceImpl implements AiService {


    // 全局唯一的Assistantmap
    private static final Map<Integer, Assistant> assistantMap = new ConcurrentHashMap<>();

    private final String token = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";

    /**
     * 支持上下文的AI对话
     * @param userInput 用户输入
     * @param sessionId 会话唯一标识
     */
    public String aiChat(String userInput, String sessionId) {
        String apiKey = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";

        ChatModel model =   OpenAiChatModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(apiKey)
                .modelName("Qwen/Qwen3-8B")
                .build();

        Long userId = UserContext.getUserId();


        // 当前会话id不存在则创建新的Assistant
        if(assistantMap.getOrDefault(Integer.parseInt(sessionId),null) == null){
            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(model)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                    .build();
            assistantMap.put(Integer.parseInt(sessionId),assistant);
        }

        int memoryId = Integer.parseInt(sessionId);
        return assistantMap.get(memoryId).chat(memoryId, userInput);

    }


}
