package com.caspian.agent.chatcase.listener;

import com.alibaba.dashscope.utils.JsonUtils;
import com.caspian.agent.chatcase.config.TokenCostConfig;
import com.caspian.agent.chatcase.pojo.TokenBillingRecord;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class MyChatModelListener {

    @Bean
    public ChatModelListener chatModelListener() {
        return new ChatModelListener() {
            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                log.info("onRequest(): {}", requestContext.chatRequest());
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.info("onResponse(): {}", responseContext.chatResponse());
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.info("onError(): {}", errorContext.error().getMessage());
            }
        };
    }

    @Bean
    public ChatModelListener billingListener() {
        return new ChatModelListener() {

            @Override
            public void onResponse(ChatModelResponseContext context) {
                try {
                    TokenUsage usage = context.chatResponse().tokenUsage();
                    if (usage == null) return;

                    String userId = (String) context.attributes().get("userId");
                    String model = context.chatResponse().modelName();

                    int inputTokens = usage.inputTokenCount();
                    int outputTokens = usage.outputTokenCount();
                    int totalTokens = usage.totalTokenCount();

                    // 计算费用
                    BigDecimal cost = calculateCost(model, inputTokens, outputTokens);

                    // 构建记录
                    TokenBillingRecord record = new TokenBillingRecord();
                    record.setUserId(userId);
                    record.setModelName(model);
                    record.setInputTokens(inputTokens);
                    record.setOutputTokens(outputTokens);
                    record.setTotalTokens(totalTokens);
                    record.setTotalCost(cost.doubleValue());
                    record.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                    //todo 存储记录

                    // 打印计费日志
                    log.info("token用量：{}", JsonUtils.toJson(record));

                } catch (Exception e) {
                    log.error("Token计费失败", e);
                }
            }

        };
    }
    private BigDecimal calculateCost(String model, int input, int output) {
        BigDecimal inputPrice;
        BigDecimal outputPrice;

        if (model.contains("qwen-turbo")) {
            inputPrice = BigDecimal.valueOf(TokenCostConfig.QWEN_TURBO_INPUT);
            outputPrice = BigDecimal.valueOf(TokenCostConfig.QWEN_TURBO_OUTPUT);
        } else if (model.contains("qwen-plus")) {
            inputPrice = BigDecimal.valueOf(TokenCostConfig.QWEN_PLUS_INPUT);
            outputPrice = BigDecimal.valueOf(TokenCostConfig.QWEN_PLUS_OUTPUT);
        } else {
            inputPrice = BigDecimal.valueOf(TokenCostConfig.DEFAULT_INPUT);
            outputPrice = BigDecimal.valueOf(TokenCostConfig.DEFAULT_OUTPUT);
        }

        // 公式：(数量 * 单价) / 1000
        return inputPrice.multiply(BigDecimal.valueOf(input))
                .add(outputPrice.multiply(BigDecimal.valueOf(output)))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
    }

}
