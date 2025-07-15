package com.demo.agent.service;

import dev.langchain4j.service.ChatMemoryAccess;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface Assistant extends ChatMemoryAccess {

    String chat(@MemoryId int memoryId, @UserMessage String message);
}