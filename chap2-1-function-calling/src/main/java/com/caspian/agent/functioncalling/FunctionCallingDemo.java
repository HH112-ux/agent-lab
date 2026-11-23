package com.caspian.agent.functioncalling;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Properties;

public class FunctionCallingDemo {

    static String getWeather(String city) {
        Map<String, String> data = Map.of(
                "北京", "晴天，25°C，湿度45%，空气质量优",
                "上海", "多云，22°C，湿度65%，空气质量良",
                "深圳", "小雨，28°C，湿度80%，空气质量优"
        );
        return data.getOrDefault(city, "暂无该城市的天气数据");
    }

    static String getPopulation(String city) {
        Map<String, String> data = Map.of(
                "北京", "2189万",
                "上海", "2487万",
                "深圳", "1768万"
        );
        return data.getOrDefault(city, "暂无该城市的人口数据");
    }

    static String callLLM(String url, String key, String body) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "appliction/json")
                .header("Authorization", "Bearer " + key)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    static String val(String json, String key) {
        int i = json.indexOf("\"" + key + "\"");
        if (i <0) {
            return null;
        }
        i = json.indexOf("\"", i + key.length() + 3);
        if (i < 0) {
            return null;
        }
        var sb = new StringBuilder();
        while (++i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\') {
                sb.append(json.charAt(++i));
                continue;
            }
            if (c == '"') {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static void main(String [] args) throws IOException, InterruptedException {
        //加载配置
        var props = new Properties();
        props.load(FunctionCallingDemo.class.getClassLoader().getResourceAsStream("config.properties"));
        String url = props.getProperty("api.url");
        String key = props.getProperty("api.key");
        String model = props.getProperty("api.model");
        String question = "上海有多少人口?";
        System.out.println("====== Function Calling 底层原理演示========\n");
        System.out.println("用户: " + question);

        String toolDefWeather = """
                                {"type":"function","function":{"name":"get_weather","description":"获取指定城市的天气","parameters":{"type":"object","properties":{"city":{"type":"string","description":"城市名称"}},"required":["city"]}}}""";
        String toolDefPopulation = """
                                {"type":"function","function":{"name":"get_population","description":"获取指定城市的人口数量","parameters":{"type":"object","properties":{"city":{"type":"string","description":"城市名称"}},"required":["city"]}}}""";
        String body1 = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"tools\":[%s,%s]}", model, question, toolDefWeather, toolDefPopulation
                );
        System.out.println("\n[1] 向LLM发送请求 (携带2个tools定义)...");
        System.out.println("    - get_weather: 获取城市天气");
        System.out.println("    - get_population: 获取城市人口");
        String resp1 = callLLM(url, key, body1);
        System.out.println(" LLM原始响应：" + resp1);

        if (!resp1.contains("tool_calls")) {
            System.out.println("LLM未触发函数调用，直接回答了问题");
            return;
        }
        // 从 tool_calls里提取函数调用信息
        int tc = resp1.indexOf("tool_calls");
        String sub = resp1.substring(tc);
        String callId = val(sub, "id");
        String funcName = val(sub, "name");
        String arguments = val(sub, "arguments");
        String city = val(arguments, "city");
        System.out.println(" LLM决定调用:" + funcName + "(city=" + city + ")");

        // 根据LLM选择的函数，路由到对应的本地函数
        String result = switch (funcName) {
            case "get_weather" -> getWeather(city);
            case "get_population" -> getPopulation(city);
            default -> "未知函数:" + funcName;
        };
        System.out.println("\n 本地执行函数：" + funcName + "(\"" + city + "\")");
        System.out.println(" 返回结果: " + result);

        // 将函数结果，回传给LLM， 最终生成自然语言回答
        String escapedArgs = arguments.replace("\"", "\\\"");
        String body2 = String.format("{\"model\":\"%s\",\"messages\":["
                + "{\"role\":\"user\",\"content\":\"%s\"},"
                + "{\"role\":\"assistant\",\"content\":null,\"tool_calls\":[{\"id\":\"%s\",\"type\":\"function\",\"function\":{\"name\":\"%s\",\"arguments\":\"%s\"}}]},"
                + "{\"role\":\"tool\",\"tool_call_id\":\"%s\",\"content\":\"%s\"}"
                + "]}",
                model, question, callId, funcName, escapedArgs, callId, result);
        System.out.println("\n 将函数结果回传给LLM...");
        String resp2 = callLLM(url, key, body2);
        String answer = val(resp2, "content");
        System.out.println("\n =====最终徊答=======");
        System.out.println(answer);
    }
}
