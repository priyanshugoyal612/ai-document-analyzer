package com.ai.document.repository;

import com.ai.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    Optional<Document> findByFilename(String filename);
    
    List<Document> findByProcessingStatus(Document.ProcessingStatus status);
    
    @Query("SELECT d FROM Document d WHERE d.originalFilename LIKE %:keyword% OR d.filename LIKE %:keyword%")
    List<Document> findByFilenameContaining(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.processingStatus = :status")
    long countByProcessingStatus(@Param("status") Document.ProcessingStatus status);
    
    boolean existsByFilename(String filename);
}
