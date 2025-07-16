package com.demo.agent.service.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;

public interface Assistant extends ChatMemoryAccess {

    String chat(@MemoryId int memoryId, @UserMessage String message);
}