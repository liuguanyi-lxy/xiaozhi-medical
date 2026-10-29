package com.atguigu.java.ai.langchain4j.config;

import com.atguigu.java.ai.langchain4j.store.KnowledgeBaseIngestor;
import com.atguigu.java.ai.langchain4j.store.MongoChatMemoryStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 小智智能助手配置类
 *
 * 该配置类用于配置小智助手的聊天记忆提供者，使用MongoDB持久化存储对话历史，
 * 支持基于窗口的消息管理策略。
 */
@Configuration
public class XiaozhiAgentConfig {
    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    /**
     * 嵌入模型，用于将文本转换为向量
     */
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * RAG 知识库幂等入库组件
     */
    @Autowired
    private KnowledgeBaseIngestor knowledgeBaseIngestor;

    /**
     * 知识库文档位置，默认指向 classpath 下的 knowledge 目录，
     * 更换知识库时只需替换 resources/knowledge 下的文档或修改该配置，无需改代码
     */
    @Value("${rag.knowledge-location:classpath:knowledge/*.md}")
    private String knowledgeLocation;

    /**
     * 创建聊天记忆提供者Bean
     * <p>
     * 配置一个基于消息窗口的聊天记忆提供者，该提供者会为每个会话ID创建独立的
     * 聊天记忆实例，并限制最大消息数量以防止内存溢出。聊天记忆通过MongoDB进行
     * 持久化存储，确保对话历史在应用重启后仍然可用。
     *
     * @return ChatMemoryProvider 聊天记忆提供者实例，用于为不同会话提供记忆管理服务
     */
    @Bean
    ChatMemoryProvider chatMemoryProviderXiaozhi() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(mongoChatMemoryStore)
                .build();
    }

    /**
     * 创建内容检索器 Bean
     * 该检索器用于从知识库文档中检索与用户查询相关的内容，支持基于向量相似度的语义搜索
     * 流程：按配置位置加载知识库文档 -> 幂等入库（内容未变化跳过、变化先删旧向量）-> 配置检索策略
     *
     * @return ContentRetriever 内容检索器实例，用于从向量数据库中检索相关知识
     */
    @Bean
    public ContentRetriever contentRetrieverXiaozhiPinecone() {
        List<Document> documents = loadKnowledgeDocuments();

        // 知识库幂等入库：重启不重复灌入，文档变更自动替换旧向量
        knowledgeBaseIngestor.ingestIfChanged(documents, embeddingStore, embeddingModel);

        // 配置并返回嵌入存储内容检索器
        return EmbeddingStoreContentRetriever
                .builder()
                .embeddingModel(embeddingModel)// 设置用于生成嵌入向量的嵌入模型
                .embeddingStore(embeddingStore)// 指定要使用的嵌入存储
                .maxResults(1)// 设置最大检索结果数量，这里表示最多返回 1 条匹配结果
                .minScore(0.8)// 设置最小得分阈值，只有得分大于等于 0.8 的结果才会被返回
                .build();// 构建最终的 EmbeddingStoreContentRetriever 实例
    }

    /**
     * 从 classpath 加载知识库文档，并写入 file_name 元数据供入库登记与溯源使用
     */
    private List<Document> loadKnowledgeDocuments() {
        TextDocumentParser parser = new TextDocumentParser();
        List<Document> documents = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(knowledgeLocation);
            for (Resource resource : resources) {
                try (InputStream inputStream = resource.getInputStream()) {
                    Document document = parser.parse(inputStream);
                    document.metadata().put("file_name", resource.getFilename());
                    documents.add(document);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("加载 RAG 知识库文档失败：" + knowledgeLocation, e);
        }
        return documents;
    }
}
