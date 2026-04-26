package com.iisigroup.df.labs;

import com.iisigroup.df.labs.base.MySpringBootTest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@MySpringBootTest
public class ChatSimpleTest {

    @Autowired
    private ChatClient.Builder builder;

    private ChatClient client;

    @PostConstruct
    public void init() {
        this.client = builder
                .build();
    }

    @Test
    public void syncChatFluent() {
        val userPrompt = "你好我叫小明 , 你能幫我介紹一下自己嗎?";
        log.info("request userPrompt: {}", userPrompt);
        val content = client.prompt()
                .system("你是我的好朋友 , 叫小厚 , 是一名軟體工程師")
                .user(userPrompt)
                .call()
                .content();
        log.info("response content: {}", content);
    }

    @Test
    public void asyncChatFluent() throws InterruptedException {
        val userPrompt = "你好我叫小明 , 你能幫我介紹一下自己嗎?";
        log.info("request userPrompt: {}", userPrompt);
        val content = client.prompt()
                .system("你是我的好朋友 , 叫小厚 , 是一名軟體工程師")
                .user(userPrompt)
                .stream()
                .content();
        content.subscribe((contentSplit) -> log.info("response content: {}", contentSplit));

        Thread.sleep(10000);
    }

    @Test
    public void syncChatNonFluent() {
        val userPrompt = "你好我叫小明 , 你能幫我介紹一下自己嗎?";
        log.info("request userPrompt: {}", userPrompt);
        val content = client
                .prompt(
                        new Prompt(
                                new SystemMessage("你是一個最佳好友 , 叫小厚 , 我是軟體工程師")
                                , new UserMessage(userPrompt)
                        )
                )
                .call()
                .content();
        log.info("response content: {}", content);
    }

    @Test
    public void syncChatWithMetadata() {
        val userPrompt = "你好";
        log.info("request userPrompt: {}", userPrompt);
        val content = client.prompt()
                .user(u ->
                        u.text(userPrompt)
                                // 可以給 advisor 存取
                                .metadata("key1", "value1")
                )
                .call()
                .content();
        log.info("response content: {}", content);
    }

}
