package com.caspian.agent.multiagent.conflict;

import com.caspian.agent.multiagent.protocol.A2AMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConflictResolver {

    private static final Logger log = LoggerFactory.getLogger(ConflictResolver.class);

    private static final String SYSTEM_PROMPT = """
            你是一个多 Agent 输出的冲突检测与解决专家。
            你的任务是检查多个 Agent 的输出是否存在矛盾或冲突，并给出解决建议。

            检查维度：
            - 事实性冲突：不同 Agent 给出了相互矛盾的事实
            - 观点冲突：不同 Agent 的建议相互矛盾
            - 风格冲突：不同 Agent 的文风或调性不一致

            如果发现冲突，请：
            1. 明确指出冲突内容
            2. 分析冲突原因
            3. 给出推荐解决方案
            如果没有冲突，请直接回复"无冲突"。
            """;

    private static final int  MAX_RETRIES = 2;
    private final ChatModel chatModel;
    public ConflictResolver(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public List<A2AMessage> resolve(List<A2AMessage> results, List<String> conflictLog) {
        log.info("开始分析{}个Agent的输出，检测冲突...", results.size());

        String userMessage = buildDetectionPrompt(results);

        String analysis;

        try {
            analysis = callLlmWithRetry(SYSTEM_PROMPT, userMessage);
        } catch (Exception ex) {
            log.warn("冲突检测LLM调用失败（已降级，使用原始结果）: {}", ex.getMessage());
            conflictLog.add("冲突检测失败（已降级）：" + ex.getMessage());
            return results;
        }
        if (analysis.contains("无冲突")) {
            log.info("冲突检测结果：无冲突，所有Agent输出一致");
            return results;
        }

        log.info("冲突检测结果：发现冲突！");
        log.info("冲突详情：{}", preview(analysis, 150));
        conflictLog.add(analysis);
        return results;
    }

    private String buildDetectionPrompt(List<A2AMessage> results) {
        StringBuilder sb = new StringBuilder("请检查以下各Agent输出之间是否存在冲突：\n\n");
        for (int i = 0; i < results.size(); i ++) {
            A2AMessage msg = results.get(i);
            sb.append("### Agent ").append(i + 1).append(": ").append(msg.getFromAgent()).append("\n");
            sb.append(msg.getContent()).append("\n\n");
        }

        return sb.toString();
    }

    private String callLlmWithRetry(String systemPrompt, String userMessage) {
        Exception lastException = null;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt ++) {
            try {
                if (attempt > 0) {
                    log.info("第{}次重试冲突检测...", attempt);
                }
                Prompt prompt = new Prompt(List.of(
                        new SystemMessage(systemPrompt),
                        new UserMessage(userMessage)
                ));
                ChatResponse response = chatModel.call(prompt);
                return response.getResult().getOutput().getText();
            } catch (Exception e) {
                lastException = e;
                log.warn("冲突检测第{}次调用失败：{}", attempt + 1, e.getMessage());
            }
        }
        throw new RuntimeException("LLM调用失败，已重试 " + MAX_RETRIES + "次", lastException);
    }

    private String preview(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        String flat = text.replace("\n", " ").trim();
        return flat.length() > maxLen ? flat.substring(0, maxLen) + "..." : flat;
    }
}
