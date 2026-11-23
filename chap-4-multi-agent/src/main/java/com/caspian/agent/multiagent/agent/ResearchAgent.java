package com.caspian.agent.multiagent.agent;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class ResearchAgent extends AbstractAgent {

    public ResearchAgent(ChatModel chatModel) {
        super(chatModel);
    }

    private static final String SYSTEM_PROMPT = """
            你是一个专业的内容调研分析师。你的职责是：
            1. 根据给定的主题和方向，进行深入的知识分析
            2. 提供相关的背景知识、最新趋势和关键数据
            3. 列出核心要点和支撑论据
            4. 指出可能的争议点和不同观点
            
            输出要求：
            - 用结构化的方式呈现调研结果
            - 区分事实和观点
            - 标注关键信息的可信度
            """;
    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    public String getName() {
        return "ResearchAgent";
    }
}
