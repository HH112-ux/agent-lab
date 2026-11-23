package com.caspian.agent.singleagent.agent;

public interface AgentTool {

    String execute(AgentMemory memory) throws Exception;

    String getToolName();

    String getToolDescription();
}
