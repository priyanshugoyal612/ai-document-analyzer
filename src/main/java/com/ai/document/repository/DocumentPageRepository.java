package com.ai.document.repository;

import com.ai.document.entity.DocumentPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentPageRepository extends JpaRepository<DocumentPage, Long> {
    
    List<DocumentPage> findByDocumentId(Long documentId);
    
    List<DocumentPage> findByDocumentIdOrderByPageNumber(Long documentId);
    
    @Query("SELECT dp FROM DocumentPage dp WHERE dp.document.id = :documentId AND dp.pageNumber = :pageNumber")
    DocumentPage findByDocumentIdAndPageNumber(@Param("documentId") Long documentId, @Param("pageNumber") Integer pageNumber);
    
    @Query("SELECT dp FROM DocumentPage dp JOIN dp.document d WHERE " +
           "LOWER(dp.pageContent) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY d.createdAt DESC")
    List<DocumentPage> findByContentContaining(@Param("keyword") String keyword);
    
    @Query("SELECT dp FROM DocumentPage dp JOIN dp.document d WHERE " +
           "LOWER(dp.pageContent) LIKE LOWER(CONCAT('%', :keyword1, '%')) AND " +
           "LOWER(dp.pageContent) LIKE LOWER(CONCAT('%', :keyword2, '%')) " +
           "ORDER BY d.createdAt DESC")
    List<DocumentPage> findByMultipleKeywords(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2);
    
    @Query("SELECT COUNT(dp) FROM DocumentPage dp WHERE dp.document.id = :documentId")
    long countByDocumentId(@Param("documentId") Long documentId);
    
    @Modifying
    @Query("DELETE FROM DocumentPage dp WHERE dp.document.id = :documentId")
    void deleteByDocumentId(@Param("documentId") Long documentId);
}
