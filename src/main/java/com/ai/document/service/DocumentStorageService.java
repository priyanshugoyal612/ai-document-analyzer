package com.ai.document.service;

import com.ai.document.entity.Document;
import com.ai.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentStorageService {
    
    private final DocumentRepository documentRepository;
    private final DocumentParsingService documentParsingService;
    
    @Value("${app.document.storage.path}")
    private String storagePath;
    
    @Value("${app.document.storage.max-size}")
    private long maxFileSize;
    
    public Document uploadDocument(MultipartFile file) throws IOException {
        validateFile(file);
        
        String filename = generateUniqueFilename(file);
        Path filePath = createStorageDirectory().resolve(filename);
        
        // Save file to storage
        Files.copy(file.getInputStream(), filePath);
        
        // Create document entity
        Document document = Document.builder()
            .filename(filename)
            .originalFilename(file.getOriginalFilename())
            .contentType(file.getContentType())
            .fileSize(file.getSize())
            .filePath(filePath.toString())
            .processingStatus(Document.ProcessingStatus.PENDING)
            .build();
        
        return documentRepository.save(document);
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }
        
        if (!documentParsingService.isSupportedContentType(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported content type: " + file.getContentType());
        }
    }
    
    private String generateUniqueFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = documentParsingService.getFileExtension(file.getContentType());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s%s%s", 
            originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "document",
            timestamp,
            uuid,
            extension);
    }
    
    private Path createStorageDirectory() throws IOException {
        Path path = Paths.get(storagePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.info("Created storage directory: {}", path);
        }
        return path;
    }
    
    public void deleteDocument(Document document) throws IOException {
        // Delete file from storage
        Path filePath = Paths.get(document.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Deleted file: {}", filePath);
        }
        
        // Delete from database
        documentRepository.delete(document);
        log.info("Deleted document record: {}", document.getId());
    }
    
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
    
    public Document getDocument(Long id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + id));
    }
    
    public byte[] getDocumentContent(Long id) throws IOException {
        Document document = getDocument(id);
        Path filePath = Paths.get(document.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }
        
        return Files.readAllBytes(filePath);
    }
    
    public Document updateDocument(Document document) {
        return documentRepository.save(document);
    }
}
