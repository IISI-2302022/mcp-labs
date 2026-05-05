package com.iisigroup.df.labs;

import com.iisigroup.df.labs.base.MySpringBootTest;
import com.iisigroup.df.labs.model.ActorFilms;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@MySpringBootTest
public class ChatForEntityTest {

    @Autowired
    private ChatClient.Builder builder;

    private ChatClient client;

    @PostConstruct
    public void init() {
        this.client = builder
                .build();
    }

    @Test
    public void syncChatForEntity() {
        val userPrompt = "隨機生成台灣一位演員的作品集";
        log.info("request userPrompt: {}", userPrompt);
        val entity = client
                .prompt(userPrompt)
                .call()
                .entity(ActorFilms.class);
        log.info("response content: {}", entity);
    }

    @Test
    public void syncChatReturnEntityWithParameterizedType() {
        val userPrompt = "為台灣演員許光漢和林依晨各列出 5 部電影的作品集。";
        log.info("request userPrompt: {}", userPrompt);
        val entity = client
                .prompt(userPrompt)
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {
                });
        log.info("response content: {}", entity);
    }


    @Test
    public void asyncChatForEntityWithParameterizedType() {
        val converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorFilms>>() {
        });

        val userPrompt = """
                  隨機生成台灣三位演員的作品集
                  {format}
                """;

        log.info("request userPrompt: {}", userPrompt);

        val contentFlux = client
                .prompt()
                .user((promptUserSpec) ->
                        promptUserSpec
                                .text(userPrompt)
                                .param("format", converter.getFormat())
                )
                .stream()
                .content();

        val content = contentFlux.collectList()
                .block()
                .stream()
                .collect(Collectors.joining());

        val actorFilms = converter.convert(content);

        log.info("response content: {}", actorFilms);

    }

}
