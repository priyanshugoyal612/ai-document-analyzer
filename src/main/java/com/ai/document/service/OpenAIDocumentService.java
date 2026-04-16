package com.ai.document.service;

import com.ai.document.entity.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIDocumentService {
    
    private final DocumentStorageService documentStorageService;
    private final DocumentIndexingService documentIndexingService;
    
    public Document uploadAndIndexDocument(MultipartFile file) throws IOException {
        log.info("Uploading and indexing document with OpenAI: {}", file.getOriginalFilename());
        
        // Upload document locally
        Document document = documentStorageService.uploadDocument(file);
        
        // Get the file from storage for async processing
        File localFile = new File(document.getFilePath());
        
        // Index document using custom implementation with File object
        documentIndexingService.indexDocumentAsync(document, localFile);
        
        return document;
    }
    
    public CompletableFuture<Void> indexDocumentAsync(Document document, File file) {
        return documentIndexingService.indexDocumentAsync(document, file);
    }
    
    public void reindexDocument(Document document, MultipartFile file) throws Exception {
        log.info("Reindexing document with OpenAI: {}", document.getOriginalFilename());
        documentIndexingService.reindexDocument(document, file);
    }
}
