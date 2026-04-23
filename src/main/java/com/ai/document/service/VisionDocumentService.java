package com.ai.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisionDocumentService {
    
    private final ChatClient chatClient;
    
    /**
     * Analyze an image using OpenAI Vision model
     * @param imageData Base64 encoded image data
     * @param mimeType MIME type of the image (e.g., image/png, image/jpeg)
     * @param query Question to ask about the image
     * @return Response from the vision model
     */
    public String analyzeImage(String imageData, String mimeType, String query) {
        log.info("Analyzing image with vision model. Query: {}", query);
        
        String systemPrompt = """
            You are a helpful AI assistant with vision capabilities. 
            Analyze the provided image and answer the user's question accurately.
            If the image contains text, extract it precisely.
            If the image contains charts, graphs, or diagrams, describe them in detail.
            """;
        
        // Format base64 data as data URL for OpenAI vision API
        String dataUrl = String.format("data:%s;base64,%s", mimeType, imageData);
        
        UserMessage userMessage = new UserMessage(
            query,
            List.of(new org.springframework.ai.chat.messages.Media(
                org.springframework.util.MimeTypeUtils.parseMimeType(mimeType),
                dataUrl
            ))
        );
        
        Prompt prompt = new Prompt(
            List.of(new SystemPromptTemplate(systemPrompt).createMessage(), userMessage),
            OpenAiChatOptions.builder()
                .withModel("gpt-4o")
                .build()
        );
        
        String response = chatClient.prompt(prompt).call().content();
        log.info("Vision model response received");
        
        return response;
    }
    
    /**
     * Extract text from an image using OCR via vision model
     * @param imageData Base64 encoded image data
     * @param mimeType MIME type of the image
     * @return Extracted text from the image
     */
    public String extractTextFromImage(String imageData, String mimeType) {
        log.info("Extracting text from image using vision model");
        
        String query = "Extract all text from this image. Provide the text exactly as it appears, maintaining the structure and formatting.";
        return analyzeImage(imageData, mimeType, query);
    }
    
    /**
     * Analyze a chart or graph in an image
     * @param imageData Base64 encoded image data
     * @param mimeType MIME type of the image
     * @return Detailed description of the chart/graph
     */
    public String analyzeChart(String imageData, String mimeType) {
        log.info("Analyzing chart/graph in image");
        
        String query = "Analyze this chart or graph. Describe the type of chart, axes, data points, trends, and any key insights.";
        return analyzeImage(imageData, mimeType, query);
    }
    
    /**
     * Convert byte array to base64 string
     * @param imageBytes Image data as byte array
     * @return Base64 encoded string
     */
    public String encodeToBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
