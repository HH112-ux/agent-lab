package com.caspian.agent.multiagent.agent;

import com.caspian.agent.multiagent.protocol.A2AMessage;

public interface Agent {

    String getName();

    A2AMessage process(A2AMessage message);
}
