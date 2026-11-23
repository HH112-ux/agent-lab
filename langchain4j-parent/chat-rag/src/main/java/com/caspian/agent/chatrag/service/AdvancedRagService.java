package com.caspian.agent.chatrag.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 高级 RAG 对话服务
 * 功能：查询转换 + 向量检索 + 元数据过滤 + 动态阈值 + 重排序 + 上下文记忆
 */
@Service
@RequiredArgsConstructor
public class AdvancedRagService {

    // 通义千问大语言模型（生成回答）
    private final ChatModel chatModel;

    // 向量嵌入模型（把问题转成向量）
    private final EmbeddingModel embeddingModel;

    // 向量存储（存储文档向量 + 相似度检索）
    private final EmbeddingStore<TextSegment> embeddingStore;

    // 查询转换器（查询扩展/重写/多轮优化）
    private final QueryTransformer queryTransformer;

    // 对话记忆（保留多轮聊天上下文）
    private final ChatMemory chatMemory;

    //重新排序模型
    private final ScoringModel scoringModel;

    /**
     * 高级 RAG 问答入口
     * 完整流程：用户问题 → 查询优化 → 向量检索 → 重排 → 大模型生成回答
     *
     * @param question 用户问题
     * @return 带知识库上下文的回答
     */
    public String advancedChat(String question) {
        // ===================== 1. 构建 高级向量检索器 =====================
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)          // 向量库
                .embeddingModel(embeddingModel)          // 嵌入模型
                .maxResults(5)                           // 最多返回5条结果
                .dynamicMaxResults(query -> 7)     // 可根据问题复杂度动态调整条数
                .minScore(0.5)                           // 基础相似度阈值
                .dynamicMinScore(query -> 0.5)    // 动态相似度（复杂问题提高精度）
                //.filter(metadataKey("userId").isEqualTo("12345")) // 元数据过滤（如按用户ID隔离数据）
                .build();

        // ===================== 2. 检索结果重排序（提高精度） =====================
        // 使用模型对检索到的多条结果重新排序，提升最终答案准确性
        ReRankingContentAggregator reRankingAggregator = ReRankingContentAggregator.builder()
                .scoringModel(scoringModel)
                .build();

        // ===================== 3. 内容注入器（你要的关键组件！） =====================
        // 作用：把检索到的文档 自动拼接到 Prompt 里
        ContentInjector contentInjector = DefaultContentInjector.builder()
                .build();

        // ===================== 4. 构建 RAG 增强器（整合检索+重排） =====================
        DefaultRetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)      // 查询优化（重写/扩展/分解）
                .contentRetriever(contentRetriever)      // 检索器
                .contentAggregator(reRankingAggregator)  // 重排器
                .contentInjector(contentInjector)        // 注入上下文
                .build();

        // ===================== 5. 构建带记忆 + RAG 的 AI 服务 =====================
        AssistantService assistantService = AiServices.builder(AssistantService.class)
                .chatModel(chatModel)                    // 大模型
                .chatMemory(chatMemory)                  // 多轮记忆
                .retrievalAugmentor(retrievalAugmentor)  // RAG增强
                .build();

        // ===================== 6. 执行问答并返回 =====================
        return assistantService.chat(question);
    }
}