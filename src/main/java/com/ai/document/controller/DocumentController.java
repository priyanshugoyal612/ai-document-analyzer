package com.ai.document.controller;

import com.ai.document.dto.DocumentUploadResponse;
import com.ai.document.dto.DocumentResponse;
import com.ai.document.entity.Document;
import com.ai.document.service.DocumentIndexingService;
import com.ai.document.service.DocumentStorageService;
import com.ai.document.service.PageIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DocumentController {
    
    private final DocumentStorageService documentStorageService;
    private final DocumentIndexingService documentIndexingService;
    private final PageIndexService pageIndexService;
    
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading document to PageIndex: {}", file.getOriginalFilename());
            
            // Upload and store document locally
            Document document = documentStorageService.uploadDocument(file);
            
            // Upload to PageIndex API
            File localFile = new File(document.getFilePath());
            String pageindexDocId = pageIndexService.uploadDocument(localFile);
            
            // Update document with PageIndex doc_id
            document.setPageindexDocId(pageindexDocId);
            document.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
            documentStorageService.updateDocument(document);
            
            DocumentUploadResponse response = DocumentUploadResponse.builder()
                .id(document.getId())
                .filename(document.getOriginalFilename())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .status(document.getProcessingStatus().toString())
                .message("Document uploaded to PageIndex successfully. Ready for queries.")
                .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IOException e) {
            log.error("Error uploading document: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .message("Error uploading document: " + e.getMessage())
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file upload: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(DocumentUploadResponse.builder()
                    .message("Invalid file: " + e.getMessage())
                    .build());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<Document> documents = documentStorageService.getAllDocuments();
        
        List<DocumentResponse> responses = documents.stream()
            .map(this::convertToDocumentResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        try {
            Document document = documentStorageService.getDocument(id);
            DocumentResponse response = convertToDocumentResponse(document);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        try {
            Document document = documentStorageService.getDocument(id);
            byte[] content = documentStorageService.getDocumentContent(id);
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + document.getOriginalFilename() + "\"")
                .header("Content-Type", document.getContentType())
                .body(content);
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Error downloading document: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            Document document = documentStorageService.getDocument(id);
            documentStorageService.deleteDocument(document);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Error deleting document: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{id}/reindex")
    public ResponseEntity<DocumentResponse> reindexDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            Document document = documentStorageService.getDocument(id);
            documentIndexingService.reindexDocument(document, file);
            
            DocumentResponse response = convertToDocumentResponse(document);
            response.setMessage("Document reindexed successfully");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error reindexing document: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private DocumentResponse convertToDocumentResponse(Document document) {
        return DocumentResponse.builder()
            .id(document.getId())
            .filename(document.getOriginalFilename())
            .contentType(document.getContentType())
            .fileSize(document.getFileSize())
            .totalPages(document.getTotalPages())
            .status(document.getProcessingStatus().toString())
            .errorMessage(document.getErrorMessage())
            .createdAt(document.getCreatedAt())
            .updatedAt(document.getUpdatedAt())
            .build();
    }
}
