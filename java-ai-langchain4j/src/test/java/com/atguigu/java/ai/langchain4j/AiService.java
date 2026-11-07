package com.atguigu.java.ai.langchain4j;

import com.atguigu.java.ai.langchain4j.Assistant.Assistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AiService {

    @Autowired
    private Assistant assistant;

    @Test
    public void testAssistant() {
        String answer = assistant.chat("你是什么模型");
        System.out.println(answer);
    }
}
