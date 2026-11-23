package com.caspian.agent.graphworkflow.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class OrderQueryTool {

    @Tool(description = "根据订单或商品关键词查询订单信息，包括订单状态、金额、商口名称等")
    public String queryOrder(@ToolParam(description = "订单号或商品关键词") String query) {
        if (query.contains("2024-001") || query.contains("手表")) {
            return "订单号: ORD-2024-001, 商品:智能手表Pro,金额¥299.00，状态：已签收，" +
                    "下单时间: 2024-12-01, 收货时间: 2024-12-05。该订单符合7天无理由退货条件。";
        }
        if (query.contains("2024-002") || query.contains("耳机")) {
            return "订单号: ORD-2024-002, 商品: 无线降噪耳机, 金额: ¥599.00, 状态: 已签收, " +
                    "下单时间: 2024-11-15, 收货时间: 2024-11-20。该订单已超过退货期限。";
        }
        return "订单查询结果: 订单号ORD-2024-003, 商品: 蓝牙音箱, 金额: ¥159.00, 状态: 配送中, " +
                "预计送达时间: 2024-12-25。该订单尚未签收，建议收货后再申请退款。";
    }
}
