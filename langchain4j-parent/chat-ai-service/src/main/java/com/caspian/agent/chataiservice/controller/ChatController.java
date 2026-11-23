package com.caspian.agent.chataiservice.controller;

import com.caspian.agent.chataiservice.advisor.AdvisorChatModel;
import com.caspian.agent.chataiservice.service.AiAssistant;
import com.caspian.agent.chataiservice.service.Friend;
import com.caspian.agent.chataiservice.service.MyAiService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.spring.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    @Autowired
    private AiAssistant aiAssistant;

    @Autowired
    private MyAiService myAiService;

    @GetMapping("/chat/{message}")
    public String chat(@PathVariable String message) {
        return openAiChatModel.chat(message);
    }

    @GetMapping("/chat2/{message}")
    public String chat2(@PathVariable String message) {
        return aiAssistant.chat(message);
    }

    @GetMapping("/chat3/{message}")
    public String chat3(@PathVariable String message) {
        return myAiService.chat(message);
    }
}
