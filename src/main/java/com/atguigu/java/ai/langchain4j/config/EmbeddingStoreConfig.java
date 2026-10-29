package com.atguigu.java.ai.langchain4j.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 嵌入存储配置类
 * 用于配置和创建基于 Pinecone 的向量嵌入存储，支持文本片段的存储和检索
 *
 * 说明：API Key 等敏感信息一律通过配置项注入，不硬编码在源码中，
 * 配置值从环境变量 / Nacos 配置中心读取，源码仓库中仅保留占位符。
 */
@Configuration
public class EmbeddingStoreConfig {

    @Autowired
    private EmbeddingModel embeddingModel;

    /** Pinecone API Key，通过环境变量 PINECONE_API_KEY 注入 */
    @Value("${pinecone.api-key}")
    private String pineconeApiKey;

    /** Pinecone 索引名称 */
    @Value("${pinecone.index:xiaozhiyiliao}")
    private String pineconeIndex;

    /** Pinecone 命名空间 */
    @Value("${pinecone.namespace:xiaozhiyiliao}")
    private String pineconeNamespace;

    /** Pinecone 部署区域 */
    @Value("${pinecone.region:us-east-1}")
    private String pineconeRegion;

    /**
     * 创建并配置 Pinecone 嵌入存储 Bean
     * 该存储用于保存和检索文本片段的向量表示，支持语义搜索功能
     *
     * @return 配置好的 EmbeddingStore 实例，用于管理 TextSegment 的向量存储
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        // 创建 Pinecone 嵌入存储实例，配置连接参数和索引设置
        EmbeddingStore<TextSegment> embeddingStore = PineconeEmbeddingStore.builder()
                .apiKey(pineconeApiKey)
                .index(pineconeIndex)
                .nameSpace(pineconeNamespace)
                .createIndex(
                        PineconeServerlessIndexConfig.builder()
                        .cloud("AWS")
                        .region(pineconeRegion)
                        .dimension(embeddingModel.dimension())
                        .build())
                .build();
        return embeddingStore;
    }
}
