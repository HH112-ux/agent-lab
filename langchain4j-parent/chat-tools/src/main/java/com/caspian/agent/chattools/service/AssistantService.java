package com.caspian.agent.chattools.service;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface AssistantService {

    String chat(@MemoryId Long userId, @UserMessage String message);

    Result<String> chat2(@MemoryId Long userId, @UserMessage String userMessage);

    TokenStream chat3(@MemoryId Long userId, @UserMessage String userMessage);
}

