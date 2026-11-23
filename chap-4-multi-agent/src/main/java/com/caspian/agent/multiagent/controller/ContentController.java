package com.caspian.agent.multiagent.controller;

import com.caspian.agent.multiagent.engine.AgentEngine;
import com.caspian.agent.multiagent.model.ContentRequest;
import com.caspian.agent.multiagent.model.ContentResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final AgentEngine agentEngine;

    public ContentController(AgentEngine agentEngine) {
        this.agentEngine = agentEngine;
    }

    @PostMapping("/create")
    public ContentResponse create(@RequestBody ContentRequest request) {
        return agentEngine.execute(request);
    }
}
