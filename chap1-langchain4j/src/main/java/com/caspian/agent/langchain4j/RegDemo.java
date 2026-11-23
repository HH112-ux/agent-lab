package com.caspian.agent.langchain4j;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class RegDemo {

    static final String CHAT_API_KEY, CHAT_BASE_URL, CHAT_MODEL_NAME;
    static final String EMBED_API_KEY, EMBED_BASE_URL, EMBED_MODEL_NAME;

    static {
        try (InputStream is = RegDemo.class.getResourceAsStream("/application.properties")){
            Properties p = new Properties();
            p.load(is);
            CHAT_API_KEY = p.getProperty("chat.api.key");
            CHAT_BASE_URL = p.getProperty("chat.base.url");
            CHAT_MODEL_NAME = p.getProperty("chat.model");
            EMBED_API_KEY = p.getProperty("embedding.api.key");
            EMBED_BASE_URL = p.getProperty("embedding.base.url");
            EMBED_MODEL_NAME = p.getProperty("embedding.model");
        } catch (Exception e) {
            throw new RuntimeException("加载配置文件失败", e);
        }
    }

    static ChatModel chatModel = OpenAiChatModel.builder()
            .apiKey(CHAT_API_KEY).baseUrl(CHAT_BASE_URL).modelName(CHAT_MODEL_NAME).build();

    static EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
            .apiKey(EMBED_API_KEY).baseUrl(EMBED_BASE_URL).modelName(EMBED_MODEL_NAME).build();

    static List<String> notes = List.of(
            "LangChain4j 是一个 Java 版的 LLM 应用框架，提供了模型对接、RAG、工具调用等核心能力。",
            "RAG（检索增强生成）通过先检索相关文档片段，再交给大模型生成回答，减少幻觉、提升准确性。",
            "Embedding（向量嵌入）将文本转换为高维向量，使得语义相似的文本在向量空间中距离更近。",
            "AiServices 是 LangChain4j 的高级抽象，通过定义 Java 接口即可自动组装 RAG 流水线。",
            "InMemoryEmbeddingStore 是 LangChain4j 内置的内存向量存储，适合原型开发和功能验证。"
    );

    static EmbeddingStore<TextSegment> buildStore() {
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        for (String note : notes) {
            TextSegment segment = TextSegment.from(note);
            Embedding embedding = embeddingModel.embed(segment).content();
            store.add(embedding, segment);
        }
        return store;
    }

    interface Assistant {String answer(String question); }

    public static void main(String [] args) {
        System.out.println("Chat: " + CHAT_BASE_URL + " / " + CHAT_MODEL_NAME);
        System.out.println("Embed: " + EMBED_BASE_URL + " / " + EMBED_MODEL_NAME);

        EmbeddingStore<TextSegment> store = buildStore();
        System.out.println("笔记已向量化并存入内存向量库");

        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store).embeddingModel(embeddingModel).maxResults(2).build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel).contentRetriever(retriever).build();

        List<String> questions = List.of(
                "什么是 RAG？它有什么好处？",
                "Embedding 是做什么的？",
                "LangChain4j 的 InMemoryEmbeddingStore 适合什么场景？"
        );

        for (String q : questions) {
            System.out.println("\nQ: " + q);
            System.out.println("A: " + assistant.answer(q));
            System.out.println("---");
        }
    }
}
