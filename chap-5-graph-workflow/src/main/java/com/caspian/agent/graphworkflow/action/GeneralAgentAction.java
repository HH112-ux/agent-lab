package com.caspian.agent.graphworkflow.action;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

public class GeneralAgentAction implements NodeActionWithConfig {
    private final ChatClient chatClient;
    public GeneralAgentAction(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        String ticketContent = state.value("ticketContent", "");
        System.out.println("[节点：generalAgent] 开始处理通用工单...");
        String response = chatClient.prompt().user(String.format("""
                你是一名专业的客服代表，擅长处理客户投诉和咨询。请针对以下的工单内容给出专业的、有同理心的回复。
                
                工单内容： %s
                
                要求：
                1. 语气友善、专业，体现同理心
                2. 承认客户的不满或解答客户的疑问
                3. 给出明确的解决方案或后续处理承诺
                4. 回复控制在200字以内
                5. 回复一定要专业、有礼貌
                6. 回复必须要直接回应了客户的问题
                """,ticketContent)).call().content();
        System.out.println("[节点：generalAgent] 通用工单处理完成");
        return Map.of("agentResponse", response);
    }
}
