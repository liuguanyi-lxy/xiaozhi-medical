package com.atguigu.java.ai.langchain4j.store;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * RAG 知识库幂等入库组件
 *
 * 原实现每次应用启动都把全部知识文档重新向量化写入 Pinecone，重启一次就多一份重复向量，
 * 既浪费存储也会让检索命中过期内容。该组件按"内容指纹 + 向量 id 登记"解决：
 * <ol>
 *   <li>入库前计算文档内容的 SHA-256 指纹，与 MongoDB 登记的指纹一致则跳过，重启不会重复入库；</li>
 *   <li>文档内容变化时，先按登记的向量 id 删除旧向量，再全量写入新向量，避免新旧版本并存；</li>
 *   <li>切分、向量化、写入时显式生成向量 id 并登记，保证删除时可精确定位该文件的所有向量。</li>
 * </ol>
 */
@Component
public class KnowledgeBaseIngestor {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseIngestor.class);

    /** 与 EmbeddingStoreIngestor 默认策略一致的递归切分器：每段约 300 token，重叠 30 token */
    private static final DocumentSplitter DOCUMENT_SPLITTER = DocumentSplitters.recursive(300, 30);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 幂等入库：内容未变化的文档跳过，变化的文档先删旧向量再写入新向量
     *
     * @param documents      待入库的知识文档，需携带 file_name 元数据
     * @param embeddingStore 向量存储（Pinecone）
     * @param embeddingModel 向量模型
     */
    public void ingestIfChanged(List<Document> documents,
                                EmbeddingStore<TextSegment> embeddingStore,
                                EmbeddingModel embeddingModel) {
        for (Document document : documents) {
            String fileName = document.metadata().getString("file_name");
            String contentHash = sha256Hex(document.text());
            KnowledgeFileRecord record = mongoTemplate.findById(fileName, KnowledgeFileRecord.class);

            if (record != null && contentHash.equals(record.getContentHash())) {
                log.info("RAG 知识库：[{}] 内容未变化，跳过入库", fileName);
                continue;
            }
            if (record != null && record.getVectorIds() != null && !record.getVectorIds().isEmpty()) {
                // 文档内容已变化：先按登记的向量 id 删除旧版本，避免新旧并存导致检索命中过期内容
                embeddingStore.removeAll(record.getVectorIds());
                log.info("RAG 知识库：[{}] 内容已变化，删除旧向量 {} 条", fileName, record.getVectorIds().size());
            }

            List<String> vectorIds = embedAndStore(document, embeddingStore, embeddingModel);
            mongoTemplate.save(new KnowledgeFileRecord(fileName, contentHash, vectorIds, new Date()));
            log.info("RAG 知识库：[{}] 入库完成，共 {} 个向量片段", fileName, vectorIds.size());
        }
    }

    /**
     * 切分 -> 向量化 -> 携带显式 id 写入向量库，并返回全部向量 id
     */
    private List<String> embedAndStore(Document document,
                                       EmbeddingStore<TextSegment> embeddingStore,
                                       EmbeddingModel embeddingModel) {
        List<TextSegment> segments = DOCUMENT_SPLITTER.split(document);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        List<String> vectorIds = new ArrayList<>(segments.size());
        for (int i = 0; i < segments.size(); i++) {
            vectorIds.add(UUID.randomUUID().toString());
        }
        embeddingStore.addAll(vectorIds, embeddings, segments);
        return vectorIds;
    }

    private String sha256Hex(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // JDK 必带 SHA-256，理论上不会走到这里
            throw new IllegalStateException("SHA-256 摘要算法不可用", e);
        }
    }
}
