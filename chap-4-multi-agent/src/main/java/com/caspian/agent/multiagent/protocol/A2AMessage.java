package com.caspian.agent.multiagent.protocol;

public class A2AMessage {

    // 消息类型
    public enum MessageType {
        // 任务分配
        TASK_ASSIGN,
        // 任务结果
        TASK_RESULT,
        // 冲突报告
        CONFLICT_REPORT
    }

    // 来源， 目标， 信的内容， 信的类型
    private String fromAgent;
    private String toAgent;
    private String content;
    private MessageType type;

    public A2AMessage() {

    }

    public A2AMessage(String fromAgent, String toAgent, String content, MessageType type) {
        this.fromAgent = fromAgent;
        this.toAgent = toAgent;
        this.content = content;
        this.type = type;
    }

    public String getFromAgent() {
        return fromAgent;
    }

    public void setFromAgent(String fromAgent) {
        this.fromAgent = fromAgent;
    }

    public String getToAgent() {
        return toAgent;
    }

    public void setToAgent(String toAgent) {
        this.toAgent = toAgent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String toString() {
        return "A2AMessage{from='%s', to='%s', type=%s, content='%s'}"
                .formatted(fromAgent, toAgent, type,
                        content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content);
    }
}
