package com.caspian.agent.chatrag.controller;

import com.caspian.agent.chatrag.service.AdvancedRagService;
import com.caspian.agent.chatrag.service.DocumentIngestService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rag")
public class RagController {

    @Resource
    private AdvancedRagService advancedRagService;

    @Resource
    private DocumentIngestService documentIngestService;

    @GetMapping("/ingest")
    public String ingest() {
        documentIngestService.ingestDocuments();
        return "文档入库成功！";
    }

    @GetMapping("/chat/{message}")
    public String chat(@PathVariable String message) {
        return advancedRagService.advancedChat(message);
    }
}
