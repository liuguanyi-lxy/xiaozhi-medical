package com.atguigu.java.ai.langchain4j.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * RAG 知识库文件入库记录
 *
 * 以文件名为主键，记录该文件最后一次向量化入库时的内容指纹（SHA-256）与
 * 全部向量 id，用于启动时判断知识文档是否需要（重新）入库。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("rag_knowledge_files")
public class KnowledgeFileRecord {
    /** 知识文件名，作为记录主键 */
    @Id
    private String fileName;
    /** 文件内容 SHA-256 指纹 */
    private String contentHash;
    /** 该文件本次入库产生的全部向量 id，用于文档变更时删除旧向量 */
    private List<String> vectorIds;
    /** 最近一次入库时间 */
    private Date ingestedAt;
}
