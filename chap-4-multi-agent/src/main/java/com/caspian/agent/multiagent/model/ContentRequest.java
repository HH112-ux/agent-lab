package com.caspian.agent.multiagent.model;

import java.util.List;

public class ContentRequest {

    // 文章主题
    private String topic;

    // 关键词列表
    private List<String> keywords;

    // 风格要求
    private String style;

    public ContentRequest() {

    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}
