package com.caspian.agent.springai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;

// 1.创建一个ChatModel Bean, 让代码能调通火山
// 2. 写一个请求拦截器，把url强制改写成火山的地址
@Configuration
public class AiConfig {

    // 属性注入
    @Value("${volcano.api-url}")
    private String apiUrl;

    @Value("${volcano.api-key}")
    private String apiKey;

    @Value("${volcano.model}")
    private String model;

    // URL拦截器
    @Bean
    public ChatModel chatModel() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestInterceptor((request, body, execution) -> {

                    HttpRequest rewritten = new HttpRequest() {
                        @Override
                        public HttpMethod getMethod() {
                            return request.getMethod();
                        }

                        @Override
                        public URI getURI() {
                            return URI.create(apiUrl);
                        }

                        @Override
                        public Map<String, Object> getAttributes() {
                            return request.getAttributes();
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            return request.getHeaders();
                        }
                    };
                    return execution.execute(rewritten, body);
                });
        // 构建OpenAiApi
        OpenAiApi api = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl("https://dummy-url")
                .restClientBuilder(restClientBuilder)
                .build();

        // 构建ChatModel
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .build();
    }
}
