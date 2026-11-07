package com.atguigu.java.ai.langchain4j.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RAG 知识库幂等入库组件
 *
 * 幂等策略保持不变，只把"入库登记"从 MongoDB 换成进程外的本地 JSON 文件：
 * <ol>
 *   <li>入库前计算文档内容的 SHA-256 指纹，与登记一致则跳过，重启不会重复入库；</li>
 *   <li>文档内容变化时，先按登记的向量 id 删除旧向量，再全量写入新向量，避免新旧并存；</li>
 *   <li>登记文件位置：${user.home}/.xiaozhi/knowledge-index.json，重启后依然有效。</li>
 * </ol>
 *
 * 改用文件登记的原因：入库登记只需要跨重启保存指纹与向量 id，放本地 JSON 文件
 * 即可满足，不必为此依赖数据库（会话记忆仍持久化在 MongoDB，两者互不影响）。
 */
@Component
public class KnowledgeBaseIngestor {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseIngestor.class);

    /** 与 EmbeddingStoreIngestor 默认策略一致的递归切分器：每段约 300 token，重叠 30 token */
    private static final DocumentSplitter DOCUMENT_SPLITTER = DocumentSplitters.recursive(300, 30);

    /** 入库登记文件：放在用户目录下，重启后仍可读取 */
    private static final Path INDEX_FILE =
            Paths.get(System.getProperty("user.home"), ".xiaozhi", "knowledge-index.json");

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 文件名 -> 入库记录 */
    private Map<String, KnowledgeFileRecord> index = new HashMap<>();

    @PostConstruct
    void loadIndex() {
        try {
            if (Files.exists(INDEX_FILE)) {
                index = objectMapper.readValue(
                        INDEX_FILE.toFile(),
                        new TypeReference<Map<String, KnowledgeFileRecord>>() {});
                log.info("RAG 知识库：已加载入库登记 {} 条（{}）", index.size(), INDEX_FILE);
            }
        } catch (Exception e) {
            // 登记文件损坏不影响启动，按首次入库重新灌一遍即可
            log.warn("RAG 知识库：入库登记读取失败，将按首次入库处理", e);
            index = new HashMap<>();
        }
    }

    private void persistIndex() {
        try {
            Files.createDirectories(INDEX_FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(INDEX_FILE.toFile(), index);
        } catch (Exception e) {
            log.warn("RAG 知识库：入库登记写入失败（不影响本次运行）", e);
        }
    }

    /**
     * 幂等入库：内容未变化的文档跳过，变化的文档先删旧向量再写入新向量
     *
     * @param documents      待入库的知识文档，需携带 file_name 元数据
     * @param embeddingStore 向量存储（Milvus）
     * @param embeddingModel 向量模型
     */
    public void ingestIfChanged(List<Document> documents,
                                EmbeddingStore<TextSegment> embeddingStore,
                                EmbeddingModel embeddingModel) {
        for (Document document : documents) {
            String fileName = document.metadata().getString("file_name");
            String contentHash = sha256Hex(document.text());
            KnowledgeFileRecord record = index.get(fileName);

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
            index.put(fileName, new KnowledgeFileRecord(fileName, contentHash, vectorIds, new Date()));
            persistIndex();
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
