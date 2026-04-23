package com.ai.document.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatLoggerAdvisor {
    
    /**
     * Log chat request details
     */
    public void logRequest(Prompt prompt) {
        log.info("=== Chat Request ===");
        log.info("Number of messages: {}", prompt.getInstructions().size());
        
        for (Message message : prompt.getInstructions()) {
            log.info("Message Type: {}, Content: {}", 
                message.getMessageType(), 
                message.getContent().length() > 200 
                    ? message.getContent().substring(0, 200) + "..." 
                    : message.getContent());
        }
        
        if (prompt.getOptions() != null) {
            log.info("Chat Options: {}", prompt.getOptions());
        }
    }
    
    /**
     * Log chat response details
     */
    public void logResponse(ChatResponse response) {
        log.info("=== Chat Response ===");
        
        if (response.getResult() != null) {
            String content = response.getResult().getOutput().getContent();
            log.info("Response Length: {} characters", content.length());
            log.info("Response Content: {}", 
                content.length() > 500 
                    ? content.substring(0, 500) + "..." 
                    : content);
        }
        
        if (response.getMetadata() != null) {
            log.info("Metadata: {}", response.getMetadata());
        }
    }
    
    /**
     * Log error during chat
     */
    public void logError(String query, Exception e) {
        log.error("=== Chat Error ===");
        log.error("Query: {}", query);
        log.error("Error: {}", e.getMessage(), e);
    }
}
