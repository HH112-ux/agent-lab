package com.caspian.agent.singleagent.tools;

import com.caspian.agent.singleagent.agent.AgentMemory;
import com.caspian.agent.singleagent.agent.AgentTool;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PersistExperienceTool implements AgentTool {

    @SuppressWarnings("unchecked")
    @Override
    public String execute(AgentMemory memory) throws Exception {
        String summary = (String) memory.getWorking("experienceSummary");
        if (summary == null || summary.isEmpty()) {
            return "异常：未设置经验摘要（工作记忆experienceSummary为空）";
        }

        List<Map<String, String>> riskItems = (List<Map<String, String>>) memory.getWorking("riskItems");
        if (riskItems == null) {
            riskItems = List.of();
        }

        String contractPath = memory.getShortTerm("contractPath");
        String fileName = contractPath != null ? Paths.get(contractPath).getFileName().toString() : "未知";

        Map<String, Object> caseItem = new LinkedHashMap<>();
        caseItem.put("id", "CASE-" + System.currentTimeMillis());
        caseItem.put("contractName", fileName);
        caseItem.put("contractType", textOrEmpty(memory.getWorking("queryContractType")));
        caseItem.put("auditTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        caseItem.put("riskCount", riskItems.size());
        caseItem.put("highRiskCount", countByLevel(riskItems, "高"));
        caseItem.put("mediumRiskCount", countByLevel(riskItems, "中"));
        caseItem.put("lowRiskCount", countByLevel(riskItems, "低"));
        caseItem.put("category", textOrEmpty(memory.getWorking("queryCategory")));
        caseItem.put("keywords", textOrEmpty(memory.getWorking("queryKeyword")));
        caseItem.put("summary", summary);

        if (riskItems.isEmpty()) {
            // 模型没按风险清单协议输出时，退回扁平结构，保证案例信息量不低于种子数据
            caseItem.put("riskDescription", summary);
            caseItem.put("suggestion", "");
        } else {
            caseItem.put("risks", new ArrayList<>(riskItems));
        }

        memory.addHistoricalCase(caseItem);
        memory.saveLongTermMemory();
        return "审核经验已沉淀：案例已写入长期记忆（含 " + riskItems.size() + " 条结构化风险，当前共 "
                + memory.getHistoricalCases().size() + " 个案例)";
    }

    private static int countByLevel(List<Map<String, String>> risks, String level) {
        int n = 0;
        for (Map<String, String> risk : risks) {
            if (level.equals(risk.getOrDefault("level", ""))) {
                n++;
            }
        }
        return n;
    }

    private static String textOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    @Override
    public String getToolName() {
        return "沉淀审核经验";
    }

    @Override
    public String getToolDescription() {
        return "将本次审核结论写入长期记忆数据库，供后续审核复用。需要先通过工作记忆设置experienceSummary（本次审核的风险摘要和关键发现）；"
                + "若同时提供了riskItems（逐条风险，含level/description/suggestion），会一并沉淀为结构化案例。";
    }
}
