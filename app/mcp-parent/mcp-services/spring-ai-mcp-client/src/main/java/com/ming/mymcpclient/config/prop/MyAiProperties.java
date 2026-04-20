package com.ming.mymcpclient.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("my.ai")
public class MyAiProperties {

    private McpProperties mcp = new McpProperties();
    private LlmProperties llm = new LlmProperties();

    @Data
    public static class McpProperties {
        private String user;
        private String pass;
    }

    @Data
    public static class LlmProperties {
        private Boolean toolRqAcceptAdditionalProperties = true;
        private Boolean isSendApiKey = true;
    }
}
