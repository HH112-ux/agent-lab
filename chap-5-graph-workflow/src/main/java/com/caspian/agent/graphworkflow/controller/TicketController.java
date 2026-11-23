package com.caspian.agent.graphworkflow.controller;

import com.caspian.agent.graphworkflow.graph.TicketWorkflow;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    private final TicketWorkflow ticketWorkflow;

    public TicketController(TicketWorkflow ticketWorkflow) {
        this.ticketWorkflow = ticketWorkflow;
    }

    @PostMapping
    public Map<String, String> process(@RequestBody Map<String, String> request) {
        String content = request.getOrDefault("content", "");
        String result = ticketWorkflow.process(content);
        return Map.of("response", result);
    }
}
