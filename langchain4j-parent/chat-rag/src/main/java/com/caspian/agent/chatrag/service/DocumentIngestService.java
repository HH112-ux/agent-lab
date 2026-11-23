package com.caspian.agent.chatrag.service;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DocumentIngestService {

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final QwenEmbeddingModel embeddingModel;

    public void ingestDocuments() {
        try {
            TextDocumentParser parser = new TextDocumentParser();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:file/*");

            for (Resource resource : resources) {
                Document document = parser.parse(resource.getInputStream());
                EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                        .documentSplitter(DocumentSplitters.recursive(1000, 200))
                        .embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore)
                        .build();

                ingestor.ingest(document);
                System.out.println("文档已入库：" + resource.getFile().getName());
            }
        } catch (Exception e) {
            throw new RuntimeException("文档加载失败", e);
        }
    }
}
