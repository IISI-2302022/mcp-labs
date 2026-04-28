
package com.ming.mymcpclient.config;

import org.springaicommunity.mcp.security.client.sync.AuthenticationMcpTransportContextProvider;
import org.springaicommunity.mcp.security.client.sync.oauth2.webclient.McpOAuth2AuthorizationCodeExchangeFilterFunction;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

@Configuration
class McpConfiguration {

    @Bean
    McpSyncClientCustomizer syncClientCustomizer() {
        return (name, syncSpec) -> syncSpec.transportContextProvider(new AuthenticationMcpTransportContextProvider());
    }

    @Bean
    WebClientCustomizer webClientCustomizer(OAuth2AuthorizedClientManager clientManager) {
        return webClientBuilder -> webClientBuilder
                .filter(new McpOAuth2AuthorizationCodeExchangeFilterFunction(clientManager, "auth-code"));
    }

}
