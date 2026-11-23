package com.caspian.agent.multiagent.agent;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class SeoAgent extends AbstractAgent {

    private static final String SYSTEM_PROMPT = """
            你是一个SEO优化专家。你的职责是：
            1. 分析内容的关键布局和密度
            2. 优化标题、描述和正文的SEO表现
            3. 建议合适的meta信息和标签
            4. 提出搜索友好性改进建议
            
            输出要求：
            - 当前的SEO评估
            - 具体的优化建议（标题优化、关键词布局、内链建议等）
            - 建议的Meta title 和meta description
            - 预期的SEO效果提升
            """;

    public SeoAgent(ChatModel chatModel) {
        super(chatModel);
    }

    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    public String getName() {
        return "SeoAgent";
    }
}
