package com.caspian.agent.chataiservice;

import com.caspian.agent.chataiservice.enums.Gender;
import com.caspian.agent.chataiservice.pojo.Person;
import com.caspian.agent.chataiservice.service.AiAssistant;
import com.caspian.agent.chataiservice.service.Friend;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static dev.langchain4j.model.chat.request.ResponseFormatType.JSON;

@SpringBootTest
public class AIServiceTest {

    @Autowired
    QwenChatModel qwenChatModel;

    @Test
    void test() {
        AiAssistant aiAssistant = AiServices.create(AiAssistant.class, qwenChatModel);
        String chat = aiAssistant.chat("你是谁");
        System.out.println(chat);
    }


    @Test
    void test2() {
        Friend friend = AiServices.create(Friend.class, qwenChatModel);
        String chat = friend.chat("你是谁");
        System.out.println(chat);
    }


    @Test
    void test3() {
        Friend friend = AiServices.builder(Friend.class)
                .chatModel(qwenChatModel)
                .systemMessageProvider((system) -> "你是我的广西朋友，请用方言和我交流")
                .systemMessage("你是我的广西朋友，请用方言和我交流")
                .build();
        String chat = friend.chat("你是谁");
        System.out.println(chat);
    }

    @Test
    void test4() {
        Friend friend = AiServices.builder(Friend.class)
                .chatModel(qwenChatModel)
                .build();
        String chat = friend.chat2("拎壶冲");
        System.out.println(chat);
    }

    @Test
    void test5() {
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(JSON) // 类型可以是 TEXT（默认）或 JSON
                .jsonSchema(JsonSchema.builder()
                        .name("Person") // OpenAI 要求为 schema 指定名称
                        .rootElement(JsonObjectSchema.builder() // 见下面的 [1]
                                .addStringProperty("name")
                                .addIntegerProperty("age")
                                .addNumberProperty("height")
                                .addBooleanProperty("married")
                                .required("name", "age", "height", "married") // 见下面的 [2]
                                .build())
                        .build())
                .build();

        UserMessage userMessage = UserMessage.from("""
                John is 42 years old and lives an independent life.
                He stands 1.75 meters tall and carries himself with confidence.
                Currently unmarried, he enjoys the freedom to focus on his personal goals and interests.
                """);

        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(responseFormat)
                .messages(userMessage)
                .build();
        ChatResponse response = qwenChatModel.chat(chatRequest);
        System.out.println(response.aiMessage().text());
    }

    @Test
    void test6() {
        Friend friend = AiServices.builder(Friend.class)
                .chatModel(qwenChatModel)
                .build();
        boolean positive = friend.isPositive("拎壶冲");
        System.out.println(positive);
    }

    @Test
    void test7() {
        Friend friend = AiServices.builder(Friend.class)
                .chatModel(qwenChatModel)
                .build();
        Gender gender = friend.analyzeGender("喜欢拎壶冲，张嘴就骂人，大性格豪爽，有两块胸肌，非常大");
        System.out.println(gender.name());
    }

    @Test
    void test8() {
        Friend friend = AiServices.builder(Friend.class)
                .chatModel(qwenChatModel)
                .build();
        Gender gender = friend.analyzeGender("喜欢拎壶冲，张嘴就骂人，大性格豪爽，有两块胸肌，非常大");
        System.out.println(gender.name());
    }

    @Test
    void test9() {
        Friend friend = AiServices.builder(Friend.class)
                .chatModel(qwenChatModel)

                .build();
        Person person = friend.desPerson("名叫拎壶冲，张嘴就骂人，大性格豪爽，有两块胸肌，非常大。身高180，是个萌妹子，家住北海边");
        System.out.println(Json.toJson(person));
    }


    @Autowired
    QwenStreamingChatModel qwenStreamingChatModel;

    @Test
    void test10() {
        Friend friend = AiServices.builder(Friend.class)
                .streamingChatModel(qwenStreamingChatModel)
                .build();
        TokenStream tokenStream = friend.descPerson("名叫拎壶冲，张嘴就骂人，大性格豪爽，有两块胸肌，非常大。身高180，是个萌妹子，家住北海边");
        tokenStream.onPartialResponse(System.out::println)
                .onRetrieved(System.out::println)
                .onCompleteResponse(System.out::println)
                .onError(System.out::println)
                .start();
    }

    @Test
    void test11() throws ExecutionException, InterruptedException {
        Friend friend = AiServices.builder(Friend.class)
                .streamingChatModel(qwenStreamingChatModel)
                .build();
        CompletableFuture<ChatResponse> futureRes = new CompletableFuture<>();
        TokenStream tokenStream = friend.descPerson("名叫拎壶冲，张嘴就骂人，大性格豪爽，有两块胸肌，非常大。身高180，是个萌妹子，家住北海边");
        tokenStream.onPartialResponse(System.out::println)
                .onRetrieved(System.out::println)
                .onCompleteResponse(futureRes::complete)
                .onError(futureRes::completeExceptionally)
                .start();
        ChatResponse response = futureRes.get();
        System.out.println(response.aiMessage().text());
    }

    @Test
    void test12()  {
        Friend friend = AiServices.builder(Friend.class)
                .streamingChatModel(qwenStreamingChatModel)
                .build();
        Flux<String> content = friend.descPerson2("名叫拎壶冲，张嘴就骂人，大性格豪爽，有两块胸肌，非常大。身高180，是个萌妹子，家住北海边");
        content.toIterable().forEach(System.out::println);
    }


}
