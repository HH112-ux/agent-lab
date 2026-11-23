package com.caspian.agent.mcpclient;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.resolution.StaticToolCallbackResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;


// 要做四件事 1.初始化配置 2.创建自定义的OpenAiApi Bean,要把请求重定向到火山 3. 创建工具回调列表，从MCP Server获取所有可用的工具 4. 创建ChatModel Bean， 把API和工具组装到一起
@Configuration
public class AiConfig {
    @Value("${volcano.api-url}")
    private String apiUrl;

    @Value("${volcano.api-key}")
    private String apiKey;

    @Value("${volcano.model}")
    private String model;


    @Bean
    public OpenAiApi openAiApi() {
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

        return OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl("https://dummy-url")
                .restClientBuilder(restClientBuilder)
                .build();
    }

    @Bean
    public List<ToolCallback> toolCallbacks(ToolCallbackProvider toolCallbackProvider) {
        return List.of(toolCallbackProvider.getToolCallbacks());
    }


    @Bean
    public ChatModel chatModel(OpenAiApi api, List<ToolCallback> toolCallbacks) {
        var toolCallingMananger = DefaultToolCallingManager.builder()
                .toolCallbackResolver(new StaticToolCallbackResolver(toolCallbacks))
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .toolCallingManager(toolCallingMananger)
                .build();
    }
}
