package com.caspian.agent.multiagent.agent;

import com.caspian.agent.multiagent.protocol.A2AMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

public abstract class AbstractAgent implements Agent {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ChatModel chatModel;

    protected AbstractAgent(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    protected abstract String getSystemPrompt();

    @Override
    public A2AMessage process(A2AMessage message) {
        log.info("[{}] 收到来自{}的任务", getName(), message.getFromAgent());

        long start = System.currentTimeMillis();

        String result = callLlm(getSystemPrompt(), message.getContent());

        long elapsed = System.currentTimeMillis();

        log.info("[{}] 完成处理， 耗时{}ms | 输出预览: {}", getName(), elapsed, preview(result, 80));

        return new A2AMessage(getName(), message.getFromAgent(), result, A2AMessage.MessageType.TASK_RESULT);
    }

    protected String preview(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        String flat = text.replace("\n", " ").trim();
        return flat.length() > maxLen ? flat.substring(0, maxLen) + "..." : flat;
    }

    protected String callLlm(String systemPrompt, String userMessage) {
        Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt),
                new UserMessage(userMessage)));
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }


}
