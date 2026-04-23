package com.ming.mymcpclient.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@Slf4j
@RequestMapping("/chat")
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(
//            MessageChatMemoryAdvisor memoryAdvisor,
            ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider toolCallbackProvider
    ) {
        this.chatClient = chatClientBuilder
//                .defaultAdvisors(memoryAdvisor)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }

    @RequestMapping("/sync")
    public String sync(@RequestParam String userPrompt, @RequestParam String contextId) {
        log.info("userPrompt: {}", userPrompt);
        return chatClient.prompt(userPrompt)
                .advisors(advisorSpec ->
                        advisorSpec
                                .param(ChatMemory.CONVERSATION_ID, contextId)
                )
                .call()
                .content();
    }


    @RequestMapping(value = "/async", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> async(@RequestParam String userPrompt, @RequestParam String contextId) {
        log.info("userPrompt: {}", userPrompt);
        return chatClient.prompt(userPrompt)
                .advisors(advisorSpec ->
                        advisorSpec
                                .param(ChatMemory.CONVERSATION_ID, contextId)
                )
                .stream()
                .content();
    }


}
