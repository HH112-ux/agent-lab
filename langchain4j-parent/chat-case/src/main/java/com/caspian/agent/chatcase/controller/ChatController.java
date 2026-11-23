package com.caspian.agent.chatcase.controller;

import com.caspian.agent.chatcase.service.AssistantService;
import com.caspian.agent.chatcase.vo.ChatRequest;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/case")
public class ChatController {

    @Resource
    private QwenChatModel qwenChatModel;

    @Resource
    private QwenStreamingChatModel qwenStreamingChatModel;

    @GetMapping("/chat/{message}")
    public String chat(@PathVariable String message) {
        return qwenChatModel.chat(message);
    }

    @GetMapping("/chat2/{message}")
    public String chat2(@PathVariable String message) {
        AssistantService service = AiServices.builder(AssistantService.class).chatModel(qwenChatModel).build();
        return service.chat(message);
    }

    @PostMapping("/chatStream")
    public Flux<String> chatStream(@RequestBody String message) {
        AssistantService service = AiServices
                .builder(AssistantService.class)
                .streamingChatModel(qwenStreamingChatModel)
                .build();
        return service.chat2(message);
    }

    @PostMapping(value = "/chatSse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatSse(@RequestBody ChatRequest request) {
        String message = request.getMessage();

        if (message == null || message.isEmpty()) {
            return Flux.just(ServerSentEvent.builder("[错误]：消息不能为空").build());
        }
        AssistantService service = AiServices
                .builder(AssistantService.class)
                .streamingChatModel(qwenStreamingChatModel)
                .build();
        return service.chat2(message)
                .map(content -> ServerSentEvent.builder(content)
                        .retry(Duration.ofSeconds(3))
                        .build()
                )
                .onErrorResume(e -> {
                    return Flux.just(ServerSentEvent.builder("[错误]：" + e.getMessage()).build());
                });
    }
}
