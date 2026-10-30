package com.abdoheddaya.ebankbot;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
public class McpTestController {
    private final ToolCallbackProvider mcpTools;

    public McpTestController(ToolCallbackProvider mcpTools) {
        this.mcpTools = mcpTools;
    }

    @GetMapping("/tools")
    public List<String> tools() {
        return Arrays.stream(mcpTools.getToolCallbacks())
                .map(tool -> tool.getToolDefinition().name()).toList();
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        Map<String, String> result = new LinkedHashMap<>();
        for (ToolCallback tool : mcpTools.getToolCallbacks()) {
            String name = tool.getToolDefinition().name();
            if (name.endsWith("list_customers") || name.endsWith("list_accounts")) {
                result.put(name, tool.call("{}"));
            }
        }
        return result;
    }
}
