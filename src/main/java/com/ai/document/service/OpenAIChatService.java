package com.ai.document.service;

import com.ai.document.entity.DocumentPage;
import com.ai.document.repository.DocumentPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIChatService {
    
    private final ChatClient chatClient;
    private final DocumentPageRepository documentPageRepository;
    
    public String chatWithDocuments(String query) {
        log.info("Chatting with all documents using OpenAI: {}", query);
        
        // Get all document pages
        List<DocumentPage> allPages = documentPageRepository.findAll();
        
        if (allPages.isEmpty()) {
            return "No documents available to chat with. Please upload a document first.";
        }
        
        // Build context from all pages
        String context = allPages.stream()
            .map(page -> String.format("[Page %d]: %s", page.getPageNumber(), page.getPageContent()))
            .collect(Collectors.joining("\n\n"));
        
        return chatWithOpenAI(query, context);
    }
    
    public String chatWithDocument(Long documentId, String query) {
        log.info("Chatting with document {} using OpenAI: {}", documentId, query);
        
        // Get pages for the specific document
        List<DocumentPage> pages = documentPageRepository.findByDocumentIdOrderByPageNumber(documentId);
        
        if (pages.isEmpty()) {
            return "No pages found for this document. The document may not be indexed yet.";
        }
        
        // Build context from document pages
        String context = pages.stream()
            .map(page -> String.format("[Page %d]: %s", page.getPageNumber(), page.getPageContent()))
            .collect(Collectors.joining("\n\n"));
        
        return chatWithOpenAI(query, context);
    }
    
    private String chatWithOpenAI(String query, String context) {
        String systemPrompt = """
            You are a helpful AI assistant. Answer the user's question based on the provided document context.
            If the context doesn't contain enough information to answer the question, say so.
            Use the page numbers to reference specific parts of the document.
            
            Document Context:
            {context}
            """;
        
        String fullPrompt = systemPrompt.replace("{context}", context) + "\n\nQuestion: " + query;
        
        String response = chatClient.prompt()
            .user(fullPrompt)
            .call()
            .content();
        
        return response;
    }
    
    public String chatDirectly(String query, String context) {
        log.info("Direct chat with OpenAI using provided context");
        
        String systemPrompt = """
            You are a helpful AI assistant. Answer the user's question based on the provided context.
            If the context doesn't contain enough information to answer the question, say so.
            
            Context:
            {context}
            """;
        
        String fullPrompt = systemPrompt.replace("{context}", context) + "\n\nQuestion: " + query;
        
        String response = chatClient.prompt()
            .user(fullPrompt)
            .call()
            .content();
        
        return response;
    }
    
    public String chatWithJson(String query, String jsonContext) {
        log.info("Chat with OpenAI using JSON context");
        
        String systemPrompt = """
            You are a helpful AI assistant. Answer the user's question based on the provided JSON context.
            Use the JSON data to provide accurate answers.
            
            JSON Context:
            {jsonContext}
            """;
        
        String fullPrompt = systemPrompt.replace("{jsonContext}", jsonContext) + "\n\nQuestion: " + query;
        
        String response = chatClient.prompt()
            .user(fullPrompt)
            .call()
            .content();
        
        return response;
    }
}
