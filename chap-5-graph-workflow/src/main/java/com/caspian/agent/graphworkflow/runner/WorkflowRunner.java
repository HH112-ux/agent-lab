package com.caspian.agent.graphworkflow.runner;

import com.caspian.agent.graphworkflow.graph.TicketWorkflow;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WorkflowRunner implements CommandLineRunner {

    private final TicketWorkflow ticketWorkflow;

    public WorkflowRunner(TicketWorkflow ticketWorkflow) {
        this.ticketWorkflow = ticketWorkflow;
    }

    @Override
    public void run(String... args) throws Exception {
        //打印
        System.out.println("\n=======智能客服工单处理系统 - Graph工作流演示 ===========");
        System.out.println("知识点：: 线性工作流 vs Graph工作流 | 核心架构与节点类型 | 节点定义与流转逻辑 | Agent与外部工具集成");
        System.out.println("==========================================\n");

        String [] tickets = {
                "我买的产品(订单号ORD-2024-001)有质量问题，要求退款！",
                "App登录页面报错500，一直无法登录，请尽快修复！",
                "你们的服务态度太差了！等了半小时都没人接电话！"
        };

        for (int i = 0; i < tickets.length; i ++) {
            System.out.println("---工单" + (i + 1) + ": " + tickets[i] + " ---");
            long startTime = System.currentTimeMillis();

            try {
                String response = ticketWorkflow.process(tickets[i]);
                System.out.println("[最终回复]" + response);
            } catch (Exception ex) {
                System.out.println("[错误] 工单处理失败: " + ex.getMessage());
            }
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("[耗时]" + elapsed + "ms\n");
        }
        System.out.println("==========================================");
        System.out.println("====================所有工单处理完成======================");
    }
}
