package com.iisigroup.df.labs.tool;

import lombok.val;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.UUID;

public class UUIDTool {

    @Tool(description = "UUID 產生器")
    public String generateUUID(@ToolParam(required = false, description = "前幾碼") Integer length, ToolContext toolContext) {
        val uuid = UUID.randomUUID().toString().replace("-", "");
        if (length == null) {
            length = (Integer) toolContext.getContext().get("defaultLength");
        }
        if (length >= uuid.length()) {
            return uuid;
        }
        return uuid.substring(0, length);
    }

}
