package com.demo.agent.service.ai;

import org.springframework.stereotype.Service;

@Service
public interface AiService {

    public String aiChat(String userInput,String sessionId);

}
