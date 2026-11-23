package com.caspian.agent.chatcase.pojo;

import lombok.Data;

@Data
public class TokenBillingRecord {
    private String userId;          // 用户ID
    private String modelName;       // 模型名称
    private int inputTokens;        // 输入token
    private int outputTokens;       // 输出token
    private int totalTokens;        // 总token
    private double totalCost;       // 总费用
    private String createTime;
}
