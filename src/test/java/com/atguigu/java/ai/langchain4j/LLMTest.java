package com.atguigu.java.ai.langchain4j;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LLMTest {
    /**
     * 整合SpringBoot，自动给我创建一个模型对象，并自动注入到Spring容器中
     */
    @Autowired
    private OpenAiChatModel openAiChatModel;
    @Qualifier("openAiChatModel")
    @Autowired
    private ChatModel chatModel;
    /**
     * 通义千问大模型
     */
    @Autowired
    private QwenChatModel qwenChatModel;
    @Test
    public void testDashScopeWanx(){
        WanxImageModel wanxImageModel = WanxImageModel.builder()
                .modelName("wanx2.1-t2i-plus")
                // 密钥从环境变量读取，禁止硬编码（IDEA 运行配置里设置 DASHSCOPE_API_KEY）
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .build();
        Response<Image> response = wanxImageModel.generate("蛊真人方源");
        System.out.println(response.content().url());
    }
    @Test
    public void testDashScopeQwen() {
        //向模型提问
        String answer = qwenChatModel.chat("你是什么模型");
        //输出结果
        System.out.println(answer);
    }
    @Test
    public void testDeepseek() {
        //向模型提问
        //String answer = openAiChatModel.chat("你好");
        String answer = chatModel.chat("你是什么模型");
        //输出结果
        System.out.println(answer);
    }
    @Test
    public void testSpringBoot() {
        //向模型提问
        String answer = openAiChatModel.chat("你是什么模型");
        //输出结果
        System.out.println(answer);
    }
//    /**
//     * gpt-4o-mini语言模型接入测试
//     */
//    @Test
//    public void testGPTDemo() {
//        //初始化模型
//        OpenAiChatModel model = OpenAiChatModel.builder()
//                //LangChain4j提供的代理服务器，该代理服务器会将演示密钥替换成真实密钥， 再将请求转发给OpenAI API
//                .baseUrl("http://langchain4j.dev/demo/openai/v1")
//                .apiKey("demo") //设置模型apiKey
//                .modelName("gpt-4o-mini") //设置模型名称
//                .build();
//
//        //向模型提问
//        String answer = model.chat("你好");
//        //输出结果
//        System.out.println(answer);
//    }
}
