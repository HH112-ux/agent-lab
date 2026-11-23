package com.caspian.agent.multiagent.agent;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class WritingAgent extends AbstractAgent {
    private static final String SYSTEM_PROMPT = """
            你是一个资深的内容创作者。你的职责是：
            1. 基于提供的调研素材和任务方案，撰写高质量的内容初稿
            2. 确保文章结构清晰，逻辑连贯，语言流畅
            3. 融入关键信息和核心论据
            4. 保持指定的写作风格
            
            输出要求：
            - 标题吸引人且准确概括内容
            - 段落分明， 过渡自然
            - 论点有理有据
            - 语言风格与要求一致
            """;

    public WritingAgent(ChatModel chatModel) {
        super(chatModel);
    }

    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    public String getName() {
        return "WritingAgent";
    }
}
