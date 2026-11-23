package com.caspian.agent.graphworkflow.action;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;

import java.util.Map;

public class OutputAction implements NodeActionWithConfig {
    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        String agentResponse = state.value("agentResponse", "");
        String category = state.value("category", "UNKNOWN");
        Integer retryCount = state.value("retryCount", 0);

        // 格式化最终的回复
        String finalResponse = String.format(
                "[分类: %s] [处理次数: %d]\n%s",
                category, retryCount + 1, agentResponse
        );
        System.out.println("[节点: output] 最终回复已生成");
        return Map.of("finalResponse", finalResponse);
    }
}
