package com.atguigu.java.ai.langchain4j.Assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 小智智能助手服务接口
 * 
 * 该接口使用LangChain4j的@AiService注解定义了一个AI代理服务，配置了显式布线模式、
 * 通义千问max模型以及专用的聊天记忆提供者，用于处理用户的对话请求。
 */
@AiService(
        wiringMode = EXPLICIT,
        streamingChatModel = "qwenStreamingChatModel",
        chatMemoryProvider = "chatMemoryProviderXiaozhi",
        tools = "appointmentTools",//tools配置,给小智添加工具方法，在一定的场景下调用
        contentRetriever = "contentRetrieverXiaozhiPinecone"//【修复】原拼写 Pincone 与 Bean 名不一致，
        //EXPLICIT 模式按 Bean 名解析（无类型回退），启动会抛 NoSuchBeanDefinitionException
)
public interface XiaozhiAgent {
    /**
     * 与智能助手进行对话
     * 
     * 该方法基于用户消息和会话ID进行智能对话，使用系统消息模板（xiaozhi-prompt-template.txt）
     * 来定义助手的行为和角色。支持多会话记忆管理，通过memoryId区分不同用户的对话上下文。
     * 
     * @param memoryId 会话记忆ID，用于标识和隔离不同用户的对话上下文
     * @param userMessage 用户发送的消息内容
     * @return AI助手生成的回复消息
     */
    @SystemMessage(fromResource = "xiaozhi-prompt-template.txt")
    Flux<String> chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}
