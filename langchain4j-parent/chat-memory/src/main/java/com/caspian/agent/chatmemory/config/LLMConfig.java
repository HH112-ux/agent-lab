package com.caspian.agent.chatmemory.config;

import com.caspian.agent.chatmemory.service.ChatPersistenceAssistant;
import com.caspian.agent.chatmemory.store.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Bean
   public ChatPersistenceAssistant chatPersistenceAssistant(ChatModel chatModel){
        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory
                .builder()
                .id(memoryId)
                .maxMessages(100)
                .chatMemoryStore(redisChatMemoryStore)
                .build();
        return AiServices.builder(ChatPersistenceAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }
}
