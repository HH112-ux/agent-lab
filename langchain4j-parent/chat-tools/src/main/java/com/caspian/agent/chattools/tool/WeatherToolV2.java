package com.caspian.agent.chattools.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

@Component
public class WeatherToolV2 {

    @Tool("返回给定城市的天气预报")
    String getWeatherWithMemoryId(@P("需要查询天气的城市名称") String city,
                                  @ToolMemoryId Long memoryId) {
        System.out.println("拿到记忆ID：" + memoryId);
        return city + " 天气晴，记忆ID：" + memoryId;
    }
}
