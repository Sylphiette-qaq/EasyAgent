package com.demo.agent.service.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface AssistantStream{

    TokenStream chat(@MemoryId int memoryId,@UserMessage String message);
}