package com.demo.agent.controller;

import com.demo.agent.common.Result;
import com.demo.agent.entity.AiChatResponse;
import com.demo.agent.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiChat {

    @Autowired
    private AiService aiService;

    @PostMapping("/aiChat")
    public Result<AiChatResponse> aiChat(String userInput,String sessionId) {
        return Result.success(aiService.aiChat(userInput,sessionId));
    }
}
