package com.caspian.agent.multiagent.config;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class AiConfig {
    @Value("${volcano.chat.api-url}")
    private String apiUrl;

    @Value("${volcano.chat.api-key}")
    private String apiKey;

    @Value("${volcano.chat.model}")
    private String model;

    @Bean
    public ChatModel chatModel(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        return new VolcanoChatModel(restTemplate, apiUrl, apiKey, model);
    }

    static class VolcanoChatModel implements ChatModel {
        private final RestTemplate restTemplate;
        private final String apiUrl;
        private final String apiKey;
        private final String model;

        public VolcanoChatModel(RestTemplate restTemplate, String apiUrl, String apiKey, String model) {
            this.restTemplate = restTemplate;
            this.apiUrl = apiUrl;
            this.apiKey = apiKey;
            this.model = model;
        }

        public ChatResponse call(Prompt prompt) {
            List<Map<String, String>> messages = prompt.getInstructions().stream()
                    .map(msg -> Map.of(
                            "role", msg.getMessageType().getValue(),
                            "content", msg.getText()
                    )).toList();

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            String content = (String) message.get("content");
            return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
        }
    }
}
