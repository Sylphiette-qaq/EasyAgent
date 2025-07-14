package com.demo.agent.controller;

import com.demo.agent.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiChat {

    @Autowired
    private AiService aiService;

    @PostMapping("/aiChat")
    public String aiChat(String userInput) {
        return aiService.aiChat(userInput);
    }
}
