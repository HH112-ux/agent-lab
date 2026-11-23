package com.caspian.agent.graphworkflow.action;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

public class ReviewAction implements NodeActionWithConfig {
    private final ChatClient chatClient;
    public ReviewAction(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        String ticketContent = state.value("ticketContent", "");
        String agentResponse = state.value("agentResponse", "");
        String category = state.value("category", "GENERAL");
        Integer retryCount = state.value("retryCount", 0);

        System.out.println("[节点：review] 开始审核回复质量...");

        String reviewResult = chatClient.prompt().user(String.format("""
                请审核以下客服回复的质量。
                
                原始工单： %s
                工单分类： %s
                客服回复： %s
                
                审核标准：
                1. 回复是否直接回应了客户的问题
                2. 回复是否专业、有礼貌
                3. 是否给出了明确的解决方案和后续的步骤
                
                请只回复PASS或FAIL:
                """, ticketContent, category, agentResponse)).call().content();
        boolean passed = reviewResult.trim().toUpperCase().contains("PASS");
        System.out.println("[节点：review] 审核结果:" + (passed ? "PASS" : "FAIL(将重新处理)"));
        if (passed) {
            return Map.of("reviewPassed", true);
        } else {
            return Map.of("reviewPassed", false, "retryCount", retryCount + 1);
        }
    }
}
