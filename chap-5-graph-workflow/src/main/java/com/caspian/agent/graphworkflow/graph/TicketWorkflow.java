package com.caspian.agent.graphworkflow.graph;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.caspian.agent.graphworkflow.action.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class TicketWorkflow {
    private final CompiledGraph compiledGraph;

    public TicketWorkflow(ChatModel chatModel) {
        try {
            // 1. 创建ChatClient实例
            ChatClient chatClient = ChatClient.create(chatModel);

            // 2. 创建各节点的动作实例
            NodeActionWithConfig classifyAction = new ClassifyAction(chatClient);
            NodeActionWithConfig refundAgentAction = new RefundAgentAction(chatModel);
            NodeActionWithConfig techAgentAction = new TechAgentAction(chatModel);
            NodeActionWithConfig generalAgentAction = new GeneralAgentAction(chatClient);
            NodeActionWithConfig reviewAction = new ReviewAction(chatClient);
            NodeActionWithConfig outputAction = new OutputAction();

            // 定义状态key的策略
            KeyStrategyFactory keyStrategyFactory = () -> {
                Map<String, KeyStrategy> strategies = new HashMap<>();
                strategies.put("ticketContent", new ReplaceStrategy());
                strategies.put("category", new ReplaceStrategy());
                strategies.put("agentResponse", new ReplaceStrategy());
                strategies.put("reviewPassed", new ReplaceStrategy());
                strategies.put("retryCount", new ReplaceStrategy());
                strategies.put("finalResponse", new ReplaceStrategy());
                return strategies;
            };

            // 构建StateGraph
            StateGraph graph = new StateGraph(keyStrategyFactory);

            // 添加节点
            graph.addNode("classify", AsyncNodeActionWithConfig.node_async(classifyAction));
            graph.addNode("refundAgent", AsyncNodeActionWithConfig.node_async(refundAgentAction));
            graph.addNode("techAgent", AsyncNodeActionWithConfig.node_async(techAgentAction));
            graph.addNode("generalAgent", AsyncNodeActionWithConfig.node_async(generalAgentAction));
            graph.addNode("review", AsyncNodeActionWithConfig.node_async(reviewAction));
            graph.addNode("output", AsyncNodeActionWithConfig.node_async(outputAction));

            // 定义边 开始->分类
            graph.addEdge(StateGraph.START, "classify");

            // 定义条件边 分类节点->条件路由
            graph.addConditionalEdges("classify",
                    AsyncEdgeActionWithConfig.edge_async((state, cfg) ->
                            state.value("category", "GENERAL")),
                    Map.of("REFUND", "refundAgent",
                            "TECH", "techAgent",
                            "GENERAL", "generalAgent")
                    );

            // 各agent -> 审核节点（汇聚）
            graph.addEdge("refundAgent", "review");
            graph.addEdge("techAgent", "review");
            graph.addEdge("generalAgent", "review");

            // 审核节点 ->条件路由
            graph.addConditionalEdges("review",
                    AsyncEdgeActionWithConfig.edge_async((state, cfg) ->{
                        Boolean passed = state.value("reviewPassed", false);
                        Integer retryCount = state.value("retryCount", 0);
                        if (passed || retryCount >= 2) {
                            return "output";
                        }
                        return "classify";
                    }),
                    Map.of("output", "output", "classify", "classify")
                    );

            // 输出节点 -> END
            graph.addEdge("output", StateGraph.END);

            // 编译图
            this.compiledGraph = graph.compile();
        } catch (Exception e) {
            throw new RuntimeException("工作流图构建失败", e);
        }
    }

    public String process(String ticketContent) {
        Optional<OverAllState> result = compiledGraph.invoke(
          Map.of("ticketContent", ticketContent, "retryCount", 0),
          RunnableConfig.builder().build()
        );
        return result.map(s -> s.value("finalResponse", "处理失败")).orElse("处理失败");
    }
}
