package com.ai.document.controller;

import com.ai.document.dto.DocumentUploadResponse;
import com.ai.document.dto.DocumentResponse;
import com.ai.document.dto.QueryRequest;
import com.ai.document.dto.QueryResponse;
import com.ai.document.entity.Document;
import com.ai.document.service.OpenAIChatService;
import com.ai.document.service.OpenAIDocumentService;
import com.ai.document.service.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/openai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OpenAIController {
    
    private final OpenAIDocumentService openAIDocumentService;
    private final OpenAIChatService openAIChatService;
    private final DocumentStorageService documentStorageService;
    
    @PostMapping("/documents/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading document with OpenAI indexing: {}", file.getOriginalFilename());
            
            Document document = openAIDocumentService.uploadAndIndexDocument(file);
            
            DocumentUploadResponse response = DocumentUploadResponse.builder()
                .id(document.getId())
                .filename(document.getOriginalFilename())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .status(document.getProcessingStatus().toString())
                .message("Document uploaded successfully. OpenAI indexing started.")
                .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IOException e) {
            log.error("Error uploading document: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .message("Error uploading document: " + e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error processing document: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .message("Error processing document: " + e.getMessage())
                    .build());
        }
    }
    
    @PostMapping("/documents/{id}/reindex")
    public ResponseEntity<DocumentResponse> reindexDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            Document document = documentStorageService.getDocument(id);
            openAIDocumentService.reindexDocument(document, file);
            
            DocumentResponse response = DocumentResponse.builder()
                .id(document.getId())
                .filename(document.getOriginalFilename())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .totalPages(document.getTotalPages())
                .status(document.getProcessingStatus().toString())
                .errorMessage(document.getErrorMessage())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .message("Document reindexed successfully with OpenAI")
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error reindexing document: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/chat")
    public ResponseEntity<QueryResponse> chatWithDocuments(@RequestBody QueryRequest request) {
        try {
            log.info("Chatting with all documents using OpenAI: {}", request.getQuery());
            
            String answer = openAIChatService.chatWithDocuments(request.getQuery());
            
            QueryResponse response = QueryResponse.builder()
                .query(request.getQuery())
                .answer(answer)
                .success(true)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing chat request: {}", request.getQuery(), e);
            
            QueryResponse response = QueryResponse.builder()
                .query(request.getQuery())
                .answer("Error processing your request: " + e.getMessage())
                .success(false)
                .build();
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/chat/document/{documentId}")
    public ResponseEntity<QueryResponse> chatWithDocument(
            @PathVariable Long documentId,
            @RequestBody QueryRequest request) {
        try {
            log.info("Chatting with document {} using OpenAI: {}", documentId, request.getQuery());
            
            String answer = openAIChatService.chatWithDocument(documentId, request.getQuery());
            
            QueryResponse response = QueryResponse.builder()
                .query(request.getQuery())
                .answer(answer)
                .success(true)
                .documentId(documentId)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing chat request for document {}: {}", documentId, request.getQuery(), e);
            
            QueryResponse response = QueryResponse.builder()
                .query(request.getQuery())
                .answer("Error processing your request: " + e.getMessage())
                .success(false)
                .documentId(documentId)
                .build();
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/chat/direct")
    public ResponseEntity<Map<String, String>> chatDirectly(@RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            String context = request.get("context");
            
            log.info("Direct chat with OpenAI");
            
            String answer = openAIChatService.chatDirectly(query, context);
            
            return ResponseEntity.ok(Map.of(
                "query", query,
                "answer", answer,
                "success", "true"
            ));
            
        } catch (Exception e) {
            log.error("Error processing direct chat request", e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error processing your request: " + e.getMessage(),
                "success", "false"
            ));
        }
    }
    
    @PostMapping("/chat/json")
    public ResponseEntity<Map<String, String>> chatWithJson(@RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            String jsonContext = request.get("jsonContext");
            
            log.info("Chat with OpenAI using JSON context");
            
            String answer = openAIChatService.chatWithJson(query, jsonContext);
            
            return ResponseEntity.ok(Map.of(
                "query", query,
                "answer", answer,
                "success", "true"
            ));
            
        } catch (Exception e) {
            log.error("Error processing JSON chat request", e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error processing your request: " + e.getMessage(),
                "success", "false"
            ));
        }
    }
    
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OpenAI service is running");
    }
}
