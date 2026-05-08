package com.iisigroup.df.labs.advisor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class PromptGuardAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        if (hasIllegalWord(chatClientRequest)) {
            throw new RuntimeException("不允許包含 hello 的訊息");
        }
        return callAdvisorChain.nextCall(chatClientRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        return Mono.just(chatClientRequest)
                .flatMapMany((clientRequest) -> {
                    if (hasIllegalWord(chatClientRequest)) {
                        return Flux.error(new RuntimeException("不允許包含 hello 的訊息"));
                    }
                    return streamAdvisorChain.nextStream(clientRequest);
                });
    }

    @Override
    public String getName() {
        return PromptGuardAdvisor.class.getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    protected boolean hasIllegalWord(ChatClientRequest chatClientRequest) {
        val userMessages = chatClientRequest.prompt()
                .getUserMessages();
        return userMessages.stream()
                .anyMatch((userMessage) ->
                        userMessage.getText().contains("hello")
                );
    }
}
