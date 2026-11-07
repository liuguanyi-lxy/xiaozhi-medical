package com.atguigu.java.ai.langchain4j.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.param.MetricType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 嵌入存储配置类
 *
 * 向量库从云端的 Pinecone 换成部署在本地虚拟机上的 Milvus：
 *   · 数据留在自己机器上，不出网
 *   · 不需要第三方 API Key，也不受额度与网络波动影响
 *   · 检索走内网，延迟更稳
 *
 * 连接参数全部来自配置项，不硬编码在源码中。
 */
@Configuration
public class EmbeddingStoreConfig {

    @Autowired
    private EmbeddingModel embeddingModel;

    /** Milvus 主机（本地虚拟机） */
    @Value("${milvus.host:192.168.13.100}")
    private String milvusHost;

    /** Milvus gRPC 端口 */
    @Value("${milvus.port:19530}")
    private Integer milvusPort;

    /** 存放知识库向量的集合名 */
    @Value("${milvus.collection:xiaozhi_knowledge}")
    private String collectionName;

    /** Milvus 用户名，未开启鉴权时留空 */
    @Value("${milvus.username:}")
    private String milvusUsername;

    /** Milvus 密码，未开启鉴权时留空 */
    @Value("${milvus.password:}")
    private String milvusPassword;

    /**
     * 创建并配置 Milvus 嵌入存储 Bean
     *
     * 维度取自当前注入的 EmbeddingModel（DashScope text-embedding-v2 为 1536），
     * 换嵌入模型时不需要手工改维度，避免维度不匹配导致检索失败。
     *
     * @return 用于存取文本片段向量的 EmbeddingStore 实例
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        MilvusEmbeddingStore.Builder builder = MilvusEmbeddingStore.builder()
                .host(milvusHost)
                .port(milvusPort)
                .collectionName(collectionName)
                // 维度跟随嵌入模型，避免手写 1536 与实际模型不一致
                .dimension(embeddingModel.dimension())
                .metricType(MetricType.COSINE)
                // 写入后立即 flush，保证刚灌完库就能检索到
                .autoFlushOnInsert(true);

        // 只有配置了用户名才走鉴权，避免空用户名被服务端拒绝
        if (milvusUsername != null && !milvusUsername.isBlank()) {
            builder.username(milvusUsername).password(milvusPassword);
        }

        return builder.build();
    }
}
