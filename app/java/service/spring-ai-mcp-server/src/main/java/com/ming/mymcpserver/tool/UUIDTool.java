package com.ming.mymcpserver.tool;

import com.ming.mymcpserver.label.AutoRegisterMcpTool;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class UUIDTool implements AutoRegisterMcpTool {

    @Tool(description = "UUID 產生器")
    public String generateUUID(@ToolParam(required = false, description = "前幾碼") Integer length) {
        val uuid = UUID.randomUUID().toString().replace("-", "");
        log.info("產生無 '-' 之 uuid: {}", uuid);

        if (length == null || length >= uuid.length()) {
            return uuid;
        }

        return uuid.substring(0, length);
    }

}
