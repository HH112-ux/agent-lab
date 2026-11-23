package com.caspian.agent.graphworkflow.action;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import com.caspian.agent.graphworkflow.tools.KnowledgeBaseTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Map;

public class TechAgentAction implements NodeActionWithConfig {

    private final ChatClient chatClient;

    public TechAgentAction(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).defaultTools(new KnowledgeBaseTool()).build();
    }


    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
        String ticketContent = state.value("ticketContent", "");
        System.out.println("[节点：techAgent] 开始处理技术支持工单...");
        System.out.println("[节点：techAgent] 按需调用工具: knowledgeBaseTool");

        String response = chatClient.prompt()
                .user(String.format("""
                        你是一名专业的技术支持工程师。请处理以下技术问题。
                        请先使用知识库工具查询相关的解决方案，然后基于检索的结果给出详细的技术回复。
                        
                        问题描述：%s
                        
                        要求：
                        1. 先搜索知识库查找相关技术文档
                        2. 基于知识库的内容给出具体的解决步骤
                        3. 如果知识库没有直接答案，给出通用的排查建议
                        4. 回复一定要专业、一定要有礼貌
                        5. 回复必须要直接回应了客户的问题
                        6. 必须给出了明确的解决方案和后续的步骤
                        """, ticketContent)).call().content();
        System.out.println("[节点：techAgent] 技术支持处理完成");
        return Map.of("agentResponse", response);
    }
}
