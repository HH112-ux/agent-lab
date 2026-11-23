package com.caspian.agent.chatrag.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AssistantService {

    @SystemMessage("""
            你是一个专业的智能助手，基于提供的知识库内容回答用户问题。
            规则：
            1. 只使用提供的知识库信息回答
            2. 不知道就说“根据现有知识无法回答”，不要编造
            3. 回答简洁、准确、有条理
            4. 保持友好自然
            """)
    String chat(@UserMessage String message);
}
