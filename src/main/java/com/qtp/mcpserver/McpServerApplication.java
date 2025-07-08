package com.qtp.mcpserver;

import com.qtp.mcpserver.tools.AlertManagementTool;
import com.qtp.mcpserver.tools.AlertTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }


    //TODO: 整合成一个
    @Bean
    public ToolCallbackProvider alertTools(AlertTool alertTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(alertTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider alertManagementTools(AlertManagementTool alertManagementTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(alertManagementTool)
                .build();
    }

}
