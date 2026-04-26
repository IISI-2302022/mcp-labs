package com.ming.mymcpclient.controller;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.val;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
class DemoController {


    private final List<McpSyncClient> clients;

    private final ChatClient chatClient;

    DemoController(ChatClient.Builder chatClientBuilder, ToolCallbackProvider toolCallbackProvider, List<McpSyncClient> clients, MessageChatMemoryAdvisor messageChatMemoryAdvisor) {
        this.clients = clients;
        this.chatClient = chatClientBuilder
                .defaultAdvisors(messageChatMemoryAdvisor)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }

    @GetMapping("/")
    String index(@RequestParam(required = false) String query, @RequestParam(required = false, defaultValue = "test") String conversationId) {
        var currentWeatherBlock = "";
        if (StringUtils.hasText(query)) {
            val chatResponse = chatClient.prompt(query)
                    .advisors((advisorSpec) ->
                            advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();

            currentWeatherBlock = """
                    <h2>問題: %s</h2>
                    <p>回復: %s</p>
                    <form action="" method="GET">
                    <button type="submit">Clear</button>
                    </form>
                    """.formatted(query, chatResponse);
        }

        val currentMcpServersBlock = this.clients.stream()
                .map(McpSyncClient::getClientInfo)
                .map(McpSchema.Implementation::name)
                .map("    <li>%s</li>"::formatted)
                .collect(Collectors.joining("\n"));

        return """
                <h1>Demo controller</h1>
                %s
                
                <hr>
                
                <p>請輸入你想問 AI 的問題</p>
                <form action="" method="GET">
                    <label for="conversationId">ConversationId:</label>
                    <input type="text" id="conversationId" name="conversationId" value="%s" placeholder="對話記憶 sessionId" />
                    <br/>
                    <label for="query">Query:</label>
                    <input type="text" id="query" name="query" value="%s" placeholder="請 AI 進行轉帳操作" />
                    <button type="submit">Ask the LLM</button>
                </form>
                
                <hr>
                
                <h2>Registered MCP servers:</h2>
                <ul>
                %s
                </ul>
                """.formatted(currentWeatherBlock, conversationId, Optional.ofNullable(query).orElse(""), currentMcpServersBlock);
    }

}
