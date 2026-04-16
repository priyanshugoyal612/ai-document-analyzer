package com.ai.document.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_pages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
    
    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;
    
    @Column(name = "page_content", columnDefinition = "TEXT")
    private String pageContent;
    
    @Column(name = "word_count")
    private Integer wordCount;
    
    @Column(name = "character_count")
    private Integer characterCount;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    private void prePersist() {
        if (pageContent != null) {
            wordCount = pageContent.split("\\s+").length;
            characterCount = pageContent.length();
        }
    }
}
