package com.ming.mymcpserver.api;

import com.ming.mymcpserver.config.prop.KongAdminProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class KongAdminClient {

    public static final String DEFAULT_WORKSPACE = "default";

    private final KongAdminProperties kongAdminProperties;
    private final WebClient webClient;

    public KongAdminClient(KongAdminProperties kongAdminProperties, WebClient.Builder builder) {
        this.kongAdminProperties = kongAdminProperties;
        this.webClient = builder
                .defaultHeader("kong-admin-token", kongAdminProperties.getToken())
                .build();
    }

    public Mono<Map<String, Object>> listPlugins(String workspace) {
        return webClient.get()
                .uri(kongAdminProperties.getBaseUrl() + "/" + getWSWithDefault(workspace) + "/plugins")
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchangeToMono((clientResponse) -> clientResponse.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                }))
                .switchIfEmpty(Mono.defer(() -> Mono.just(new HashMap<>())));
    }

    public Mono<Map<String, Object>> listRoutes(String workspace) {
        return webClient.get()
                .uri(kongAdminProperties.getBaseUrl() + "/" + getWSWithDefault(workspace) + "/routes")
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchangeToMono((clientResponse) -> clientResponse.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                }))
                .switchIfEmpty(Mono.defer(() -> Mono.just(new HashMap<>())));
    }

    private String getWSWithDefault(String workspace) {
        return StringUtils.hasText(workspace)
                ? workspace.trim()
                : DEFAULT_WORKSPACE;
    }


}
