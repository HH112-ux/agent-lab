package com.caspian.agent.chatrag;

import com.caspian.agent.chatrag.service.AssistantService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
public class RagTest {


    @Resource
    QwenChatModel qwenChatModel;

    @Test
    void easyRag() throws URISyntaxException {
        //加载file文件夹下所有文件
        URL resourceUrl = getClass().getResource("/file");
        Path resourcePath = Paths.get(resourceUrl.toURI());
        //加载文档
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(resourcePath);

        //将文档转换为向量并存储在内存中
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        /**
         *  这里做了两件事
         *  1.每个 Document 被分割成更小的片段（TextSegment），每个片段不超过 300 个token， 并有 30 个token的重叠（Chunk Overlap）
         *  2.通过 SPI 从 langchain4j-easy-rag 依赖加载 EmbeddingModel。 每个 TextSegment 使用 EmbeddingModel 转换为 Embedding（没有用到大模型去向量化）
         */
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

        //通过SPI创建一个EmbeddingStoreContentRetriever实例
        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.from(embeddingStore);

        //直接提问
        AssistantService service = AiServices.builder(AssistantService.class)
                .chatModel(qwenChatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(retriever)
                .build();
        String chat = service.chat("哪里是江南最后的秘境");
        System.out.println(chat);
    }


    @Resource
    QwenEmbeddingModel qwenEmbeddingModel;

    @Test
    void naiveRag() throws URISyntaxException {
        //获取文件路径
        URL dirUrl = getClass().getResource("/file");
        Path dirPath = Paths.get(dirUrl.toURI());
        Path resourcePath = dirPath.resolve("演示专用文档.txt");
        //加载文档->转换成document对象
        Document document = FileSystemDocumentLoader.loadDocument(resourcePath);
        //切分成小块,两个参数 最大分段长度，分段之间的最大重叠长度
        DocumentSplitter recursive = DocumentSplitters.recursive(300, 0);
        //得到了每个小块
        List<TextSegment> segments = recursive.split(document);
        //通过大模型 向量化
        List<Embedding> contents = qwenEmbeddingModel.embedAll(segments).content();
        //存入向量数据库
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(contents, segments);
        //创建检索对象
        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(2)//检索时会检索出相关性最高的两个文本
                .minScore(0.5)//相似度阈值，0-1之间，0表示相似度由低到高
                .build();
        //提問
        AssistantService service = AiServices.builder(AssistantService.class)
                .chatModel(qwenChatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(retriever)
                .build();
        String chat = service.chat("哪里是江南最后的秘境");
        System.out.println(chat);

    }
}
