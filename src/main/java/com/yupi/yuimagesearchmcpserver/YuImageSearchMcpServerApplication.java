package com.yupi.yuimagesearchmcpserver;

import com.yupi.yuimagesearchmcpserver.tools.AlertManagementTool;
import com.yupi.yuimagesearchmcpserver.tools.AlertTool;
import com.yupi.yuimagesearchmcpserver.tools.ImageSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class YuImageSearchMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuImageSearchMcpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider imageSearchTools(ImageSearchTool imageSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageSearchTool)
                .build();
    }

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
