package com.iisigroup.df.labs;

import com.iisigroup.df.labs.base.MySpringBootTest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@MySpringBootTest
public class ChatPromptTemplateTest {

    @Autowired
    private ChatClient.Builder builder;

    private ChatClient client;

    @PostConstruct
    public void init() {
        this.client = builder
                .build();
    }

    @Test
    public void syncChatWithPromptTemplate() {
        val userPrompt = "請告訴我 5 部由 {composer} 創作配樂的電影名稱。";

        log.info("request userPrompt: {}", userPrompt);

        val content = this.client.prompt()
                .user(u -> u
                        .text(userPrompt)
                        .param("composer", "周杰倫")
                )
                .call()
                .content();

        log.info("response content: {}", content);
    }

    @Test
    public void syncChatWithPromptTemplateAndCustomReplacement() {
        val userPrompt = "請{you}告訴我 5 部由 <composer> 創作配樂的電影名稱。";

        log.info("request userPrompt: {}", userPrompt);

        val content = this.client.prompt()
                .user(u -> u
                        .text(userPrompt)
                        .param("composer", "周杰倫")
                )
                .templateRenderer(
                        StTemplateRenderer.builder()
                                .startDelimiterToken('<')
                                .endDelimiterToken('>')
                                .build()
                )
                .call()
                .content();

        log.info("response content: {}", content);
    }
}
