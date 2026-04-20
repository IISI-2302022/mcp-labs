package com.ming.mymcpserver.config;

import com.ming.mymcpserver.label.AutoRegisterMcpTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(ObjectProvider<AutoRegisterMcpTool> autoRegisterMcpToolObjectProvider) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(autoRegisterMcpToolObjectProvider.orderedStream().toArray())
                .build();
    }
}
