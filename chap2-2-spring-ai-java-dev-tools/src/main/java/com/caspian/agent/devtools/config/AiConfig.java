package com.caspian.agent.devtools.config;

import com.caspian.agent.devtools.tools.DevTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
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

/**
 * 1.创建DevTools的实例，
 * 2.把五个工具函数注册成为ToolCallback，
 * 3.构建跟大模型通信用的ChatModel Bean
 */

@Configuration
public class AiConfig {
    @Value("${volcano.api-url}")
    private String apiUrl;
    @Value("${volcano.api-key}")
    private String apiKey;
    @Value("${volcano.model}")
    private String model;

    @Bean
    public DevTools devTools(){
        return new DevTools();
    }

    @Bean
    public List<ToolCallback> toolCallbacks(DevTools tools) {
        return List.of(
                FunctionToolCallback.builder("checkPort", tools::checkPort)
                        .description("查询本地指定端口的占用情况，返回占用该端口的进程PID和进程名称。参数：port（int，端口号）")
                        .inputType(DevTools.PortRequest.class)
                        .build(),
                FunctionToolCallback.builder("killPort", tools::killPort)
                        .description("杀掉占用指定端口的所有进程。输入端口号，自动查找并强制终止占用该端口的进程。参数：port（int，端口号）")
                        .inputType(DevTools.PortRequest.class)
                        .build(),
                FunctionToolCallback.builder("listJavaProcesses", tools::listJavaProcesses)
                        .description("查询本地运行的所有 Java 进程，返回 PID、主类名、JVM 参数等信息。无需输入参数")
                        .inputType(DevTools.EmptyRequest.class)
                        .build(),
                FunctionToolCallback.builder("runMaven", tools::runMaven)
                        .description("在指定项目目录执行 Maven 命令（如 clean、compile、package、install、version等）。参数：command（Maven命令，如 clean compile），projectPath（项目根目录路径）")
                        .inputType(DevTools.MavenRequest.class)
                        .build(),
                FunctionToolCallback.builder("convertEncoding", tools::convertEncoding)
                        .description("将指定目录下所有 Java 源文件从原始编码转换为 UTF-8，解决乱码问题。参数：directory（目录路径），fromEncoding（原始编码，如 GBK、ISO-8859-1）")
                        .inputType(DevTools.EncodingRequest.class)
                        .build()
        );
    }

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
                .restClientBuilder(restClientBuilder)
                .build();
    }

    @Bean
    public ChatModel chatModel(OpenAiApi api, List<ToolCallback> toolCallbacks) {
        var toolCallingManager = DefaultToolCallingManager.builder()
                .toolCallbackResolver(new StaticToolCallbackResolver(toolCallbacks))
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .toolCallingManager(toolCallingManager)
                .build();
    }
}
