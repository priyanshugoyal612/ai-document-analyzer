package com.ai.document.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageIndexService {
    
    private final RestTemplate restTemplate;
    
    @Value("${app.pageindex.api-key}")
    private String apiKey;
    
    @Value("${app.pageindex.api-url}")
    private String apiUrl;
    
    public String uploadDocument(File file) {
        log.info("Uploading document to PageIndex: {}", file.getName());
        
        String url = apiUrl + "/doc/";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("api_key", apiKey);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<PageIndexUploadResponse> response = restTemplate.postForEntity(
                url, requestEntity, PageIndexUploadResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String docId = response.getBody().getDocId();
                log.info("Document uploaded successfully. doc_id: {}", docId);
                return docId;
            } else {
                log.error("Failed to upload document. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to upload document to PageIndex");
            }
        } catch (Exception e) {
            log.error("Error uploading document to PageIndex", e);
            throw new RuntimeException("Error uploading document to PageIndex: " + e.getMessage(), e);
        }
    }
    
    public String getDocumentStatus(String docId) {
        log.info("Checking document status for doc_id: {}", docId);
        
        String url = apiUrl + "/doc/" + docId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("api_key", apiKey);
        
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String status = (String) response.getBody().get("status");
                log.info("Document status: {}", status);
                return status;
            } else {
                log.error("Failed to get document status. Status: {}", response.getStatusCode());
                return "unknown";
            }
        } catch (Exception e) {
            log.error("Error getting document status", e);
            return "error";
        }
    }
    
    public String queryDocument(String query, String docId) {
        log.info("Querying document with PageIndex. doc_id: {}, query: {}", docId, query);
        
        String url = apiUrl + "/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api_key", apiKey);
        
        PageIndexChatRequest request = new PageIndexChatRequest();
        request.setMessages(List.of(new Message("user", query)));
        request.setDocId(docId);
        request.setStream(false);
        
        HttpEntity<PageIndexChatRequest> requestEntity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<PageIndexChatResponse> response = restTemplate.postForEntity(
                url, requestEntity, PageIndexChatResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String answer = response.getBody().getChoices().get(0).getMessage().getContent();
                log.info("Query successful");
                return answer;
            } else {
                log.error("Failed to query document. Status: {}", response.getStatusCode());
                return "Failed to query document";
            }
        } catch (Exception e) {
            log.error("Error querying document", e);
            return "Error querying document: " + e.getMessage();
        }
    }
    
    public String queryAllDocuments(String query, List<String> docIds) {
        log.info("Querying multiple documents with PageIndex. doc_ids: {}, query: {}", docIds, query);
        
        String url = apiUrl + "/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api_key", apiKey);
        
        PageIndexChatRequest request = new PageIndexChatRequest();
        request.setMessages(List.of(new Message("user", query)));
        request.setDocIds(docIds);
        request.setStream(false);
        
        HttpEntity<PageIndexChatRequest> requestEntity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<PageIndexChatResponse> response = restTemplate.postForEntity(
                url, requestEntity, PageIndexChatResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String answer = response.getBody().getChoices().get(0).getMessage().getContent();
                log.info("Query successful");
                return answer;
            } else {
                log.error("Failed to query documents. Status: {}", response.getStatusCode());
                return "Failed to query documents";
            }
        } catch (Exception e) {
            log.error("Error querying documents", e);
            return "Error querying documents: " + e.getMessage();
        }
    }
    
    @Data
    public static class PageIndexUploadResponse {
        @JsonProperty("doc_id")
        private String docId;
    }
    
    @Data
    public static class PageIndexChatRequest {
        private List<Message> messages;
        @JsonProperty("doc_id")
        private String docId;
        @JsonProperty("doc_ids")
        private List<String> docIds;
        private boolean stream;
    }
    
    @Data
    public static class Message {
        private String role;
        private String content;
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
    
    @Data
    public static class PageIndexChatResponse {
        private String id;
        private List<Choice> choices;
        private Usage usage;
    }
    
    @Data
    public static class Choice {
        private Message message;
        private String finishReason;
    }
    
    @Data
    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
    }
}
