package com.caspian.agent.multiagent.engine;

import com.caspian.agent.multiagent.agent.AbstractAgent;
import com.caspian.agent.multiagent.agent.Agent;
import com.caspian.agent.multiagent.agent.MasterAgent;
import com.caspian.agent.multiagent.conflict.ConflictResolver;
import com.caspian.agent.multiagent.model.ContentRequest;
import com.caspian.agent.multiagent.model.ContentResponse;
import com.caspian.agent.multiagent.protocol.A2AMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AgentEngine {
    private static final Logger log = LoggerFactory.getLogger(AgentEngine.class);

    private static final String SEP = "===============================================================================";

    private final Map<String, Agent> agentRegistry;

    private final MasterAgent masterAgent;

    private final ConflictResolver conflictResolver;

    public AgentEngine(List<Agent> agentList, MasterAgent masterAgent, ConflictResolver conflictResolver) {
        this.masterAgent = masterAgent;
        this.conflictResolver = conflictResolver;
        this.agentRegistry = agentList.stream().filter(a -> !(a instanceof MasterAgent)).collect(Collectors.toMap(Agent::getName, Function.identity()));
        log.info(SEP);
        log.info(" 多Agent协同引擎启动");
        log.info(" 已注册子Agent：{}", agentRegistry.keySet());
        log.info(" 主Agent：MasterAgent");
        log.info(SEP);
    }

    public ContentResponse execute(ContentRequest request) {
        // 记录时间
        long startTime = System.currentTimeMillis();
        List<ContentResponse.StepResult> steps = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();

        // 先打印请求摘要
        log.info(SEP);
        log.info(" 收到创作请求");
        log.info(" 主题：{}", request.getTopic());
        log.info(" 关键词：{}", request.getKeywords());
        log.info(" 风格：{}", request.getStyle());
        log.info(SEP);

        try {
            // 阶段1: MasterAgent分析
            log.info(">>> [阶段1/6] MasterAgent分析请求，生成任务拆解方案...");
            long t1 = System.currentTimeMillis();
            String taskPlan = masterAgent.analyze(request);
            log.info(">>> [阶段1/6] MasterAgent分析完成，耗时{}ms", System.currentTimeMillis() - t1);
            log.info("  任务方案预览：{}", preview(taskPlan, 100));

            // 阶段2: ResearchAgent调研
            log.info(">>> 【阶段2/6] 调度ResearchAgent进行调研...");
            A2AMessage researchInput = new A2AMessage("MasterAgent", "ResearchAgent", taskPlan, A2AMessage.MessageType.TASK_ASSIGN);
            A2AMessage researchResult = dispatch("ResearchAgent", researchInput);
            steps.add(new ContentResponse.StepResult("ResearchAgent", researchResult.getContent()));

            // 阶段3: 撰写
            log.info(">>> [阶段3/6] 调度 WritingAgent撰写初稿...");
            String writingInput = taskPlan + "\n\n##调研素材\n" + researchResult.getContent();
            A2AMessage writingMsg = new A2AMessage("MasterAgent", "WritingAgent", writingInput, A2AMessage.MessageType.TASK_ASSIGN);
            A2AMessage writingResult = dispatch("WritingAgent", writingMsg);
            steps.add(new ContentResponse.StepResult("WritingAgent", writingResult.getContent()));

            // 阶段4: Review+SEO并行
            log.info(">>>[阶段4/6] 并行调度 ReviewAgent + SeoAgent");

            CompletableFuture<A2AMessage> reviewFuture = CompletableFuture.supplyAsync(() -> {
                log.info(" [并行] ReviewAgent开始工作...");
                A2AMessage msg = new A2AMessage("MasterAgent", "ReviewAgent", writingResult.getContent(), A2AMessage.MessageType.TASK_ASSIGN);
                return  dispatch("ReviewAgent", msg);
            });
            CompletableFuture<A2AMessage> seoFuture = CompletableFuture.supplyAsync(() -> {
                log.info(" [并行] SEOAgent开始工作...");
                A2AMessage msg = new A2AMessage("MasterAgent", "SeoAgent", writingResult.getContent(), A2AMessage.MessageType.TASK_ASSIGN);
                return dispatch("SeoAgent", msg);
            });

            // join
            A2AMessage reviewResult = reviewFuture.join();
            A2AMessage seoResult = seoFuture.join();
            log.info(">>>[阶段4/6] ReviewAgent + SeoAgent 均已完成");
            steps.add(new ContentResponse.StepResult("ReviewAgent", reviewResult.getContent()));
            steps.add(new ContentResponse.StepResult("SeoAgent", seoResult.getContent()));

            // 阶段5，冲突检测
            log.info(">>> [阶段5/6] ConflictResolver冲突检测...");
            long t5 = System.currentTimeMillis();
            List<A2AMessage> allResults = List.of(researchResult, writingResult, reviewResult, seoResult);
            List<A2AMessage> resolvedResults = conflictResolver.resolve(allResults, conflicts);
            log.info(">>>[阶段5/6] 冲突检测完成， 耗时{}ms | 冲突数：{}", System.currentTimeMillis() - t5, conflicts.size());

            // 阶段6 masterAgent聚合
            log.info(">>>[阶段6/6] MasterAgent 聚合终稿");
            long t6 = System.currentTimeMillis();
            String finalContent = masterAgent.aggregate(resolvedResults, conflicts);
            log.info(">>>[阶段6/6] 聚合完成，耗时{}ms", System.currentTimeMillis() - t6);

            // 完成
            long duration = System.currentTimeMillis() - startTime;
            log.info(SEP);
            log.info(" 创作完成！ 总耗时{}ms 约{}秒", duration, duration/1000);
            log.info("参考Agent数:{}", steps.size() + 2);
            log.info("冲突解决：{}条", conflicts.size());
            log.info(SEP);
            log.info("最终内容:\n{}", finalContent);
            log.info(SEP);

            return new ContentResponse(true, finalContent, steps, conflicts, duration);
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error(SEP);
            log.error(" 创作失败！ 耗时{}ms", duration);
            log.error(" 异常：{}", ex.getMessage());
            log.error(SEP);
            return new ContentResponse(false, "创作失败：" + ex.getMessage(), steps, conflicts, duration);
        }
     }

    private String preview(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        String flat = text.replace("\n", " ").trim();
        return flat.length() > maxLen ? flat.substring(0, maxLen) + "..." : flat;
    }

    private A2AMessage dispatch(String agentName, A2AMessage message) {
        Agent agent = agentRegistry.get(agentName);
        if (agent == null) {
            throw new IllegalStateException("Agent不存在：" + agentName);
        }
        return agent.process(message);
    }
}


