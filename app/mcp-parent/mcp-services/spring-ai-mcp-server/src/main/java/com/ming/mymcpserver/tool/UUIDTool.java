package com.ming.mymcpserver.tool;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class UUIDTool {

    @McpTool(description = "UUID 產生器")
    public Mono<String> generateUUID(@McpToolParam(required = false, description = "前幾碼") Integer length) {
        return Mono.fromSupplier(() -> UUID.randomUUID().toString().replace("-", ""))
                .map((uuid) -> {
                    if (length == null || length >= uuid.length()) {
                        return uuid;
                    }
                    return uuid.substring(0, length);
                })
                .doOnNext((uuid) -> log.info("產生 uuid: {}", uuid));
    }

}
