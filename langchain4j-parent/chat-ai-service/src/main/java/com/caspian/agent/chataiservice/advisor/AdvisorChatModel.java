package com.caspian.agent.chataiservice.advisor;


import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AdvisorChatModel implements ChatModel {

    private final ChatModel chatModel;

    @Override
    public ChatResponse chat(ChatRequest request) {
        log.info("[Advisor] 收到请求：{}",request.messages());
        ChatResponse response = chatModel.chat(request);
        log.info("[Advisor] 原始响应：{}",request.messages());
        return response;
    }

}
