package com.caspian.agent.multiagent.model;

import java.util.List;

public class ContentResponse {

    // 整个创作流程是否成功完成
    private boolean success;

    // 主编汇总后的最终文章
    private String finalContent;

    // 记录每个Agent的输出
    private List<StepResult> steps;

    // 记录冲突检测发现的问题
    private List<String> conflicts;

    // 总耗时
    private long totalDuration;

    public ContentResponse(boolean success, String finalContent, List<StepResult> steps, List<String> conflicts, long totalDuration) {
        this.success = success;
        this.finalContent = finalContent;
        this.steps = steps;
        this.conflicts = conflicts;
        this.totalDuration = totalDuration;
    }

    public static class StepResult {
        // agent的名字
        private String agent;

        // 这个agent的输出
        private String output;
        public StepResult() {

        }
        public StepResult(String agent, String output) {
            this.agent = agent;
            this.output = output;
        }

        public String getAgent() {
            return agent;
        }

        public void setAgent(String agent) {
            this.agent = agent;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFinalContent() {
        return finalContent;
    }

    public void setFinalContent(String finalContent) {
        this.finalContent = finalContent;
    }

    public List<StepResult> getSteps() {
        return steps;
    }

    public void setSteps(List<StepResult> steps) {
        this.steps = steps;
    }

    public List<String> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<String> conflicts) {
        this.conflicts = conflicts;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }
}
