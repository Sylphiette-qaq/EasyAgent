package com.demo.agent.service.impl;

import com.demo.agent.service.AiService;
import com.demo.agent.service.Assistant;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiServiceImpl implements AiService {

    private final String token = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";

    /**
     * 支持上下文的AI对话
     * @param userInput 用户输入
     * @param sessionId 会话唯一标识
     */
    public String aiChat(String userInput, String sessionId) {
        String apiKey = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";

        ChatLanguageModel model =   OpenAiChatModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(apiKey)
                .modelName("Qwen/Qwen3-8B")
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();

        int memoryId = Integer.parseInt(sessionId);
        return assistant.chat(memoryId, userInput);

    }


}
