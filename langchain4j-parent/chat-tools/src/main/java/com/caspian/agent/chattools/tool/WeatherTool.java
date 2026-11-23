package com.caspian.agent.chattools.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool {

    @Tool("返回给定城市的天气预报")
    String getWeather(@P("应返回天气预报的城市") String city) {
        return city + "天气晴";
    }

}
