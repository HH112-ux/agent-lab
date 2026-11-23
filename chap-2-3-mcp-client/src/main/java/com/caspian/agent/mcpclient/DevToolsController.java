package com.caspian.agent.mcpclient;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DevToolsController {

    private final ChatModel chatModel;

    private final List<ToolCallback> tooCallbacks;

    @Value("${volcano.model}")
    private String model;

    public DevToolsController(ChatModel chatModel, List<ToolCallback> toolCallbacks) {
        this.chatModel = chatModel;
        this.tooCallbacks = toolCallbacks;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String userMessage) {
        try {
            SystemMessage systemMessage = new SystemMessage(
                    "你是一个 Java 开发工具箱智能助手，通过 MCP 协议调用远程工具，可以帮用户完成以下操作：\n" +
                            "1. 查询端口占用情况（check_port）\n" +
                            "2. 查询本地进程状态（query_process）\n" +
                            "3. 执行 Maven 命令（run_maven）\n" +
                            "请根据用户的自然语言描述，判断需要调用哪个工具来完成任务。\n" +
                            "如果用户要求多个操作，请依次调用多个工具。\n" +
                            "重要：每次调用工具后，在回复开头用【MCP工具：工具名(参数)】标注你调用了哪个工具，然后再给出详细结果。例如：【MCP工具：run_maven(--version)】"

            );
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(model)
                    .toolCallbacks(tooCallbacks)
                    .build();

            Prompt prompt = new Prompt(List.of(systemMessage, new UserMessage(userMessage)), options);

            return chatModel.call(prompt).getResult().getOutput().getText();
        } catch (Exception e) {
            e.printStackTrace();
            return "调用失败： " + e.getMessage();
        }
    }
}
