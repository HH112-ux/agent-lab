package com.caspian.agent.chattools.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WeatherToolConfigV2 {

    public ToolSpecification getWeatherSpecification() {
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

    // 注入/创建 ObjectMapper，用于解析 JSON 参数
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public ToolExecutor getWeatherToolExecutor() {
        return (toolExecutionRequest, memoryId) -> {
            Map<String, Object> arguments = null;
            try {
                arguments = OBJECT_MAPPER.readValue(
                        toolExecutionRequest.arguments(),
                        new TypeReference<>() {
                        }
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            String city = arguments.get("city").toString();

            System.out.println("city :" + city + " + memoryId: " + memoryId);

            // 执行业务逻辑
            return getWeather(city);
        };
    }


    public ToolProvider getWeatherToolProvider() {
        return (toolProviderRequest) -> {
            if (toolProviderRequest.userMessage().singleText().contains("北京")) {
                return ToolProviderResult.builder()
                        .add(getWeatherSpecification(), getWeatherToolExecutor())
                        .build();
            } else {
                return null;
            }
        };
    }

    private String getWeather(String city) {
        return city + "天气晴朗";
    }
}
