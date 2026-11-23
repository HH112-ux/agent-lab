package com.caspian.agent.multiagent.agent;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class ReviewAgent extends AbstractAgent {
    private static final String SYSTEM_PROMPT = """
            你是一个严格的内容审核专家。你的职责是：
            1. 审核文章的逻辑性，准确性和完整性
            2. 检查是否存在事实性错误或逻辑漏洞
            3. 评估内容的专业性和可读性
            4. 给出具体的修改建议
            
            输出要求：
            - 列出发现的问题（如有）
            - 对每个问题给出修改建议
            - 给出整体的质量评分（1-10分）
            - 总结文章的优点和不足
            """;

    public ReviewAgent(ChatModel chatModel) {
        super(chatModel);
    }

    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    public String getName() {
        return "ReviewAgent";
    }
}
