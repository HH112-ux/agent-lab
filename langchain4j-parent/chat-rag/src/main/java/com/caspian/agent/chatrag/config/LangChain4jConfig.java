package com.caspian.agent.chatrag.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.jina.JinaScoringModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    //向量存储
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(System.getenv("CHROMA_BASE_URL"))
                .collectionName("my_collection")
                .apiVersion(ChromaApiVersion.V2)//新版要用v2
                .tenantName("default_tenant")
                .databaseName("default_database")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    //对话内存（保留最近10轮对话）
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }

    //高级查询压缩器（基于对话历史优化查询）
    @Bean
    public CompressingQueryTransformer queryTransformer(ChatModel chatModel) {
        return new CompressingQueryTransformer(chatModel);
    }

    //Rerank重排序的模型
    @Bean
    public ScoringModel scoringModel() {
      return JinaScoringModel.builder()
                .apiKey(System.getenv("JINA_API_KEY"))
                .modelName("jina-reranker-v2-base-multilingual")
                .build();
    }
}
