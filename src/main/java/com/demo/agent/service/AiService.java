package com.demo.agent.service;

import com.demo.agent.entity.AiChatResponse;
import org.springframework.stereotype.Service;

@Service
public interface AiService {

    public AiChatResponse aiChat(String userInput,String sessionId);

}
