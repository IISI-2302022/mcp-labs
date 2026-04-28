package com.iisigroup.df.labs;

import com.iisigroup.df.labs.base.MySpringBootTest;
import com.iisigroup.df.labs.config.LoggingConfig;
import com.iisigroup.df.labs.tool.DateTimeTool;
import com.iisigroup.df.labs.tool.UUIDTool;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Map;

@Import(LoggingConfig.class)
@Slf4j
@MySpringBootTest
public class ChatFunctionCallTest {
    @Autowired
    private SimpleLoggerAdvisor simpleLoggerAdvisor;

    @Autowired
    private ChatClient.Builder builder;

    private ChatClient client;

    @PostConstruct
    public void init() {
        this.client = builder
                .defaultAdvisors(simpleLoggerAdvisor)
                .build();
    }


    @Test
    public void syncChatWithDateTimeTool1() {
        val userPrompt = "What day is tomorrow?";

        log.info("request userPrompt: {}", userPrompt);

        val content = client
                .prompt(userPrompt)
                .tools(new DateTimeTool())
                .call()
                .content();

        log.info("response content: {}", content);
    }

    @Test
    public void syncChatWithDateTimeTool2() {
        val userPrompt = "Can you set an alarm 10 minutes from now?";

        log.info("request userPrompt: {}", userPrompt);

        val content = client
                .prompt(userPrompt)
                .tools(new DateTimeTool())
                .call()
                .content();

        log.info("response content: {}", content);
    }

    @Test
    public void syncChatWithUUIDToolAndToolContext() {
        val userPrompt = "產生 uuid";

        log.info("request userPrompt: {}", userPrompt);

        val content = client
                .prompt(userPrompt)
                .tools(new UUIDTool())
                .toolContext(
                        // 可以給 tool method 吃
                        Map.of("defaultLength", 5)
                )
                .call()
                .content();

        log.info("response content: {}", content);
    }
}
