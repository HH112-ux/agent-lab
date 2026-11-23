package com.caspian.agent.singleagent;

import com.caspian.agent.singleagent.agent.ContractAuditAgent;

import java.util.Scanner;

public class Main {

    public static void main(String [] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║       智能合同审核与合规校验单Agent系统            ║");
        System.out.println("║       (纯Java原生 / 零中间件 / 完全离线)          ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        String contractPath;
        if (args.length >0) {
            contractPath = args[0];
        } else {
            contractPath = "contracts/样例房屋租赁合同.txt";
            System.out.println("[提示] 未指定合同路径，使用默认样例合同: " + contractPath);
            System.out.println("[提示] 也可通过命令行参数指定: java com.caspian.agent.singleagent.Main <合同文件路径>");
            System.out.println();
            System.out.print("请输入合同文件路径（直接回车使用默认样例）: ");
//            Scanner scanner = new Scanner(System.in);
//            String input = scanner.nextLine().trim();
//            if (!input.isEmpty()) {
//                contractPath = input;
//            }
        }
        System.out.println();
        ContractAuditAgent agent = new ContractAuditAgent();
        agent.runAudit(contractPath);
    }
}
