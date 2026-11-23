package com.caspian.agent.chatcase.config;

public class TokenCostConfig {
    // 1000 token 对应的价格（单位：元）
    public static final double QWEN_TURBO_INPUT = 0.005;    // 通义千问 turbo 输入
    public static final double QWEN_TURBO_OUTPUT = 0.010;   // 通义千问 turbo 输出

    public static final double QWEN_PLUS_INPUT = 0.015;     // 通义千问 plus
    public static final double QWEN_PLUS_OUTPUT = 0.030;

    public static final double DEFAULT_INPUT = 0.01;
    public static final double DEFAULT_OUTPUT = 0.02;
}
