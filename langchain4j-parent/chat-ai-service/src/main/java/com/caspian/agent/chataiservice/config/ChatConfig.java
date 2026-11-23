package com.caspian.agent.chataiservice.config;

import com.caspian.agent.chataiservice.advisor.AdvisorChatModel;
import com.caspian.agent.chataiservice.service.AiAssistant;
import com.caspian.agent.chataiservice.service.Friend;
import com.caspian.agent.chataiservice.service.MyAiService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.spring.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatConfig {

 private final QwenChatModel qwenChatModel;

    @Bean
    public ChatModel advisorChatModel() {
        return new AdvisorChatModel(qwenChatModel);
    }

    @Bean
    public MyAiService myAiService(){
        return AiServices
                .builder(MyAiService.class)
                .chatModel(advisorChatModel())
                .build();
    }
}
