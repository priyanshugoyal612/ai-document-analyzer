package com.ai.document.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentUploadResponse {
    private Long id;
    private String filename;
    private String contentType;
    private Long fileSize;
    private String status;
    private String message;
}
