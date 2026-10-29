package com.atguigu.java.ai.langchain4j.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB配置类
 * 
 * 配置MongoDB连接参数，包括连接超时和socket超时，避免应用启动时因连接问题而长时间等待。
 */
@Configuration
public class MongoConfig {

    /**
     * MongoDB连接URI
     */
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    /**
     * 创建MongoTemplate Bean
     * 
     * 配置带有超时设置的MongoDB客户端，防止在网络不可用时启动过慢。
     * 
     * @return MongoTemplate MongoDB模板对象，用于数据库操作
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        /**
         * 构建MongoDB客户端设置，配置连接和socket超时时间
         */
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(mongoUri))
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(3000, TimeUnit.MILLISECONDS)
                           .readTimeout(3000, TimeUnit.MILLISECONDS))
                .build();

        /**
         * 创建MongoDB客户端和数据库工厂
         */
        MongoClient mongoClient = MongoClients.create(settings);
        SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(mongoClient, "chat_memory_db");

        /**
         * 返回配置好的MongoTemplate
         */
        return new MongoTemplate(factory);
    }
}
