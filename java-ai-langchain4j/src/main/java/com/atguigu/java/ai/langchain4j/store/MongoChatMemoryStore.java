package com.atguigu.java.ai.langchain4j.store;

import com.atguigu.java.ai.langchain4j.bean.ChatMessages;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * MongoDB聊天记忆存储实现类
 * 
 * 该类实现了LangChain4j的ChatMemoryStore接口，使用MongoDB作为持久化存储介质，
 * 用于保存和检索对话历史消息。支持消息的查询、更新和删除操作。
 */
@Component
public class MongoChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 获取指定会话的聊天消息列表
     * 
     * 根据会话ID从MongoDB中查询对应的聊天消息记录，如果不存在则返回空列表。
     * 查询到的JSON格式消息会被反序列化为ChatMessage对象列表。
     * 
     * @param memoryId 会话记忆ID，用于标识特定的对话会话
     * @return List<ChatMessage> 聊天消息列表，如果该会话不存在则返回空列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        //ChatMessages是用来存储从MongoDB查询到的聊天消息的，而ChatMessage才是需要返回的形式
        ChatMessages chatMessages = mongoTemplate.findOne(query, ChatMessages.class);
        if(chatMessages == null) return new LinkedList<>();
        return ChatMessageDeserializer.messagesFromJson(chatMessages.getContent());
    }

    /**
     * 更新或保存指定会话的聊天消息列表
     * 
     * 将聊天消息列表序列化为JSON格式并保存到MongoDB中。使用upsert操作，
     * 如果存在对应会话ID的记录则更新，否则创建新记录。
     * 
     * @param memoryId 会话记忆ID，用于标识特定的对话会话
     * @param messages 需要保存的聊天消息列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", ChatMessageSerializer.messagesToJson(messages));
        //根据query条件能查询出文档，则修改文档；否则新增文档
        mongoTemplate.upsert(query, update, ChatMessages.class);
    }

    /**
     * 删除指定会话的聊天消息记录
     * 
     * 根据会话ID从MongoDB中删除对应的聊天消息记录。
     * 
     * @param memoryId 会话记忆ID，用于标识需要删除的对话会话
     */
    @Override
    public void deleteMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, ChatMessages.class);
    }
}
