package com.iisigroup.df.labs;

import com.iisigroup.df.labs.base.MySpringBootTest;
import com.iisigroup.df.labs.config.LoggingConfig;
import com.iisigroup.df.labs.config.MemoryConfig;
import com.iisigroup.df.labs.model.ActorFilms;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

@Import({LoggingConfig.class, MemoryConfig.class})
@Slf4j
@MySpringBootTest
public class ChatAdvisorTest {

    @Autowired
    private ChatClient.Builder builder;

    private ChatClient client;

    @Autowired
    private SimpleLoggerAdvisor simpleLoggerAdvisor;

    @Autowired
    private MessageChatMemoryAdvisor messageChatMemoryAdvisor;

    @PostConstruct
    public void init() {
        this.client = builder
                .build();
    }

    @Test
    public void syncChatWithLogging() {
        val userPrompt = "你好";
        log.info("request userPrompt: {}", userPrompt);

        val content = client.prompt(userPrompt)
                .advisors(simpleLoggerAdvisor)
                .call()
                .content();
        log.info("response content: {}", content);
    }

    @Test
    public void syncChatWithMemory() {

        val conversationId = "conversation-id-123";

        // todo 自己改
        val name = "ming";

        val userPrompt1 = "我叫做 {name} , 你好";

        log.info("request userPrompt1: {}", userPrompt1);

        val content = client.prompt()
                .user(u ->
                        u
                                .text(userPrompt1)
                                .param("name", name)
                )
                .advisors((advisorSpec) ->
                        advisorSpec
                                .advisors(messageChatMemoryAdvisor)
                                .param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .call()
                .content();

        log.info("response content1: {}", content);


        val userPrompt2 = "你知道我的名子嗎?";
        log.info("request userPrompt2: {}", userPrompt2);

        val content1 = client.prompt()
                .user(userPrompt2)
                .advisors((advisorSpec) ->
                        advisorSpec
                                .advisors(messageChatMemoryAdvisor)
                                .param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .call()
                .content();

        log.info("response content1: {}", content1);

    }


    @Test
    public void syncChatForEntityWithErrorRetry() {
        val parameterizedTypeReference = new ParameterizedTypeReference<List<ActorFilms>>() {
        };
        val validationAdvisor = StructuredOutputValidationAdvisor.builder()
                .outputType(parameterizedTypeReference)
                .maxRepeatAttempts(3)
                .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 1000)
                .build();

        val userPrompt = "為台灣演員許光漢和林依晨各列出 5 部電影的作品集。";
        log.info("request userPrompt: {}", userPrompt);
        val entity = client
                .prompt(userPrompt)
                .advisors(validationAdvisor)
                .call()
                .entity(parameterizedTypeReference);

        log.info("response content: {}", entity);
    }
}
