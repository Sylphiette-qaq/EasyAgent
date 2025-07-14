package com.demo.agent.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.demo.agent.entity.AiChatResponse;
import com.demo.agent.entity.Message;
import com.demo.agent.service.AiService;
import com.demo.agent.service.ChatContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiServiceImpl implements AiService {

    private final String token = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";

    @Autowired
    private ChatContextService chatContextService;

    /**
     * 支持上下文的AI对话
     * @param userInput 用户输入
     * @param sessionId 会话唯一标识
     */
    public AiChatResponse aiChat(String userInput, String sessionId) {
        String url = "https://api.siliconflow.cn/v1/chat/completions";
        List<Message> context = chatContextService.getContext(sessionId);
        // 新增本轮用户消息
        Message userMsg = new Message();
        userMsg.setRole("user");
        userMsg.setContent(userInput);
        context.add(userMsg);

        // 构造messages参数
        List<Map<String, String>> messages = new ArrayList<>();
        for (Message msg : context) {
            Map<String, String> m = new HashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            messages.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("model", "Qwen/QwQ-32B");
        data.put("messages", messages);

        HttpRequest request = HttpUtil.createPost(url)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(data));

        try (HttpResponse response = request.execute()) {
            AiChatResponse aiResp = JSONUtil.toBean(response.body(), AiChatResponse.class);
            // 追加AI回复到上下文
            if (aiResp != null && aiResp.getChoices() != null && !aiResp.getChoices().isEmpty()) {
                Message aiMsg = aiResp.getChoices().get(0).getMessage();
                aiMsg.setRole("assistant");
                context.add(aiMsg);
                chatContextService.saveContext(sessionId, context);
            }
            return aiResp;
        } catch (Exception e) {
            throw new RuntimeException("AI 对话请求失败", e);
        }
    }


}
