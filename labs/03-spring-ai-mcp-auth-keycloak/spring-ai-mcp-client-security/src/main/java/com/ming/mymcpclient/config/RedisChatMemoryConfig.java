
package com.ming.mymcpclient.config;

import com.ming.mymcpclient.config.prop.RedisChatMemoryProperties;
import com.ming.mymcpclient.memory.redis.RedisChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisChatMemoryProperties.class)
public class RedisChatMemoryConfig {

    private final RedisChatMemoryProperties chatMemoryProperties;


    @Bean
    public RedisChatMemoryRepository redisChatMemoryRepository() {
        log.info("Configuring Redis chat memory repository");
        return RedisChatMemoryRepository.builder()
                .host(chatMemoryProperties.getHost())
                .port(chatMemoryProperties.getPort())
                .password(chatMemoryProperties.getPassword())
                .timeout(chatMemoryProperties.getTimeout())
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor redisMessageChatMemoryAdvisor(
            ChatMemory chatMemory
    ) {
        return MessageChatMemoryAdvisor.builder(chatMemory)
                .build();
    }

}