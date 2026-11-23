package com.caspian.agent.chatmemory.store;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RedisChatMemoryStore implements ChatMemoryStore {
    public static final String CHAT_MEMORY_PREFIX = "CHAT_MEMORY:";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = CHAT_MEMORY_PREFIX + memoryId;
        // 这里用 String 接收，兼容你的 Redis 序列化
        String json = (String) redisTemplate.opsForValue().get(key);

        if (json != null && !json.isEmpty()) {
            return ChatMessageDeserializer.messagesFromJson(json);
        }
        return new ArrayList<>();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        redisTemplate.opsForValue().set(CHAT_MEMORY_PREFIX + memoryId, ChatMessageSerializer.messagesToJson(messages));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(CHAT_MEMORY_PREFIX + memoryId);
    }
}
