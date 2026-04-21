package com.ming.mymcpserver.tool;

import com.ming.mymcpserver.tool.label.AutoRegisterMcpTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExampleTool implements AutoRegisterMcpTool {

    @Tool(description = "答錄機工具，輸入任意文字，回覆相同的文字")
    public String sameReply(@ToolParam(description = "任意文字輸入") String something) {
        return something;
    }

}
