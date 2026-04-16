package com.ai.document.service;

import com.ai.document.entity.DocumentPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentQueryService {
    
    private final ChatClient chatClient;
    private final DocumentIndexingService documentIndexingService;
    
    @Value("${app.query.max-retrieved-pages}")
    private int maxRetrievedPages;
    
    @Value("${app.query.answer-max-tokens}")
    private int answerMaxTokens;
    
    public String queryDocuments(String query) {
        log.info("Processing query: {}", query);
        
        // Retrieve relevant pages using keyword search
        List<DocumentPage> relevantPages = retrieveRelevantPages(query);
        
        if (relevantPages.isEmpty()) {
            return "No relevant information found in the documents for your query.";
        }
        
        // Generate context from retrieved pages
        String context = buildContext(relevantPages);
        
        // Generate answer using Spring AI
        return generateAnswer(query, context);
    }
    
    private List<DocumentPage> retrieveRelevantPages(String query) {
        // Extract keywords from query (simple implementation)
        String[] keywords = query.toLowerCase().split("\\s+");
        
        List<DocumentPage> results = List.of();
        
        // Try single keyword searches first
        for (String keyword : keywords) {
            if (keyword.length() >= 3) { // Include 3-character words
                List<DocumentPage> pages = documentIndexingService.searchPagesByKeyword(keyword, maxRetrievedPages);
                results = mergeResults(results, pages, maxRetrievedPages);
            }
        }
        
        // If still no results, try multiple keyword combinations
        if (results.isEmpty() && keywords.length >= 2) {
            for (int i = 0; i < keywords.length - 1; i++) {
                String keyword1 = keywords[i];
                String keyword2 = keywords[i + 1];
                
                if (keyword1.length() >= 3 && keyword2.length() >= 3) {
                    List<DocumentPage> pages = documentIndexingService.searchPagesByMultipleKeywords(keyword1, keyword2, maxRetrievedPages);
                    results = mergeResults(results, pages, maxRetrievedPages);
                }
            }
        }
        
        return results;
    }
    
    private List<DocumentPage> mergeResults(List<DocumentPage> existing, List<DocumentPage> newPages, int maxResults) {
        // Simple merge without duplicates (based on ID)
        Map<Long, DocumentPage> merged = existing.stream()
            .collect(Collectors.toMap(DocumentPage::getId, page -> page));
        
        newPages.forEach(page -> merged.putIfAbsent(page.getId(), page));
        
        return merged.values().stream()
            .limit(maxResults)
            .collect(Collectors.toList());
    }
    
    private String buildContext(List<DocumentPage> pages) {
        StringBuilder context = new StringBuilder();
        context.append("Context from relevant documents:\n\n");
        
        for (int i = 0; i < pages.size(); i++) {
            DocumentPage page = pages.get(i);
            context.append(String.format("[Document: %s, Page: %d]\n", 
                page.getDocument().getOriginalFilename(), 
                page.getPageNumber()));
            context.append(page.getPageContent());
            context.append("\n---\n");
        }
        
        return context.toString();
    }
    
    private String generateAnswer(String query, String context) {
        String template = """
            You are a helpful assistant that answers questions based on the provided context.
            Use only the information from the context to answer the question.
            If the context doesn't contain enough information to answer the question, say so clearly.
            
            Context:
            {context}
            
            Question: {question}
            
            Answer:
            """;
        
        return chatClient.prompt()
            .user(template.formatted(context, query))
            .call()
            .content();
    }
    
    public String querySpecificDocument(Long documentId, String query) {
        log.info("Processing query for document {}: {}", documentId, query);
        
        // Get all pages from the specific document
        List<DocumentPage> documentPages = documentIndexingService.getDocumentPages(documentId);
        
        if (documentPages.isEmpty()) {
            return "No pages found for the specified document.";
        }
        
        // Filter pages based on query keywords
        List<DocumentPage> relevantPages = filterPagesByQuery(documentPages, query);
        
        if (relevantPages.isEmpty()) {
            return "No relevant information found in this document for your query.";
        }
        
        // Generate context from filtered pages
        String context = buildContext(relevantPages);
        
        // Generate answer using Spring AI
        return generateAnswer(query, context);
    }
    
    private List<DocumentPage> filterPagesByQuery(List<DocumentPage> pages, String query) {
        String[] keywords = query.toLowerCase().split("\\s+");
        
        return pages.stream()
            .filter(page -> {
                String content = page.getPageContent().toLowerCase();
                for (String keyword : keywords) {
                    if (keyword.length() > 3 && content.contains(keyword)) {
                        return true;
                    }
                }
                return false;
            })
            .limit(maxRetrievedPages)
            .collect(Collectors.toList());
    }
}
