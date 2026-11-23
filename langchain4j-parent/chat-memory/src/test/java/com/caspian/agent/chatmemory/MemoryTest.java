package com.caspian.agent.chatmemory;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenTokenCountEstimator;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MemoryTest {

    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String apiKey;

    @Autowired
    QwenChatModel qwenChatModel;

    @Test
    void memoryTest() {
        TokenWindowChatMemory windowChatMemory = TokenWindowChatMemory
                .builder()
                .maxTokens(30, new QwenTokenCountEstimator(apiKey, "qwen-plus"))
                .build();
        windowChatMemory.add(new UserMessage("你是谁"));
        AiMessage aiMessage = qwenChatModel.chat(windowChatMemory.messages()).aiMessage();
        System.out.println(aiMessage.text());
    }

    @Test
    void memoryTest2() {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
        messageWindowChatMemory.add(new UserMessage("你是一只狗，用十个字以内回答我"));
        AiMessage aiMessage = qwenChatModel.chat(messageWindowChatMemory.messages()).aiMessage();
        System.out.println(aiMessage.text());
    }

    @Test
    void memoryTest3() {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
        messageWindowChatMemory.add(new UserMessage("你是一只狗，用十个字以内回答我"));
        AiMessage aiMessage = qwenChatModel.chat(messageWindowChatMemory.messages()).aiMessage();
        messageWindowChatMemory.add(aiMessage);
        messageWindowChatMemory.add(new UserMessage("现在大声告诉我你是谁？"));
        AiMessage aiMessage2 = qwenChatModel.chat(messageWindowChatMemory.messages()).aiMessage();
        System.out.println(aiMessage2.text());
    }


    @Test
    void memoryTest4() {
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(qwenChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        System.out.println(assistant.chat(1, "my name is harry"));
        System.out.println(assistant.chat(2, "my name is cook"));

        System.out.println(assistant.chat(1, "what is my name"));
        System.out.println(assistant.chat(2, "what is my name"));
    }


    interface Assistant {
        String chat(@MemoryId int memoryId, @dev.langchain4j.service.UserMessage String userMessage);
    }

}

