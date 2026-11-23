package com.caspian.agent.chataiservice.config;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MyConfiguration {

    @Bean
    public ChatModelListener chatModelListener() {
        return new ChatModelListener() {
            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                log.info("请求prompt：{}",requestContext.chatRequest().messages());
                ChatModelListener.super.onRequest(requestContext);
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.info("相应prompt：{}",responseContext.chatResponse().aiMessage().text());

                ChatModelListener.super.onResponse(responseContext);
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                ChatModelListener.super.onError(errorContext);
            }
        };
    }
}
