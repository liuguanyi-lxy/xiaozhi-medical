package com.atguigu.java.ai.langchain4j.bean;

import lombok.Data;

/**
 * 聊天表单数据传输对象
 * 
 * 用于封装用户与智能助手对话时的请求数据，包含会话ID和用户消息内容。
 */
@Data
public class ChatForm {
    /**
     * 对话记忆ID，用于标识和区分不同的会话上下文
     */
    private Long memoryId;

    /**
     * 用户发送的消息内容或问题文本
     */
    private String message;
}
