package com.caspian.agent.chatmemory.controller;

import com.caspian.agent.chatmemory.service.ChatPersistenceAssistant;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/memory")
public class MemoryController {

    @Autowired
    private QwenChatModel qwenChatModel;

    @Resource
    private ChatPersistenceAssistant chatPersistenceAssistant;

    @GetMapping("/chat/{message}")
    public String chat(@PathVariable String message) {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
        messageWindowChatMemory.add(new UserMessage(message));
        return qwenChatModel.chat(messageWindowChatMemory.messages()).aiMessage().text();

    }

    @GetMapping("/chat2/{message}")
    public String chat2(@PathVariable String message) {
        return chatPersistenceAssistant.chat(1L, "你好我的名字是 roof");
    }
}
