
package com.ming.mymcpclient.config;

import lombok.val;
import org.springaicommunity.mcp.security.client.sync.AuthenticationMcpTransportContextProvider;
import org.springaicommunity.mcp.security.client.sync.oauth2.webclient.McpOAuth2HybridExchangeFilterFunction;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
class McpConfiguration {

    @Bean
    McpSyncClientCustomizer syncClientCustomizer() {
        return (name, syncSpec) -> syncSpec.transportContextProvider(new AuthenticationMcpTransportContextProvider());
    }

    @Bean
    WebClientCustomizer webClientCustomizer(
            OAuth2AuthorizedClientManager clientManager
            , ClientRegistrationRepository clientRegistrationRepository
            , OAuth2AuthorizedClientService oAuth2AuthorizedClientService
    ) {
        return webClientBuilder -> webClientBuilder
                .filter(new McpOAuth2HybridExchangeFilterFunction(
                                clientManager
                                , getServiceAuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
                                , "auth-code"
                                , "client-credentials"
                        )
                );
    }

    private AuthorizedClientServiceOAuth2AuthorizedClientManager getServiceAuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        val manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService
        );
        manager.setAuthorizedClientProvider(authorizedClientProvider);
        return manager;
    }

}
