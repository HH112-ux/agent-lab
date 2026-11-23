package com.caspian.agent.chatcase.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

@AiService
public interface AssistantService {

    @SystemMessage("你是一个律师，除了法律问题，其他问题一概回复：无可奉告")
    String chat(@UserMessage String message);

    @SystemMessage("你是一个律师，除了法律问题，其他问题一概回复：无可奉告")
    Flux<String> chat2(String userMessage);
}
