package com.ai.document.service;

import com.ai.document.entity.Document;
import com.ai.document.entity.DocumentPage;
import com.ai.document.repository.DocumentPageRepository;
import com.ai.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIndexingService {
    
    private final DocumentPageRepository documentPageRepository;
    private final DocumentParsingService documentParsingService;
    private final DocumentRepository documentRepository;
    
    @Async
    @Transactional
    public CompletableFuture<Void> indexDocumentAsync(Document document, java.io.File file) {
        try {
            log.info("Starting indexing for document: {}", document.getId());
            
            // Update status to processing
            document.setProcessingStatus(Document.ProcessingStatus.PROCESSING);
            documentRepository.save(document);
            
            // Extract pages from document
            List<String> pages = documentParsingService.extractPagesFromFile(file, document.getContentType());
            
            // Clear existing pages if any
            documentPageRepository.deleteByDocumentId(document.getId());
            
            // Index each page
            for (int i = 0; i < pages.size(); i++) {
                String pageContent = pages.get(i);
                
                DocumentPage documentPage = DocumentPage.builder()
                    .document(document)
                    .pageNumber(i + 1)
                    .pageContent(pageContent)
                    .build();
                
                documentPageRepository.save(documentPage);
            }
            
            // Update document with total pages and status
            document.setTotalPages(pages.size());
            document.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
            document.setErrorMessage(null);
            documentRepository.save(document);
            
            log.info("Successfully indexed {} pages for document: {}", pages.size(), document.getId());
            
        } catch (Exception e) {
            log.error("Error indexing document: {}", document.getId(), e);
            document.setProcessingStatus(Document.ProcessingStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Transactional
    public void reindexDocument(Document document, org.springframework.web.multipart.MultipartFile file) throws Exception {
        log.info("Reindexing document: {}", document.getId());
        
        // Clear existing pages
        documentPageRepository.deleteByDocumentId(document.getId());
        
        // Extract and index pages
        List<String> pages = documentParsingService.extractPages(file);
        
        for (int i = 0; i < pages.size(); i++) {
            String pageContent = pages.get(i);
            
            DocumentPage documentPage = DocumentPage.builder()
                .document(document)
                .pageNumber(i + 1)
                .pageContent(pageContent)
                .build();
            
            documentPageRepository.save(documentPage);
        }
        
        // Update document
        document.setTotalPages(pages.size());
        document.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
        document.setErrorMessage(null);
        
        log.info("Successfully reindexed {} pages for document: {}", pages.size(), document.getId());
    }
    
    public List<DocumentPage> searchPagesByKeyword(String keyword, int maxResults) {
        List<DocumentPage> pages = documentPageRepository.findByContentContaining(keyword);
        
        if (pages.size() > maxResults) {
            return pages.subList(0, maxResults);
        }
        
        return pages;
    }
    
    public List<DocumentPage> searchPagesByMultipleKeywords(String keyword1, String keyword2, int maxResults) {
        List<DocumentPage> pages = documentPageRepository.findByMultipleKeywords(keyword1, keyword2);
        
        if (pages.size() > maxResults) {
            return pages.subList(0, maxResults);
        }
        
        return pages;
    }
    
    public List<DocumentPage> getDocumentPages(Long documentId) {
        return documentPageRepository.findByDocumentIdOrderByPageNumber(documentId);
    }
    
    public DocumentPage getPage(Long documentId, int pageNumber) {
        return documentPageRepository.findByDocumentIdAndPageNumber(documentId, pageNumber);
    }
}
