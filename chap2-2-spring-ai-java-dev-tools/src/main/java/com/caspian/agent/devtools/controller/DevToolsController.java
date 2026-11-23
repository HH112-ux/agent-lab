package com.caspian.agent.devtools.controller;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DevToolsController {

    private final ChatModel chatModel;

    private final List<ToolCallback> toolCallbacks;

    @Value("${volcano.model}")
    private String model;

    public DevToolsController(ChatModel chatModel, List<ToolCallback> toolCallbacks) {
        this.chatModel = chatModel;
        this.toolCallbacks = toolCallbacks;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String userMessage) {
        try {
            SystemMessage systemMsg = new SystemMessage(
                    "你是一个 Java 开发工具箱智能助手，可以帮用户完成以下操作：\n" +
                            "1. 查询端口占用情况（checkPort）\n" +
                            "2. 杀掉占用端口的进程（killPort）\n" +
                            "3. 查看本地 Java 进程状态（listJavaProcesses）\n" +
                            "4. 执行 Maven 命令（runMaven）\n" +
                            "5. 转换文件编码为 UTF-8（convertEncoding）\n" +
                            // 【说】最后一句话特别重要："如果用户要求多个操作，请依次调用多个工具函数"。这确保了大模型能处理"先查端口再杀进程"这种复合请求。
                            "请根据用户的自然语言描述，判断需要调用哪个工具函数来完成任务。\n" +
                            "如果用户要求多个操作（如先查后杀），请依次调用多个工具函数。"
            );

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(model)
                    .toolCallbacks(toolCallbacks)
                    .build();

            Prompt prompt = new Prompt(List.of(systemMsg, new UserMessage(userMessage)), options);
            return chatModel.call(prompt).getResult().getOutput().getText();
        } catch (Exception e) {
            e.printStackTrace();
            return "调用失败：" + e.getMessage();
        }
    }
}
