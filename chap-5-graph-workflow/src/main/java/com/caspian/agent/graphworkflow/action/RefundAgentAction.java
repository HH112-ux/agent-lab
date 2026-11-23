package com.caspian.agent.graphworkflow.action;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import com.caspian.agent.graphworkflow.tools.OrderQueryTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Map;

public class RefundAgentAction implements NodeActionWithConfig {

    private final ChatClient chatClient;

    public RefundAgentAction(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultTools(new OrderQueryTool())
                .build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        String ticketContent = state.value("ticketContent", "");
        System.out.println("[节点：refundAgent] 开始处理退款工单...");
        System.out.println("[节点：refundAgent] 按需调用工具：OrderQueryTool");

        String response = chatClient.prompt().user(String.format("""
                你是一名专业的退款处理专员。请处理以下退款工单。
                请先使用订单查询工具查询相关的订单信息，然后基于查询结果给出专业的处理方案。
                
                工单内容： %s
                
                要求：
                1. 先查询订单信息确认订单状态
                2. 根据订单状态判断是否符合退款条件
                3，给出明确的处理建议和后续的步骤
                4. 回复一定要专业、有礼貌
                5. 回复必须要直接回应了客户的问题
                """, ticketContent)).call().content();
        System.out.println("[节点：refundAgent] 退款处理完成");
        return Map.of("agentResponse", response);
    }
}
