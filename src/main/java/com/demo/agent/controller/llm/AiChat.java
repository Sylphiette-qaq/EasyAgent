package com.demo.agent.controller.llm;

import com.demo.agent.common.Result;
import com.demo.agent.service.ai.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiChat {

    @Autowired
    private AiService aiService;

    @PostMapping("/aiChat")
    public Result<Object> aiChat(@RequestParam("userInput") String userInput, @RequestParam("sessionId") String sessionId) {
        return Result.success(aiService.aiChat(userInput,sessionId));
    }
}
