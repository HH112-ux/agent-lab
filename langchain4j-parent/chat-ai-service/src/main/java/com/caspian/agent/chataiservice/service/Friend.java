package com.caspian.agent.chataiservice.service;

import com.caspian.agent.chataiservice.enums.Gender;
import com.caspian.agent.chataiservice.pojo.Person;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import reactor.core.publisher.Flux;

public interface Friend {
    @SystemMessage(value = "你是我的广东朋友，请用广东话和我聊天")
    String chat(String userMessage);

    @UserMessage(value = "你是我的广东朋友，请用广东话和我聊天，你的名字是{{name}}")
    String chat2(@V("name")String name);

    @UserMessage("{{name}}脾气好不好")
    boolean isPositive(@V("name")String text);

    @UserMessage("根据爱好帮我推断它的性别： {{hobby}}")
    Gender analyzeGender(@V("hobby")String hobby);

    @UserMessage("帮我描绘这个人的具体特征： {{person}}")
    Person desPerson(@V("person")String person);

    @UserMessage("帮我描绘这个人的具体特征： {{person}}")
    TokenStream descPerson(@V("person")String person);

    @UserMessage("帮我描绘这个人的具体特征： {{person}}")
    Flux<String> descPerson2(@V("person")String person);
}
