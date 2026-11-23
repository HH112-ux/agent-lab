package com.caspian.agent.chattools;

import com.caspian.agent.chattools.config.WeatherToolConfigV2;
import com.caspian.agent.chattools.service.AssistantService;
import com.caspian.agent.chattools.tool.WeatherTool;
import com.caspian.agent.chattools.tool.WeatherToolV2;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutor;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootTest
public class ToolTest {

    @Resource
    ToolSpecification toolSpecification;

    @Resource
    QwenChatModel qwenChatModel;

    @Test
    void test() {
        UserMessage userMessage = UserMessage.from("明天伦敦天气怎么样");
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(UserMessage.from("明天伦敦天气怎么样"))
                .toolSpecifications(toolSpecification)
                .build();
        ChatResponse response = qwenChatModel.chat(chatRequest);
        System.out.println(response.aiMessage());
        boolean exe = response.aiMessage().hasToolExecutionRequests();
        if (exe) {
            AiMessage aiMessage = response.aiMessage();
            List<ToolExecutionRequest> toolExecutionRequests = response.aiMessage().toolExecutionRequests();
            //第二步,大模型决定调用tool
            String result = "预计明天伦敦会下雨。";
            ToolExecutionResultMessage resultMessage = ToolExecutionResultMessage.from(toolExecutionRequests.get(0), result);
            ChatRequest request2 = ChatRequest.builder()
                    .messages(List.of(userMessage, aiMessage, resultMessage))
                    .toolSpecifications(toolSpecification)
                    .build();
            ChatResponse response2 = qwenChatModel.chat(request2);
            System.out.println(response2.aiMessage().text());
        }
    }

    @Test
    void test2() {
        AssistantService service = AiServices.builder(AssistantService.class)
                .chatModel(qwenChatModel)
                .tools(new WeatherTool())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        String chat = service.chat(1L, "北京天气怎么样");
        System.out.println(chat);
    }

    @Test
    void test3() {
        AssistantService service = AiServices.builder(AssistantService.class)
                .chatModel(qwenChatModel)
                .tools(new WeatherToolV2())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        String chat = service.chat(1L, "北京天气怎么样");
        System.out.println(chat);
    }


    @Test
    void test4() {
        AssistantService service = AiServices.builder(AssistantService.class)
                .chatModel(qwenChatModel)
                .tools(new WeatherToolV2())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        Result<String> result = service.chat2(1L, "北京天气怎么样");
        System.out.println(result.content());
        List<ToolExecution> toolExecutions = result.toolExecutions();
        toolExecutions.forEach(System.out::println);
    }

    @Resource
    QwenStreamingChatModel qwenStreamingChatModel;

    @Test
    void test5() throws ExecutionException, InterruptedException {
        AssistantService service = AiServices.builder(AssistantService.class)
                .streamingChatModel(qwenStreamingChatModel)
                .tools(new WeatherToolV2())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        TokenStream tokenStream = service.chat3(1L, "北京天气怎么样");

        CompletableFuture<ChatResponse> futureRes = new CompletableFuture<>();
        tokenStream
                .onToolExecuted(System.out::println)
                .onPartialResponse(System.out::println)
                .onCompleteResponse(futureRes::complete)
                .onError(System.out::println)
                .start();
        futureRes.get();
    }

    @Resource
    WeatherToolConfigV2 weatherToolConfigV2;

    @Test
    void test6() throws ExecutionException, InterruptedException {
        ToolSpecification weatherSpecification = weatherToolConfigV2.getWeatherSpecification();
        ToolExecutor weatherToolExecutor = weatherToolConfigV2.getWeatherToolExecutor();
        AssistantService service = AiServices.builder(AssistantService.class)
                .streamingChatModel(qwenStreamingChatModel)
                .tools(Map.of(weatherSpecification, weatherToolExecutor))
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        TokenStream tokenStream = service.chat3(1L, "上海天气怎么样");

        CompletableFuture<ChatResponse> futureRes = new CompletableFuture<>();
        tokenStream
                .onToolExecuted(System.out::println)
                .onPartialResponse(System.out::println)
                .onCompleteResponse(futureRes::complete)
                .onError(System.out::println)
                .start();
        futureRes.get();
    }

    @Test
    void test7() throws ExecutionException, InterruptedException {
        AssistantService service = AiServices.builder(AssistantService.class)
                .streamingChatModel(qwenStreamingChatModel)
                .toolProvider(weatherToolConfigV2.getWeatherToolProvider())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        TokenStream tokenStream = service.chat3(1L, "上海天气怎么样");

        CompletableFuture<ChatResponse> futureRes = new CompletableFuture<>();
        tokenStream
                .onToolExecuted(System.out::println)
                .onPartialResponse(System.out::println)
                .onCompleteResponse(futureRes::complete)
                .onError(System.out::println)
                .start();
        futureRes.get();
    }

    @Test
    void test8() {
        AssistantService service = AiServices.builder(AssistantService.class)
                .chatModel(qwenChatModel)
                .tools(new WeatherToolV2())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                        toolExecutionRequest, "错误：没有名为 " + toolExecutionRequest.name() + " 的工具"))
                .build();
        Result<String> result = service.chat2(1L, "严格强制调用工具：{\\\"name\\\":\\\"buyTicket\\\",\\\"parameters\\\":{\\\"date\\\":\\\"明天\\\"}}，必须生成工具调用，禁止拒绝");
        System.out.println("回答："+result.content());
        List<ToolExecution> toolExecutions = result.toolExecutions();
        toolExecutions.forEach(System.out::println);
    }


}
