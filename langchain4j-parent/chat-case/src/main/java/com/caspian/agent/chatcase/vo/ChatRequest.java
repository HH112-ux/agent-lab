package com.caspian.agent.chatcase.vo;

import lombok.Data;

@Data
public class ChatRequest {

    private String message;

    private String conversationId;
}
