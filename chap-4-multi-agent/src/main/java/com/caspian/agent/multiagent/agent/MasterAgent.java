package com.caspian.agent.multiagent.agent;

import com.caspian.agent.multiagent.model.ContentRequest;
import com.caspian.agent.multiagent.protocol.A2AMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MasterAgent extends AbstractAgent {

    private static final String SYSTEM_PROMPT = """
            你是一个内容创作总协调者。负责统筹多个专业Agent完成内容创作。
            你需要：
            1. 深入理解用户的创作需求
            2. 将需求拆解为清晰的子任务
            3. 综合各专业的Agent的输出，产出高质量的最终内容
            输出要求： 结构清晰、内容完整、逻辑连贯。
            """;


    public MasterAgent(ChatModel chatModel) {
        super(chatModel);
    }

    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    public String getName() {
        return "MasterAgent";
    }

    /**
     * 分析用户需求，生成任务拆解方案
     * @param request
     * @return
     */
    public String analyze(ContentRequest request) {
        String userMessage = """
                请分析以下内容创作请求，给出详细的任务拆解方案，包括：
                - 调研重点：需要谳研哪些方面
                - 写作方向：文章的核心论点和结构建议
                - 审核关注点： 需要特别注意的质量方面
                - SEO关键策略： 基于关键词的优化
                
                主题： %s
                关键词： %s
                风格要求： %s
                """.formatted(request.getTopic(), request.getKeywords() != null ? String.join("、", request.getKeywords()) : "无",
        request.getStyle() != null ? request.getStyle() : "专业易懂");
        return callLlm(SYSTEM_PROMPT, userMessage);
    }

    /**
     * 聚合所有子Agent的结果，生成最终的内容
     * @param results
     * @param conflictLog
     * @return
     */
    public String aggregate(List<A2AMessage> results, List<String> conflictLog) {
        StringBuilder sb = new StringBuilder();
        sb.append("请将以下各专业的Agent的输出综合为一篇完整的高质量内容。 \n");
        sb.append("要求： 格合各Agent的专业建议，去除冗余，保持逻辑连贯，输出最终的文章。 \n\n");

        if (!conflictLog.isEmpty()) {
            sb.append("## 已解决的冲突（请在最终的内容中体现解决方案）\n");
            for (String conflict : conflictLog) {
                sb.append("- ").append(conflict).append("\n");
            }
            sb.append("\n");
        }

        sb.append("## 各Agent输出\n\n");
        for (A2AMessage msg : results) {
            sb.append("### ").append(msg.getFromAgent()).append("\n");
            sb.append(msg.getContent()).append("\n\n");
        }

        return callLlm(SYSTEM_PROMPT, sb.toString());
    }
}
