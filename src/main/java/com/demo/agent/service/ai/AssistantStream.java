package com.demo.agent.service.ai;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface AssistantStream{

    TokenStream chat(@UserMessage String message);
}