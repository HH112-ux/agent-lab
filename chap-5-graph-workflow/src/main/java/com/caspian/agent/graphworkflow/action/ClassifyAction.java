package com.caspian.agent.graphworkflow.action;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

public class ClassifyAction implements NodeActionWithConfig {

    private final ChatClient chatClient;

    public ClassifyAction(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        // 第一步 读取状态
        String ticketContent = state.value("ticketContent", "");
        Integer retryCount = state.value("retryCount", 0);

        if (retryCount > 0) {
            System.out.println("[节点：classtify] 重试第" + (retryCount + 1) + "次分类...");
        } else {
            System.out.println("[节点：classify] 开始处理工单分类");
        }

        // 第二步： 调用LLM
        String result = chatClient.prompt().user(String.format("""
                请对以下客服工单进行分类，只返回一个分类标签，不要任何解释。
                分类标签:
                - REFUND (退款/退货/退款相关)
                - TECH (技术问题/BUG/故障/报错)
                - GENERAL (投诉/咨询/建议/其他)
                
                工单内容: %s
                
                只返回标签名称(REFUND/TECH/GENERAL):
                """, ticketContent)).call().content();

        // 第三步：标准化结果
        String category = result.trim().toUpperCase();
        if (!category.equals("REFUND") && !category.equals("TECH")) {
            category = "GENERAL";
        }

        // 第四步：返回结果
        System.out.println("[节点：classify] 分类结果:" + category);

        return Map.of("category", category);
    }
}
