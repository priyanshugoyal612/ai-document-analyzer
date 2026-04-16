package com.ai.document.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private Long id;
    private String filename;
    private String contentType;
    private Long fileSize;
    private Integer totalPages;
    private String status;
    private String errorMessage;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
