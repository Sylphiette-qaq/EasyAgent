package com.demo.agent.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.demo.agent.service.AiService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    private final String token = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";

    @Override
    public String aiChat(String userInput) {
        String url = "https://api.siliconflow.cn/v1/chat/completions";
        Map<String, Object> data = new HashMap<>();
        data.put("model", "Qwen/QwQ-32B");
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userInput);
        data.put("messages", java.util.Collections.singletonList(message));

        HttpRequest request = HttpUtil.createPost(url)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(data));

        HttpResponse response = request.execute();
        return response.body();
    }
}
