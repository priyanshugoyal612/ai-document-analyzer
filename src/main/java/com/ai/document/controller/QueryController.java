package com.ai.document.controller;

import com.ai.document.dto.QueryRequest;
import com.ai.document.dto.QueryResponse;
import com.ai.document.entity.Document;
import com.ai.document.service.DocumentStorageService;
import com.ai.document.service.PageIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/query")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class QueryController {
    
    private final DocumentStorageService documentStorageService;
    private final PageIndexService pageIndexService;
    
    @PostMapping
    public ResponseEntity<QueryResponse> queryDocuments(@RequestBody QueryRequest request) {
        try {
            log.info("Processing query: {}", request.getQuery());
            
            // Get all documents with PageIndex doc_ids
            List<Document> documents = documentStorageService.getAllDocuments();
            List<String> docIds = documents.stream()
                .filter(doc -> doc.getPageindexDocId() != null)
                .map(Document::getPageindexDocId)
                .collect(Collectors.toList());
            
            if (docIds.isEmpty()) {
                return ResponseEntity.ok(QueryResponse.builder()
                    .query(request.getQuery())
                    .answer("No documents available for querying. Please upload documents first.")
                    .success(false)
                    .build());
            }
            
            String answer = pageIndexService.queryAllDocuments(request.getQuery(), docIds);
            
            QueryResponse response = QueryResponse.builder()
                .query(request.getQuery())
                .answer(answer)
                .success(true)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing query: {}", request.getQuery(), e);
            
            QueryResponse response = QueryResponse.builder()
                .query(request.getQuery())
                .answer("Error processing your query: " + e.getMessage())
                .success(false)
                .build();
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/document/{documentId}")
    public ResponseEntity<QueryResponse> querySpecificDocument(
            @PathVariable Long documentId,
            @RequestBody QueryRequest request) {
        try {
            log.info("Processing query for document {}: {}", documentId, request.getQuery());
            
            Document document = documentStorageService.getDocument(documentId);
            
            if (document.getPageindexDocId() == null) {
                return ResponseEntity.ok(QueryResponse.builder()
                    .query(request.getQuery())
                    .answer("Document not yet processed by PageIndex. Please wait or re-upload the document.")
                    .success(false)
                    .documentId(documentId)
                    .build());
            }
            
            String answer = pageIndexService.queryDocument(request.getQuery(), document.getPageindexDocId());
            
            QueryResponse response = QueryResponse.builder()
                .query(request.getQuery())
                .answer(answer)
                .success(true)
                .documentId(documentId)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing query for document {}: {}", documentId, request.getQuery(), e);
            
            QueryResponse response = QueryResponse.builder()
                .query(request.getQuery())
                .answer("Error processing your query: " + e.getMessage())
                .success(false)
                .documentId(documentId)
                .build();
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Query service is running");
    }
}
