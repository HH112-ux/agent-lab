package com.caspian.agent.chattools.config;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeatherToolConfig {

    @Bean
    public ToolSpecification weatherToolSpecification() {
        return ToolSpecification
                .builder()
                .name("getWeather")
                .description("返回给定城市的天气预报")
                .parameters(JsonObjectSchema
                        .builder()
                        .addStringProperty("city", "应返回天气预报的城市")
                        .required("city")
                        .build())
                .build();
    }

}
